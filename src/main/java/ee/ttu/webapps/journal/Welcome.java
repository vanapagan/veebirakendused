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
		String head = "<head><meta charset=\"utf-8\">" + "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">"
				+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
				+ "<meta name=\"description\" content=\"\"><meta name=\"author\" content=\"\">"
				+ "<link rel=\"icon\" href=\"../../favicon.ico\">"
				+ "<title>Online Journal</title><link href=\"dist/css/bootstrap.min.css\" rel=\"stylesheet\">"
				+ "<link href=\"styles/signin.css\" rel=\"stylesheet\"><link href=\"styles/navbar.css\" rel=\"stylesheet\"></head>";
		String bodyStart = "<body>";

		String js = "<script>" + "function deleteEntry(id) {" + "document.getElementById(\"entryId\").value = id;"
				+ "document.getElementById(\"handlerAction\").value = \"deleteJournalEntry\"; "
				+ "document.getElementById(\"manipulateEntry\").submit();}"
				+ "function editEntry(id, memoText, subjectId) {" + "document.getElementById(\"entryId\").value = id;"
				+ "document.getElementById(\"memoText\").value = memoText;"
				+ "document.getElementById(\"subjectId\").value = subjectId;"
				+ "document.getElementById(\"handlerAction\").value = \"createEditJournalEntryForm\";"
				+ "}" + "</script>";

		String container = "<div class=\"container\"><div class=\"jumbotron\"><h1>Welcome back "
				+ username.substring(0, 1).toUpperCase() + username.substring(1)
				+ "!</h1><p>Here you can browse your previous entries and make new ones.</p>";
		String jumboEnd = "</div></div>";
		String bodyEnd = "</body>";
		String htmlEnd = "</html>";
		sb.append(docType);
		sb.append(htmlStart);
		sb.append(head);
		sb.append(bodyStart);
		sb.append(js);
		sb.append(container);

		int userId = getUserId(username);

		sb.append(getNewEntryForm(username, userId));
		sb.append(getUserEntries(userId, username));
		sb.append(jumboEnd);
		sb.append(bodyEnd);
		sb.append(htmlEnd);

		out.println(sb.toString());
	}

	private String getNewEntryForm(String username, int userId) {
		StringBuilder sb = new StringBuilder();
		sb.append("<form class=\"form-signin\" action=\"handler\" method=\"POST\">");
		sb.append("<h2 class=\"form-signin-heading\">Make a new entry:</h2>");
		sb.append(getAllSubjects());
		sb.append(
				"<input type=\"text\" id=\"inputEmail\" class=\"form-control\" placeholder=\"Write something...\" name=\"memo\" required>");
		sb.append("<input type=\"hidden\" class=\"form-control\" name=\"username\" value=\"" + username + "\">");
		sb.append("<input type=\"hidden\" class=\"form-control\" name=\"userId\" value=" + userId + ">");
		sb.append("<input type=\"hidden\" class=\"form-control\" name=\"handlerAction\" value=\"addJournalEntry\">");
		sb.append("<button class=\"btn btn-lg btn-primary btn-block\" type=\"submit\">Save</button>");
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
			sb.append("<select name=\"subjectId\" class=\"selectpicker\">");
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

	private String getUserEntries(int id, String username) {
		StringBuilder sb = new StringBuilder();
		try {

			Class.forName("com.mysql.cj.jdbc.Driver");

			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/journaldb", "root", "kristo");
			PreparedStatement ps = con.prepareStatement(
					"select r.timestamp, o.name, r.memo, r.id, r.subject_id from journaldb.entry r, journaldb.subject o where r.user_id=? and r.subject_id = o.id order by r.timestamp desc");
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();

			sb.append("<form action=\"handler\" id=\"manipulateEntry\" method=\"POST\">");
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
				sb.append("<td>");
				sb.append("<button class=\"btn btn-edit\" onclick=\"editEntry(" + rs.getInt("id") + ", "
						+ "'" + rs.getString("memo") + "'" + ", " + rs.getInt("subject_id")
						+ ")\"><span class=\"glyphicon glyphicon-edit\"></span></button>");
				sb.append("<button class=\"btn btn-danger\" onclick=\"deleteEntry("
						+ rs.getInt("id") + ")\"><span class=\"glyphicon glyphicon-trash\"></span></button>");
				sb.append("</td>");
				sb.append("</tr>");
			}
			sb.append("</table>");
			sb.append("<input type=\"hidden\" id=\"entryId\" class=\"form-control\" name=\"entryId\">");
			sb.append("<input type=\"hidden\" id=\"memoText\" class=\"form-control\" name=\"memo\">");
			sb.append("<input type=\"hidden\" id=\"userId\" class=\"form-control\" name=\"userId\" value=\"" + id + "\">");
			sb.append("<input type=\"hidden\" id=\"subjectId\" class=\"form-control\" name=\"subjectId\">");
			sb.append("<input type=\"hidden\" id=\"handlerAction\" class=\"form-control\" name=\"handlerAction\">");
			sb.append("<input type=\"hidden\" class=\"form-control\" name=\"username\" value=\"" + username + "\">");
			sb.append("</form><br/>");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

}