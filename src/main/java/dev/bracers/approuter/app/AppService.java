package dev.bracers.approuter.app;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AppService {
	private Map<String, String> bindings = new HashMap<String, String>();
	
	public void pushBinding(String path, String port) {
		bindings.put(path, port);
	}
	
	public String getPort(String path) {
		return bindings.get(path);
	}

	public ResponseEntity<String> GET(String urlToRead) throws MalformedURLException {
		StringBuilder response = new StringBuilder();
		
		URL url = new URL(urlToRead);
		
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(conn.getInputStream()))) {
				for (String line; (line = reader.readLine()) != null; ) {
					response.append(line);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body("");
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		
		return ResponseEntity.ok().body(response.toString());
	}

	public ResponseEntity<String> POST(String urlToRead, String request, String contentType) throws MalformedURLException {
		System.out.println("REQUEST: " + request);
		
		int responseStatus;
		StringBuilder response = new StringBuilder();
		
		URL url = new URL(urlToRead);
		
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", contentType);
			
			conn.setRequestProperty("Content-Length",
					Integer.toString(request.getBytes().length));
			
			conn.setUseCaches(false);
			conn.setDoOutput(true);
			
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(request);
			wr.close();
			
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(conn.getInputStream()))) {
				for (String line; (line = reader.readLine()) != null; ) {
					response.append(line);
				}
			}
			
			responseStatus = conn.getResponseCode();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body("");
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		
		return ResponseEntity.status(responseStatus).body(response.toString());
	}
}
