package com.bc.web.download.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.hsqldb.Types;

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;

/**
 * Servlet implementation class DownloadServlet
 * 
 * Sehr plumpe "Write Only" Methode, Downloads zu protokollieren: 
 * Die Download-Requests werden nur per POST beantwortet, bevorzugt,
 * nachdem Benutzer (freiwillig) einige Daten über sich eingegeben
 * haben.
 */
public class DownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private static File path = null;
	private static LookupService geoLookupService = null;
	private static long nextId = new Date().getTime();
	
	private static synchronized long getId() {
		return ++nextId;
	}
	
    public DownloadServlet() {
        super();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
    	super.init(config);
		Context initContext;
		try {
			initContext = new InitialContext();
			path = new File((String) initContext.lookup("java:/comp/env/downloadDirectory"));
			if(! path.exists()) {
				throw new ServletException("configured downloadDirectory " + path.getAbsolutePath() + " does not exist.");
			}
			if(! path.isDirectory()) {
				throw new ServletException("configured downloadDirectory " + path.getAbsolutePath() + " is not a directory.");
			}
			String geoLookupDatabaseFile = (String)initContext.lookup("java:/comp/env/geoLookupDatabase");
			geoLookupService = new LookupService(geoLookupDatabaseFile);
		} catch (Exception e) {
			throw new ServletException(e);
		}
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect("index.jsp?what=" + request.getParameter("what"));
	}

    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String what = URLEncoder.encode(request.getParameter("what"), "UTF-8");
		handleCookies(request,response);
		Location location = geoLookupService.getLocation(request.getRemoteAddr());
		int status = 200;
		try {
			status = streamContent(response, what);
		} finally {
		}
		logToDb(request, response, what, status, location);
	}
    
    public static String getCountry(HttpServletRequest request) {
		String remoteAddr = request.getRemoteAddr();
		if(remoteAddr != null) {
			if(remoteAddr.startsWith("192.168.101")) {
				return "Brockmann-Consultanien";
			} else if(remoteAddr.startsWith("127.0.0")) {
				return "Localhostien";
			}
		}
		Location location = geoLookupService.getLocation(remoteAddr);
		if(location==null || location.countryName==null) {
			return "";
		} else {
			return location.countryName;
		}
    }

	@SuppressWarnings("unchecked")
	private static void handleCookies(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, String[]> parameters = request.getParameterMap();
		if(! parameters.containsKey("setCookies")) {
			return;
		}
		Map<String, String> cookies = extractCookies(request);
		
		doit(request, response, parameters, cookies, "name");
		doit(request, response, parameters, cookies, "mail");
		doit(request, response, parameters, cookies, "location");
	}

	public static Map<String, String> extractCookies(HttpServletRequest request) {
		Cookie[] c = request.getCookies();
		HashMap<String, String> cookies = new HashMap<String, String>();
		if(c != null)
			for(Cookie cookie: c) {
				try {
					cookies.put(cookie.getName(), URLEncoder.encode(cookie.getValue(), "UTF-8"));
				} catch (UnsupportedEncodingException ignore) { }
			}
		return cookies;
	}

	private static void doit(HttpServletRequest request, HttpServletResponse response, Map<String, String[]> parameters, Map<String, String> cookies, String key) {
		if(parameters.containsKey(key)) {
			String cookieKey = cookies.get("download_" + key);
			String[] names = parameters.get(key);
			if(names!=null && names.length > 0) {
				if(cookieKey == null || ! cookieKey.equals(names[0])) {
					String uri = request.getContextPath();
					response.addCookie(createCookie("download_"+key, names[0], uri));
				}
			}
		}
	}

	private static Cookie createCookie(String name, String value,
			String requestURI) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath(requestURI);
		cookie.setMaxAge((int) (3600*24*365*3));
		return cookie;
	}

	private void logToDb(HttpServletRequest request,
			HttpServletResponse response, String what, int status, Location location) {
		Context initContext;
		try {
			initContext = new InitialContext();
			DataSource ds = (DataSource)initContext.lookup("java:/comp/env/jdbc/download");
			Connection conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("INSERT INTO access_log( " +
					"id, agent, request_file, referer, remote_host, " + 
					"request_protocol, request_uri, status, time_stamp, " +
					"bc_name, bc_mail, bc_location, bc_comment, bc_what," +
					"bc_city, bc_countryCode, bc_countryName, bc_latitude, " +
					"bc_longitude, bc_region) VALUES (" +
					"?, ?, ?, ?, ?, ?, " +
					"?, ?, ?," +
					"?, ?, ?, ?, ?," +
					"?, ?, ?, ?," +
					"?, ? )");
			int i = 1; // makes it easier to add parameters somewhere in the middle...
			stmt.setLong(   i++, getId());
			stmt.setString( i++, request.getHeader("User-Agent"));
			stmt.setString( i++, what);
			stmt.setString( i++, request.getHeader("Referer"));
			stmt.setString( i++, request.getRemoteAddr());
			stmt.setString( i++, request.getProtocol());
			stmt.setString( i++, request.getRequestURI());
			stmt.setInt(    i++, status);
			stmt.setLong(   i++, System.currentTimeMillis()/1000);
			setStmtString( stmt, i++, request.getParameter("name"), 100);
			setStmtString( stmt, i++, request.getParameter("mail"), 100);
			setStmtString( stmt, i++, request.getParameter("location"), 100);
			setStmtString( stmt, i++, request.getParameter("comment"), 512);
			setStmtString( stmt, i++, request.getParameter("what"), 256);
			if(location != null) {
				setStmtString( stmt, i++, location.city, 128);
				setStmtString( stmt, i++, location.countryCode, 10);
				setStmtString( stmt, i++, location.countryName, 64);
				stmt.setDouble( i++, location.latitude);
				stmt.setDouble( i++, location.longitude);
				setStmtString( stmt, i++, location.region, 10);
			} else {
				stmt.setNull(i++, Types.VARCHAR);
				stmt.setNull(i++, Types.VARCHAR);
				stmt.setNull(i++, Types.VARCHAR);
				stmt.setNull(i++, Types.DOUBLE);
				stmt.setNull(i++, Types.DOUBLE);
				stmt.setNull(i++, Types.VARCHAR);
			}
			stmt.executeUpdate();
			stmt.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void setStmtString(PreparedStatement stmt, int index, String value, int limit) throws SQLException {
		if(value == null) {
			stmt.setNull(index, Types.VARCHAR);
		} else {
			stmt.setString(index, limit(value,limit));
		}
	}

	private String limit(String parameter, int i) {
		if(parameter==null)
			return null;
		if(parameter.length() > i) 
			return parameter.trim().substring(0, i);
		return parameter;
	}

	private int streamContent(HttpServletResponse response, String what) throws IOException {
		try {
			FileInputStream in = null;
			File theFile = new File(path, sanitizeFilename(what));
			if(! theFile.exists() || theFile.isDirectory()) {
				response.sendRedirect("404.jsp?what=" + theFile.getName());
				return 404;
			}
			ServletOutputStream out = null;
			try {
				out = response.getOutputStream();
			    in = new FileInputStream(theFile);

			    response.setContentType(getContentType(what));
			    String attachment = "attachment; filename=" + theFile.getName();
			    response.setHeader("Content-Disposition", attachment);

			    if (theFile.length() > 0) {
			        response.setContentLength((int) theFile.length());
			    }
			    response.setHeader("Cache-Control", "private");
			    response.setHeader("Pragma", "private");

			    int len;
			    byte[] buffer = new byte[1024];
			    while ((len = in.read(buffer)) > 0) {
			        out.write(buffer, 0, len);
			    }
			} finally {
			    if (in != null) {
			        in.close();
			    }
			    out.close();
			}
			return 200;
		} catch (IOException e) {
			e.printStackTrace();
			return 500;
		}
	}

	public static boolean exists(String fileName) {
		File theFile = new File(path, sanitizeFilename(fileName));
		return theFile.exists() && !theFile.isDirectory();
	}
	
	private static String sanitizeFilename(String what) {
		return new File(what).getName();
	}

	/**
	 * seeeeeehr plump- Das gibt's irgendwo besser...
	 */
	private String getContentType(String fileName) {
		if(fileName.endsWith(".zip")) {
			return "application/zip";
		} else if(fileName.endsWith(".gz")) {
			return "application/x-gzip";
		} else if(fileName.endsWith(".sh")) {
			return "application/x-sh";
		} else if(fileName.endsWith(".txt")) {
			return "text/plain";
		} else if(fileName.endsWith(".html")) {
			return "text/html";
		} else {
			return "application/octet-stream";
		}
	}
}
