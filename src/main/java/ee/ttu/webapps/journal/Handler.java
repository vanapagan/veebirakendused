package ee.ttu.webapps.journal;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Handler extends HttpServlet {

	private static final long serialVersionUID = -2185308049410360441L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();

		String handlerAction = request.getParameter("handlerAction");
		try {
			if (handlerAction.equals("addJournalEntry")) {
				addJournalEntry(request);
				out.println("Successfully created a new journal entry");

			} else if (handlerAction.equals("deleteJournalEntry")) {
				deleteJournalEntry(request);
				out.println("Successfully deleted a journal entry");

			} else {
				throw new JournalException("Handler action '" + handlerAction + "' is not supported at the moment");
			}
			RequestDispatcher rs = request.getRequestDispatcher("welcome");
			rs.include(request, response);
		} catch (JournalException e) {
			handleException(e, out, request, response);
		} catch (Exception e) {
			handleException(e, out, request, response);
		}
	}

	private void handleException(Throwable t, PrintWriter out, HttpServletRequest request,
			HttpServletResponse response) {
		out.println(t.getMessage());
		RequestDispatcher rs = request.getRequestDispatcher("index.html");
		try {
			rs.include(request, response);
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void deleteJournalEntry(HttpServletRequest request) throws JournalException {
		try {

			Class.forName("com.mysql.cj.jdbc.Driver");

			Connection con = getConnection();

			int entryId = new Integer(request.getParameter("entryId")).intValue();

			PreparedStatement ps = con.prepareStatement("DELETE FROM journaldb.entry WHERE id=?;");
			ps.setInt(1, entryId);

			ps.executeUpdate();

		} catch (Exception e) {
			throw new JournalException("Failed to delete entry: " + e.getMessage(), e);
		}
	}

	private static void addJournalEntry(HttpServletRequest request) throws JournalException {

		try {

			Class.forName("com.mysql.cj.jdbc.Driver");

			Connection con = getConnection();

			int userId = new Integer(request.getParameter("userId")).intValue();
			int subjectId = new Integer(request.getParameter("subjectId")).intValue();
			String memo = request.getParameter("memo");

			PreparedStatement ps = con
					.prepareStatement("insert into journaldb.entry (user_id, subject_id, memo) values (?, ?, ?)");

			ps.setInt(1, userId);
			ps.setInt(2, subjectId);
			ps.setString(3, memo);

			ps.executeUpdate();

		} catch (Exception e) {
			throw new JournalException("Failed to add a new entry to the journal: " + e.getMessage(), e);
		}

	}

	private static Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:mysql://localhost:3306/journaldb", "root", "kristo");
	}

}
