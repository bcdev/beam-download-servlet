<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@page import="com.bc.web.download.servlet.DownloadServlet"%>
<%@page import="java.util.Map"%><html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Download</title>
	<style type="text/css">
		body.prod {
			font-size:12px;
		}
		
		h1 {
			font-size:12px;
			background-color: #dddddd;
			text-align: center;
		}
	</style>
</head>
<body class="prod">
<% String what=java.net.URLEncoder.encode(request.getParameter("what"), "UTF-8"); %>
	<h1><%=what%></h1>
	<p>You have requested to download <b><%=what%></b>.</p>
	<%
	if(DownloadServlet.exists(what)) { 
		Map<String, String> cookies = DownloadServlet.extractCookies(request);
		String location = cookies.containsKey("download_location") ? cookies.get("download_location") : DownloadServlet.getCountry(request); 
		String name     = cookies.containsKey("download_name")     ? cookies.get("download_name") : ""; 
		String mail     = cookies.containsKey("download_mail")     ? cookies.get("download_mail") : ""; 
	%>
	<p>
		We'd like to know more about our users. You are free to leave the fields 
		blank and just continue with the download.
	</p>
	<p>
		We'll use your data only internally and will never give them away. 
		Anonymous statistics (e.g. about the geographical locations of our users) 
		may be posted or shared with others. 
	</p>
	<form action="get" method="post">
		<input type="hidden" name="what" value="<%=what%>"/>
		Please tell us, where you are from:<br/> 
		<input type="text" name="location" value="<%=location %>" size="40"/><br/><br/> 
		We'd like to know, who you are: <br/>
		<input type="text" name="name" value="<%=name %>" size="40"/><br/><br/> 
		If you like to be notified of new releases, please provide your mail address (very low frequency):<br/>
		<input type="text" name="mail" value="<%=mail %>" size="40"/><br/><br/> 
		What do you use BEAM for? Is there anything else, you'd like us to know?<br/>
		<textarea name="comment" value="" cols="40" rows="6"></textarea><br/><br/> 
		Should we remember these values for later downloads (cookies will be set - check if ok)?
		<input type="checkbox" name="setCookies" value="on" checked="checked"><br/><br/> 
		<input type="submit" name="submit" value="download <%=what %>" style="font-weight: bold; background-color: #EEEEBB;"/>
	</form>
	<%
	} else {
	%>
	<p>Sorry, the file does not exist.</p>
	<%
	}
	%>
</body>
</html>