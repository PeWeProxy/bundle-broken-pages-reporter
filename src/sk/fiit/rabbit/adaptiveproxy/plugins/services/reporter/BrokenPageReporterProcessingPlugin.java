package sk.fiit.rabbit.adaptiveproxy.plugins.services.reporter;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import sk.fiit.peweproxy.headers.RequestHeader;
import sk.fiit.peweproxy.messages.HttpMessageFactory;
import sk.fiit.peweproxy.messages.HttpResponse;
import sk.fiit.peweproxy.messages.ModifiableHttpRequest;
import sk.fiit.peweproxy.messages.ModifiableHttpResponse;
import sk.fiit.peweproxy.plugins.PluginProperties;
import sk.fiit.peweproxy.services.ProxyService;
import sk.fiit.peweproxy.services.content.ModifiableStringService;
import sk.fiit.peweproxy.services.content.StringContentService;
import sk.fiit.rabbit.adaptiveproxy.plugins.servicedefinitions.DatabaseConnectionProviderService;
import sk.fiit.rabbit.adaptiveproxy.plugins.services.bubble.BubbleMenuProcessingPlugin;
import sk.fiit.rabbit.adaptiveproxy.plugins.utils.SqlUtils;

public class BrokenPageReporterProcessingPlugin extends BubbleMenuProcessingPlugin {
	
	String pacGeneratorUrl = "";
	
	@Override
	public HttpResponse getResponse(ModifiableHttpRequest request, HttpMessageFactory messageFactory) {
	
		StringContentService stringContentService = request.getServicesHandle().getService(StringContentService.class);

		Map<String, String> postData = getPostDataFromRequest(stringContentService.getContent());
		String content = "";
		Connection connection = null;
		
		if(postData != null && request.getServicesHandle().isServiceAvailable(DatabaseConnectionProviderService.class)) {				
			try {
				connection = request.getServicesHandle().getService(DatabaseConnectionProviderService.class).getDatabaseConnection();
	
				if (request.getRequestHeader().getRequestURI().contains("action=reportPage")) {
					content = this.reportPage(connection, postData.get("uid"), request.getRequestHeader().getField("Referer"));
				}
				if (request.getRequestHeader().getRequestURI().contains("action=getReportedStatus")) {
					content = this.getPageStatus(connection, request.getRequestHeader().getField("Referer"));
				}
				
			} finally {
				SqlUtils.close(connection);
			}
		}
		
		ModifiableHttpResponse httpResponse = messageFactory.constructHttpResponse(null, "text/html");
		ModifiableStringService stringService = httpResponse.getServicesHandle().getService(ModifiableStringService.class);
		stringService.setContent(content);
		
		return httpResponse;
	}
	
	private String reportPage(Connection connection, String uid, String url) {
		PreparedStatement stmt = null;
		java.util.Date today = new java.util.Date();
		String timestamp = new Timestamp(today.getTime()).toString();
		String formatedTimeStamp = timestamp.substring(0, timestamp.indexOf("."));
		
		try {
			stmt = connection.prepareStatement("INSERT INTO `broken_pages` (`url`, `uid`, `timestamp`) VALUES (?, ?, ?);");
			
			stmt.setString(1, url);
			stmt.setString(2, uid);
			stmt.setString(3, formatedTimeStamp);

			stmt.execute();
			
			URL requestUrl;
			try {
				requestUrl = new URL(pacGeneratorUrl);
				InputStream is = requestUrl.openStream();
			} catch (MalformedURLException e) {
				//logger.error("Could send generate pac file request ", e);
			} catch (IOException e) {
				//logger.error("Could send generate pac file request ", e);
			}

			
			return "OK";
		} catch (SQLException e) {
			//logger.error("Could not get messageboard count ", e);
			return "FAIL";
		} finally {
			SqlUtils.close(stmt);
		}
	}
	
	private String getPageStatus(Connection connection, String url) {
		PreparedStatement stmt = null;
		int pageStatus = 0;
		
		try {
			stmt = connection.prepareStatement("SELECT COUNT(`id`) FROM `broken_pages` WHERE `url` = ?;");
			
			stmt.setString(1, url);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			
			while (rs.next()) {
				pageStatus = rs.getInt(1);
			}
		} catch (SQLException e) {
			//logger.error("Could not get messageboard count ", e);
			return "FAIL";
		} finally {
			SqlUtils.close(stmt);
		}
		
		if (pageStatus > 0) {
			return "true";
		} else {
			return "false";
		}
	}
		
	private Map<String, String> getPostDataFromRequest (String requestContent) {
		try {
			requestContent = URLDecoder.decode(requestContent, "utf-8");
		} catch (UnsupportedEncodingException e) {
			//logger.warn(e);
		}
		Map<String, String> postData = new HashMap<String, String>();
		String attributeName;
		String attributeValue;

		for (String postPair : requestContent.split("&")) {
			if (postPair.split("=").length == 2) {
				attributeName = postPair.split("=")[0];
				attributeValue = postPair.split("=")[1];
				postData.put(attributeName, attributeValue);
			}
		}

		return postData;
	}
	@Override
	public boolean start (PluginProperties props) {
		this.pacGeneratorUrl = props.getProperty("pacGeneratorUrl");
		return super.start(props);
	}

	@Override
	public void desiredRequestServices (
			Set<Class<? extends ProxyService>> desiredServices,
			RequestHeader clientRQHeader) {
		super.desiredRequestServices(desiredServices, clientRQHeader);
		desiredServices.add(ModifiableStringService.class); //FIXME: toto je docasny hack kvoli late processingu, spravne tu ma byt len StringContentService
		desiredServices.add(DatabaseConnectionProviderService.class);
	}
}