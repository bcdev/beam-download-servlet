package com.bc.web.download.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
 * Sehr plumpe "Write Only" Methode, Downloads zu protokollieren: Die
 * Download-Requests werden nur per POST beantwortet, bevorzugt, nachdem
 * Benutzer (freiwillig) einige Daten über sich eingegeben haben.
 */
public class DownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String ACCESS_URI = "/access/";

	private static File path = null;
	private static LookupService geoLookupService = null;
	private static long nextId = new Date().getTime();
	private static String[] countries = new String[] {
			"Afghanistan",
			"&Aring;land Islands",
			"Albania",
			"Algeria",
			"American Samoa",
			"Andorra",
			"Angola",
			"Anguilla",
			"Antarctica",
			"Antigua and Barbuda",
			"Argentina",
			"Armenia",
			"Aruba",
			"Australia",
			"Austria",
			"Azerbaijan",
			"Bahamas",
			"Bahrain",
			"Bangladesh",
			"Barbados",
			"Belarus",
			"Belgium",
			"Belize",
			"Benin",
			"Bermuda",
			"Bhutan",
			"Bolivia",
			"Bosnia and Herzegovina",
			"Botswana",
			"Bouvet Island",
			"Brazil",
			"British Indian Ocean territory",
			"Brunei Darussalam",
			"Bulgaria",
			"Burkina Faso",
			"Burundi",
			"Cambodia",
			"Cameroon",
			"Canada",
			"Cape Verde",
			"Cayman Islands",
			"Central African Republic",
			"Chad",
			"Chile",
			"China",
			"Christmas Island",
			"Cocos (Keeling) Islands",
			"Colombia",
			"Comoros",
			"Congo",
			"Congo, Democratic Republic",
			"Cook Islands",
			"Costa Rica",
			"C&ocirc;te d'Ivoire (Ivory Coast)",
			"Croatia (Hrvatska)",
			"Cuba",
			"Cyprus",
			"Czech Republic",
			"Denmark",
			"Djibouti",
			"Dominica",
			"Dominican Republic",
			"East Timor",
			"Ecuador",
			"Egypt",
			"El Salvador",
			"Equatorial Guinea",
			"Eritrea",
			"Estonia",
			"Ethiopia",
			"Falkland Islands",
			"Faroe Islands",
			"Fiji",
			"Finland",
			"France",
			"French Guiana",
			"French Polynesia",
			"French Southern Territories",
			"Gabon",
			"Gambia",
			"Georgia",
			"Germany",
			"Ghana",
			"Gibraltar",
			"Greece",
			"Greenland",
			"Grenada",
			"Guadeloupe",
			"Guam",
			"Guatemala",
			"Guinea",
			"Guinea-Bissau",
			"Guyana",
			"Haiti",
			"Heard and McDonald Islands",
			"Honduras",
			"Hong Kong",
			"Hungary",
			"Iceland",
			"India",
			"Indonesia",
			"Iran",
			"Iraq",
			"Ireland",
			"Israel",
			"Italy",
			"Jamaica",
			"Japan",
			"Jordan",
			"Kazakhstan",
			"Kenya",
			"Kiribati",
			"Korea (north)",
			"Korea (south)",
			"Kuwait",
			"Kyrgyzstan",
			"Lao People's Democratic Republic",
			"Latvia",
			"Lebanon",
			"Lesotho",
			"Liberia",
			"Libyan Arab Jamahiriya",
			"Liechtenstein",
			"Lithuania",
			"Luxembourg",
			"Macao",
			"Macedonia, Former Yugoslav Republic Of",
			"Madagascar",
			"Malawi",
			"Malaysia",
			"Maldives",
			"Mali",
			"Malta",
			"Marshall Islands",
			"Martinique",
			"Mauritania",
			"Mauritius",
			"Mayotte",
			"Mexico",
			"Micronesia",
			"Moldova",
			"Monaco",
			"Mongolia",
			"Montserrat",
			"Morocco",
			"Mozambique",
			"Myanmar",
			"Namibia",
			"Nauru",
			"Nepal",
			"Netherlands",
			"Netherlands Antilles",
			"New Caledonia",
			"New Zealand",
			"Nicaragua",
			"Niger",
			"Nigeria",
			"Niue",
			"Norfolk Island",
			"Northern Mariana Islands",
			"Norway",
			"Oman",
			"Pakistan",
			"Palau",
			"Palestinian Territories",
			"Panama",
			"Papua New Guinea",
			"Paraguay",
			"Peru",
			"Philippines",
			"Pitcairn",
			"Poland",
			"Portugal",
			"Puerto Rico",
			"Qatar",
			"R&eacute;union",
			"Romania",
			"Russian Federation",
			"Rwanda",
			"Saint Helena",
			"Saint Kitts and Nevis",
			"Saint Lucia",
			"Saint Pierre and Miquelon",
			"Saint Vincent and the Grenadines",
			"Samoa",
			"San Marino",
			"Sao Tome and Principe",
			"Saudi Arabia",
			"Senegal",
			"Serbia and Montenegro",
			"Seychelles",
			"Sierra Leone",
			"Singapore",
			"Slovakia",
			"Slovenia",
			"Solomon Islands",
			"Somalia",
			"South Africa",
			"South Georgia and the South Sandwich Islands",
			"Spain",
			"Sri Lanka",
			"Sudan",
			"Suriname",
			"Svalbard and Jan Mayen Islands",
			"Swaziland",
			"Sweden",
			"Switzerland",
			"Syria",
			"Taiwan",
			"Tajikistan",
			"Tanzania",
			"Thailand",
			"Togo",
			"Tokelau",
			"Tonga",
			"Trinidad and Tobago",
			"Tunisia",
			"Turkey",
			"Turkmenistan",
			"Turks and Caicos Islands",
			"Tuvalu",
			"Uganda",
			"Ukraine",
			"United Arab Emirates",
			"United Kingdom",
			"United States of America",
			"Uruguay", 
			"Uzbekistan", 
			"Vanuatu", 
			"Vatican City", 
			"Venezuela",
			"Vietnam", 
			"Virgin Islands (British)", 
			"Virgin Islands (US)",
			"Wallis and Futuna Islands", 
			"Western Sahara", 
			"Yemen", 
			"Zaire",
			"Zambia", 
			"Zimbabwe" 
	};

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
			path = new File((String) initContext
					.lookup("java:/comp/env/downloadDirectory"));
			if (!path.exists()) {
				throw new ServletException("configured downloadDirectory "
						+ path.getAbsolutePath() + " does not exist.");
			}
			if (!path.isDirectory()) {
				throw new ServletException("configured downloadDirectory "
						+ path.getAbsolutePath() + " is not a directory.");
			}
			String geoLookupDatabaseFile = (String) initContext
					.lookup("java:/comp/env/geoLookupDatabase");
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
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String uri = request.getRequestURI();
		String prefix = request.getContextPath() + ACCESS_URI;
		if(uri.startsWith(prefix)) {
			// it seems to be the automatic updater - this is a very basic test
			// as it only needs to know the URL
			String what = encode(uri.substring(prefix.length()));
			System.out.println(what);
			download(what, request, response);
		} else {
			response.sendRedirect("index.jsp?what=" + encode(request.getParameter("what")));
		}
	}

	/**
	 * Provide POST access, that usually comes from the registration page and
	 * logs user provided data into the database.
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		System.out.println(request.getCharacterEncoding());
		String what = encode(request.getParameter("what"));
		download(what, request, response);
	}

	private void download(String what, HttpServletRequest request, HttpServletResponse response) throws IOException {
		handleCookies(request, response);
		// currently database dump downloads are handled by the same
		// routine - cheap safety measures, so that not everyone
		// can download the mail addresses from everywhere.
		if(what.startsWith("mysql")) {
			if(!request.getRemoteAddr().startsWith("141.4.215")) {
				ServletOutputStream out = response.getOutputStream();
				response.sendError(401, "Access denied");
				System.out.println("Denied access to mysql data from " + request.getRemoteAddr());
				return;
			} else {
				System.out.println("Allowing access to mysql data from " + request.getRemoteAddr());
			}
		}
		Location location = geoLookupService.getLocation(request.getRemoteAddr());
		int status = 200;
		try {
			System.out.println("Starting download of " + what + " to " + ( location!=null ? (""+location.city + " " + location.countryName + " (" + request.getRemoteAddr() + ")"):"unknown") );
			status = streamContent(response, what);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Finished download of " + what + " to " + ( location!=null ? (""+location.city + " " + location.countryName + " (" + request.getRemoteAddr() + ")"):"unknown") );
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

	public static String getCountrySelectOptions(HttpServletRequest request) {
		String autoLocation = getCountry(request).trim();
		boolean selected = false;
		StringBuffer buffer = new StringBuffer();
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
//				cookies.put(cookie.getName(), encode(cookie.getValue());
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
		cookie.setMaxAge((int) (3600 * 24 * 365 * 3));
		return cookie;
	}

	private void logToDb(HttpServletRequest request,
			HttpServletResponse response, String what, int status,
			Location location) {
		Context initContext;
		try {
			initContext = new InitialContext();
			DataSource ds = (DataSource) initContext
					.lookup("java:/comp/env/jdbc/download");
			Connection conn = ds.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("INSERT INTO access_log( "
							+ "id, agent, request_file, referer, remote_host, "
							+ "request_protocol, request_uri, status, time_stamp, "
							+ "bc_name, bc_mail, bc_location, bc_comment, bc_what,"
							+ "bc_city, bc_countryCode, bc_countryName, bc_latitude, "
							+ "bc_longitude, bc_region) VALUES ("
							+ "?, ?, ?, ?, ?, ?, " + "?, ?, ?,"
							+ "?, ?, ?, ?, ?," + "?, ?, ?, ?," + "?, ? )");
			int i = 1; // makes it easier to add parameters somewhere in the
						// middle...
			stmt.setLong(i++, getId());
			stmt.setString(i++, request.getHeader("User-Agent"));
			stmt.setString(i++, what);
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

	private int streamContent(HttpServletResponse response, String what) {
		try {
			FileInputStream in = null;
			File theFile = new File(path, sanitizeFilename(what));
			if (!theFile.exists() || theFile.isDirectory()) {
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
		if (fileName.endsWith(".zip")) {
			return "application/zip";
		} else if (fileName.endsWith(".gz")) {
			return "application/x-gzip";
		} else if (fileName.endsWith(".sh")) {
			return "application/x-sh";
		} else if (fileName.endsWith(".txt")) {
			return "text/plain";
		} else if (fileName.endsWith(".html")) {
			return "text/html";
		} else {
			return "application/octet-stream";
		}
	}
}
