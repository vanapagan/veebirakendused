package ee.ttu.webapps.journal;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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

		String handlderAction = request.getParameter("handlerAction");
		if (handlderAction.equals("addJournalEntry")) {
			addJournalEntry(request);
			out.println("Successfully created a new journal entry");
			RequestDispatcher rs = request.getRequestDispatcher("welcome");
			rs.include(request, response);
		} else if (handlderAction.equals("deleteJournalEntry")) {
			deleteJournalEntry(request);
			out.println("Successfully deleted journal entry");
			RequestDispatcher rs = request.getRequestDispatcher("welcome");
			rs.include(request, response);
		} else {
			out.println("Failed to create a new entry to the journal");
			RequestDispatcher rs = request.getRequestDispatcher("index.html");
			rs.include(request, response);
		}
		
	}
	
	private static void deleteJournalEntry(HttpServletRequest request) {
		try {
			// loading drivers for mysql
			Class.forName("com.mysql.cj.jdbc.Driver");
			
			// creating connection with the database
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/journaldb", "root", "kristo");
			
			int entryId = new Integer(request.getParameter("entryId")).intValue();	
			
			PreparedStatement ps = con.prepareStatement("DELETE FROM journaldb.entry WHERE id=?;");
			ps.setInt(1, entryId);
			
			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void addJournalEntry(HttpServletRequest request) {
		
		try {
			// loading drivers for mysql
			Class.forName("com.mysql.cj.jdbc.Driver");
			
			// creating connection with the database
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/journaldb", "root", "kristo");
			
			int userId = new Integer(request.getParameter("userId")).intValue();	
			int subjectId = new Integer(request.getParameter("subjectId")).intValue();
			String memo = request.getParameter("memo");
			
			PreparedStatement ps = con.prepareStatement("insert into journaldb.entry (user_id, subject_id, memo) values (?, ?, ?)");
			
			ps.setInt(1, userId);
			ps.setInt(2, subjectId);
			ps.setString(3, memo);
			
			ps.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
