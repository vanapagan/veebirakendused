package ee.ttu.webapps.journal;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.*;
import javax.servlet.http.*;

public class Welcome extends HttpServlet {

	private static final long serialVersionUID = -3446065402525346844L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		String username = request.getParameter("username");
		
		PrintWriter out = response.getWriter();

		StringBuilder sb = new StringBuilder();
		String docType = "<!DOCTYPE html>";
		String htmlStart = "<html lang=\"en\">";
		String head = "<head><meta charset=\"utf-8\"><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><meta name=\"description\" content=\"\"><meta name=\"author\" content=\"\"><link rel=\"icon\" href=\"../../favicon.ico\"><title>Online Journal</title><link href=\"dist/css/bootstrap.min.css\" rel=\"stylesheet\"><link href=\"ssets/css/ie10-viewport-bug-workaround.css\" rel=\"stylesheet\"><link href=\"styles/signin.css\" rel=\"stylesheet\"><link href=\"styles/navbar.css\" rel=\"stylesheet\"></head>";
		String body = "<body><div class=\"container\"><div class=\"jumbotron\"><h1>Welcome back "
				+ username.substring(0, 1).toUpperCase() + username.substring(1)
				+ "!</h1><p>Here you can browse your previous entries and make new ones.</p>";
		String jumboEnd = "</div></div></body>";
		String htmlEnd = "</html>";
		sb.append(docType);
		sb.append(htmlStart);
		sb.append(head);
		sb.append(body);

		// TODO form for new entries
		sb.append(getNewEntryForm(username));
		
		int userId = getUserId(username);
		sb.append(getUserEntries(userId));

		sb.append(jumboEnd);
		sb.append(htmlEnd);

		out.println(sb.toString());
	}
	
	private String getNewEntryForm(String username) {
		StringBuilder sb = new StringBuilder();
		sb.append("<form class=\"form-signin\" action=\"welcome\" method=\"POST\">");
		sb.append("<h2 class=\"form-signin-heading\">Make a new entry:</h2>");
		sb.append(getAllSubjects());
		sb.append("<input type=\"text\" id=\"inputEmail\" class=\"form-control\" placeholder=\"Write something...\" name=\"memo\" required>");
		sb.append("<input type=\"hidden\" id=\"inputEmail\" class=\"form-control\" name=\"username\" value=\"" + username + "\">");
		sb.append("<button class=\"btn btn-lg btn-primary btn-block\" type=\"submit\">Write</button>");
		sb.append("</form><br/>");
		
		return sb.toString();
	}
	
	private String getAllSubjects() {
		StringBuilder sb = new StringBuilder();
		try {

			Class.forName("com.mysql.cj.jdbc.Driver");

			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/journaldb", "root", "kristo");
			PreparedStatement ps = con.prepareStatement("select id, name from journaldb.subject");
			ResultSet rs = ps.executeQuery();
			sb.append("<select name=\"carlist\" form=\"carform\">");
			while (rs.next()) {
				sb.append("<option value=\"" + rs.getInt("id") + "\">" + rs.getString("name") + "</option>");
			}
			sb.append("</select>");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	private int getUserId(String username) {
		int res = 0;
		try {

			Class.forName("com.mysql.cj.jdbc.Driver");

			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/journaldb", "root", "kristo");
			PreparedStatement ps = con.prepareStatement("select id from journaldb.user where username=?");
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				res = rs.getInt("id");
			}
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	private String getUserEntries(int id) {
		StringBuilder sb = new StringBuilder();
		try {

			Class.forName("com.mysql.cj.jdbc.Driver");

			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/journaldb", "root", "kristo");
			PreparedStatement ps = con.prepareStatement(
					"select r.timestamp, o.name,r.memo from journaldb.entry r, journaldb.subject o where r.user_id=? and r.subject_id = o.id order by r.timestamp desc");
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();

			sb.append("<table class=\"table table-bordered\">");
			while (rs.next()) {
				sb.append("<tr>");
				sb.append("<td>");
				sb.append(rs.getTimestamp("timestamp"));
				sb.append("</td>");
				sb.append("<td>");
				sb.append(rs.getString("name"));
				sb.append("</td>");
				sb.append("<td>");
				sb.append(rs.getString("memo"));
				sb.append("</td>");
				sb.append("</tr>");
			}
			sb.append("</table>");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

}