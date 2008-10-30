<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>404</title>
</head>
<body>
<% String what=java.net.URLEncoder.encode(request.getParameter("what"), "UTF-8"); %>
	Sorry, the requested file <%=what %> does not exist. Please notify 
	the BEAM Team of the page where you found the link to this file
</body>
</html>