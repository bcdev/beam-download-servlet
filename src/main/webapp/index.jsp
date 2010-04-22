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
			font-family:Verdana,Arial,Helvetica,sans-serif;
		}
		
		h1 {
			font-size:12px;
			background-color: #dddddd;
			text-align: center;
		}
	</style>
</head>
<body class="prod">
<%
   String what = request.getParameter("what");
   if(what==null) {
	   response.sendRedirect("http://www.brockmann-consult.de/beam");
	   return;
   }
   //String whatDecoded = java.net.URLEncoder.encode(what, "UTF-8");
   String filename = DownloadServlet.getFilename(what);
   
   String description = request.getParameter("description");
   if(description == null) description = "BEAM";
   description = java.net.URLEncoder.encode(description, "UTF-8");
   %>
	<p>You have requested to download <b><%=filename%></b>.</p>
	<%
	if(DownloadServlet.exists(what)) { 
		Map<String, String> cookies = DownloadServlet.extractCookies(request);
		String location = cookies.containsKey("download_location") ? cookies.get("download_location") : DownloadServlet.getCountry(request); 
		String name     = cookies.containsKey("download_name")     ? cookies.get("download_name") : ""; 
		String mail     = cookies.containsKey("download_mail")     ? cookies.get("download_mail") : ""; 
	%>
	<p>
		We would like to know more about our users. <b>This survey is completely optional.</b> 
		You are free to leave the fields blank and just continue with the download.
	</p>
	<p>
		We will use your data only internally and will never give them away. 
		Anonymous statistics (e.g. about the geographical locations of our users) 
		may be posted or shared with others. 
	</p>
	<form action="get" method="post" accept-charset="utf-8">
		<input type="hidden" name="what" value="<%=what%>"/>
		<b>Please tell us, where you are from:</b><br/>
		<select name="location" size="1"><%=DownloadServlet.getCountrySelectOptions(request) %></select><br/><br/> 
		<b>We would like to know, who you are: </b><br/>
		<input type="text" name="name" value="<%=name %>" size="40"/><br/><br/> 
		<b>If you like to be notified of new releases, please provide your mail address:</b> (very low frequency)<br/>
		<input type="text" name="mail" value="<%=mail %>" size="40"/><br/><br/> 
		<b>What do you use <%=description %> for? Is there anything else, you would like us to know?</b><br/>
		<textarea name="comment" value="" cols="40" rows="6"></textarea><br/><br/> 
		<input type="checkbox" name="setCookies" value="on" checked="checked">
		<b>Remember these settings for later downloads?</b> (cookies will be set)
		<br/><br/> 
		<center><input type="submit" name="submit" value="Download"/></center>
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