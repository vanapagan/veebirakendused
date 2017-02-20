package ee.ttu.webapps.journal;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
			} else if (handlerAction.equals("editJournalEntry")) {
				editJournalEntry(request);
				out.println("Successfully edited a journal entry");
			} else if (handlerAction.equals("createEditJournalEntryForm")) {
				createEditJournalEntryForm(request, response);
			} else if (handlerAction.equals("deleteJournalEntry")) {
				deleteJournalEntry(request);
				out.println("Successfully deleted a journal entry");
			} else if (handlerAction.equals("signUp")) {
				String username = signUpUser(request);
				out.println("User '" + username + "' created successfully");
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

	private String signUpUser(HttpServletRequest request) throws JournalException {
		try {

			Class.forName("com.mysql.cj.jdbc.Driver");

			Connection con = getConnection();

			String username = request.getParameter("username");
			String password = request.getParameter("password");

			PreparedStatement ps = con
					.prepareStatement("insert into journaldb.user (username, password) values (?, ?)");

			ps.setString(1, username);
			ps.setString(2, password);

			ps.executeUpdate();
			return username;
		} catch (Exception e) {
			throw new JournalException("Failed to create new user " + e.getMessage(), e);
		}
		
	}

	private void editJournalEntry(HttpServletRequest request) throws JournalException {
		try {

			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection con = getConnection();

			int entryId = new Integer(request.getParameter("entryId")).intValue();

			PreparedStatement ps = con
					.prepareStatement("UPDATE journaldb.entry SET subject_id = ?, memo = ? WHERE id=?");

			ps.setInt(1, new Integer(request.getParameter("subjectId")).intValue());
			ps.setString(2, request.getParameter("memo"));
			ps.setInt(3, entryId);

			ps.executeUpdate();

		} catch (Exception e) {
			throw new JournalException("Failed to edit entry: " + e.getMessage(), e);
		}
	}

	private void createEditJournalEntryForm(HttpServletRequest request, HttpServletResponse response)
			throws JournalException {
		try {

			response.setContentType("text/html;charset=UTF-8");

			int subjectId = new Integer(request.getParameter("subjectId")).intValue();
			String username = request.getParameter("username");
			String memoText = request.getParameter("memo");

			String docType = "<!DOCTYPE html>";
			String htmlStart = "<html lang=\"en\">";
			String head = "<head><meta charset=\"utf-8\">" + "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">"
					+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
					+ "<meta name=\"description\" content=\"\"><meta name=\"author\" content=\"\">"
					+ "<link rel=\"icon\" href=\"../../favicon.ico\">"
					+ "<title>Online Journal</title><link href=\"dist/css/bootstrap.min.css\" rel=\"stylesheet\">"
					+ "<link href=\"styles/signin.css\" rel=\"stylesheet\"><link href=\"styles/navbar.css\" rel=\"stylesheet\"></head>";
			String bodyStart = "<body>";
			String container = "<div class=\"container\">";
			String form = getNewEditEntryForm(username, new Integer(request.getParameter("userId")).intValue(),
					memoText, subjectId, new Integer(request.getParameter("entryId")).intValue());
			String jumboEnd = "</div></div>";
			String bodyEnd = "</body>";
			String htmlEnd = "</html>";

			StringBuilder sb = new StringBuilder();
			sb.append(docType);
			sb.append(htmlStart);
			sb.append(head);
			sb.append(bodyStart);
			sb.append(container);
			sb.append(form);
			sb.append(jumboEnd);
			sb.append(bodyEnd);
			sb.append(htmlEnd);

			PrintWriter out = response.getWriter();
			out.println(sb.toString());

		} catch (Exception e) {
			throw new JournalException("Failed to create edit entry form: " + e.getMessage(), e);
		}
	}

	private String getNewEditEntryForm(String username, int userId, String memoText, int subjectId, int entryId) {
		StringBuilder sb = new StringBuilder();
		sb.append("<form class=\"form-signin\" action=\"handler\" method=\"POST\">");
		sb.append("<div class=\"jumbotron\"><h2>Edit entry:</h2>");
		sb.append(getAllSubjects(subjectId));
		sb.append("<input type=\"text\" class=\"form-control\" placeholder=\"Write something...\" value=\"" + memoText
				+ "\" name=\"memo\" required>");
		sb.append("<input type=\"hidden\" class=\"form-control\" name=\"username\" value=\"" + username + "\">");
		sb.append("<input type=\"hidden\" class=\"form-control\" name=\"userId\" value=" + userId + ">");
		sb.append("<input type=\"hidden\" class=\"form-control\" name=\"entryId\" value=" + entryId + ">");
		sb.append("<input type=\"hidden\" class=\"form-control\" name=\"handlerAction\" value=\"editJournalEntry\">");
		sb.append("<button class=\"btn btn-lg btn-primary btn-block\" type=\"submit\">Save changes</button>");
		sb.append("</form><br/>");

		return sb.toString();
	}

	private String getAllSubjects(int subjectId) {
		StringBuilder sb = new StringBuilder();
		try {

			Class.forName("com.mysql.cj.jdbc.Driver");

			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/journaldb", "root", "kristo");
			PreparedStatement ps = con.prepareStatement("select id, name from journaldb.subject");
			ResultSet rs = ps.executeQuery();
			sb.append("<select name=\"subjectId\">");
			while (rs.next()) {
				sb.append("<option value=\"" + rs.getInt("id") + "\" " + setSelected(rs.getInt("id"), subjectId) + ">"
						+ rs.getString("name"));

				sb.append("</option>");
			}
			sb.append("</select>");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	private String setSelected(int a, int b) {
		if (a == b) {
			return "selected=\"selected\"";
		}
		return "";
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

			PreparedStatement ps = con.prepareStatement("DELETE FROM journaldb.entry WHERE id=?");
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
