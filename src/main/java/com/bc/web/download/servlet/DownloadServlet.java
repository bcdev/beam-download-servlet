package com.bc.web.download.servlet;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;

/**
 * Servlet implementation class DownloadServlet
 * 
 * Sehr plumpe "Write Only" Methode, Downloads zu protokollieren: Die
 * Download-Requests werden nur per POST beantwortet, bevorzugt, nachdem
 * Benutzer (freiwillig) einige Daten über sich eingegeben haben.
 */
public class DownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String ACCESS_URI = "/access/";
	private static LookupService geoLookupService = null;
    private static String s3Host = null;
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
            s3Host = (String) initContext.lookup("java:/comp/env/s3Host");
			String geoLookupDatabaseFile = (String) initContext.lookup("java:/comp/env/geoLookupDatabase");
			geoLookupService = new LookupService(geoLookupDatabaseFile);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	/**
	 * Provide GET access only with 'special' URL, e.g. for automatic updater.
	 * (The special url currently is /access/filename)
	 * Browsers should come through the registration page and ask people about
	 * some of their data.<br/>
	 * GET access might later be filtered by user agent or similar.
	 */
	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uri = request.getRequestURI();
		String prefix = request.getContextPath() + ACCESS_URI;
		if(uri.startsWith(prefix)) {
			// it seems to be the automatic updater - this is a very basic test
			// as it only needs to know the URL
			String what = encode(uri.substring(prefix.length()));
			System.out.println(what);
			redirectToS3Download(what, request, response);
		} else {
			response.sendRedirect("index.jsp?what=" + encode(request.getParameter("what")));
		}
	}

	/**
	 * Provide POST access, that usually comes from the registration page and
	 * logs user provided data into the database.
	 */
	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String what = encode(request.getParameter("what"));
		redirectToS3Download(what, request, response);
	}

	private void redirectToS3Download(String what, HttpServletRequest request, HttpServletResponse response) throws IOException {
		handleCookies(request, response);
		Location location = geoLookupService.getLocation(request.getRemoteAddr());
		int status = 200;
		try {
			System.out.println("Redirecting of " + what + " to " + ( location!=null ? (""+location.city + " " + location.countryName + " (" + request.getRemoteAddr() + ")"):"unknown") );
            response.sendRedirect(s3Host + decode(what));
		} catch (Exception e) {
			e.printStackTrace();
            status = 500;
		}
		logToDb(request, response, what, status, location);
	}
	
	public static String getCountry(HttpServletRequest request) {
		String remoteAddr = request.getRemoteAddr();
		if (remoteAddr != null) {
			if (remoteAddr.startsWith("192.168.101")) {
				return "Brockmann-Consultanien";
			} else	if (remoteAddr.equals("141.4.215.14")) {
				return "Brockmann-Consultanien";
			} else	if (remoteAddr.equals("141.4.215.32")) {
				return "Brockmann-Consultanien";
            } else	if (remoteAddr.equals("10.3.0.70")) {
                return "Brockmann-Consultanien";
			} else if (remoteAddr.startsWith("127.0.0")) {
				return "Localhostien";
			}
		}
		Location location = geoLookupService.getLocation(remoteAddr);
		if (location == null || location.countryName == null) {
			return "";
		} else {
			return location.countryName;
		}
	}

    // used from inside the JSP
	public static String getCountrySelectOptions(HttpServletRequest request) {
		String autoLocation = getCountry(request).trim();
		boolean selected = false;
		StringBuffer buffer = new StringBuffer();
        String[] countries = CountryList.COUNTRIES;
		for (int i = 0; i < countries.length; i++) {
			buffer.append("<option");
			if (autoLocation.equalsIgnoreCase(countries[i])) {
				selected = true;
				buffer.append(" selected");
			}
			buffer.append(">").append(countries[i]).append("</option>");
		}
		if(!selected && autoLocation.trim().length() > 0) {
			buffer.append("<option selected>");
			buffer.append(autoLocation);
			buffer.append("</option>");
		} else {
			return "<option selected>Please select</option>" + buffer.toString();
		}
		return "<option>Please select</option>" + buffer.toString();
	}

	@SuppressWarnings("unchecked")
	private static void handleCookies(HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, String[]> parameters = request.getParameterMap();
		if (!parameters.containsKey("setCookies")) {
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
		if (c != null)
			for (Cookie cookie : c) {
				cookies.put(cookie.getName(), decode(cookie.getValue()));
			}
		return cookies;
	}

	private static String decode(String value) {
		try {
			return escape(URLDecoder.decode(value, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			return escape(value);
		}
	}

	private static String encode(String value) {
		if(value == null) {
			value = "";
		}
		try {
			return URLEncoder.encode(escape(value), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return value;
		}
	}

	private static String escape(String value) {
		if(value == null) {
			return "";
		}
		value = value.replace('"', ' ');
		value = value.replace('<', '(');
		value = value.replace('>', ')');
		value = value.replace('\'', ' ');
		value = value.replace('\n', ' ');
		return value;
	}
	
	private static void doit(HttpServletRequest request,
			HttpServletResponse response, Map<String, String[]> parameters,
			Map<String, String> cookies, String key) {
		if (parameters.containsKey(key)) {
			String cookieKey = cookies.get("download_" + key);
			String[] names = parameters.get(key);
			if (names != null && names.length > 0) {
				if (cookieKey == null || !cookieKey.equals(names[0])) {
					String uri = request.getContextPath();
					try {
						response.addCookie(createCookie("download_" + key,
								encode(names[0]), uri));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private static Cookie createCookie(String name, String value,
			String requestURI) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath(requestURI);
		cookie.setMaxAge(3600 * 24 * 365 * 3);
		return cookie;
	}

	private void logToDb(HttpServletRequest request,
			HttpServletResponse response, String what, int status, Location location) {
		Context initContext;
		try {
			initContext = new InitialContext();
			DataSource ds = (DataSource) initContext.lookup("java:/comp/env/jdbc/download");
			Connection conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement("INSERT INTO access_log( "
							+ "id, agent, request_file, referer, remote_host, "
							+ "request_protocol, request_uri, status, time_stamp, "
							+ "bc_name, bc_mail, bc_location, bc_comment, bc_what,"
							+ "bc_city, bc_countryCode, bc_countryName, bc_latitude, "
							+ "bc_longitude, bc_region) VALUES ("
							+ "?, ?, ?, ?, ?, ?, " + "?, ?, ?,"
							+ "?, ?, ?, ?, ?," + "?, ?, ?, ?," + "?, ? )");
			int i = 1; // makes it easier to add parameters somewhere in the middle...
			stmt.setLong(i++, getId());
			stmt.setString(i++, request.getHeader("User-Agent"));
			stmt.setString(i++, getFilename(what));
			stmt.setString(i++, request.getHeader("Referer"));
			stmt.setString(i++, request.getRemoteAddr());
			stmt.setString(i++, request.getProtocol());
			stmt.setString(i++, request.getRequestURI());
			stmt.setInt(i++, status);
			stmt.setLong(i++, System.currentTimeMillis() / 1000);
			setStmtString(stmt, i++, escape(request.getParameter("name")), 100);
			setStmtString(stmt, i++, escape(request.getParameter("mail")), 100);
			setStmtString(stmt, i++, escape(request.getParameter("location")), 100);
			setStmtString(stmt, i++, escape(request.getParameter("comment")), 512);
			setStmtString(stmt, i++, escape(request.getParameter("what")), 256);
			if (location != null) {
				setStmtString(stmt, i++, location.city, 128);
				setStmtString(stmt, i++, location.countryCode, 10);
				setStmtString(stmt, i++, location.countryName, 64);
				stmt.setDouble(i++, location.latitude);
				stmt.setDouble(i++, location.longitude);
				setStmtString(stmt, i++, location.region, 10);
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

	private void setStmtString(PreparedStatement stmt, int index, String value,
			int limit) throws SQLException {
		if (value == null) {
			stmt.setNull(index, Types.VARCHAR);
		} else {
			stmt.setString(index, limit(value, limit));
		}
	}

	private String limit(String parameter, int i) {
		if (parameter == null)
			return null;
		if (parameter.length() > i)
			return parameter.trim().substring(0, i);
		return parameter;
	}

    // used from inside the JSP
	public static boolean exists(String fileName) {
        // check no longer possible since we are using S3
        return true;
//		File theFile = new File(path, sanitizeFilename(fileName));
//		return theFile.exists() && !theFile.isDirectory();
	}

    // used from inside the JSP
    public static String getFilename(String what) {
        int slashIndex = what.lastIndexOf("/");
        return what.substring(slashIndex + 1);
	}

	private static String sanitizeFilename(String what) {
		return new File(what).getName();
	}
}
