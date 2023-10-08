package dev.bracers.approuter.app;

import java.io.IOException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping(path="/app/{app}", produces=MediaType.APPLICATION_JSON_VALUE)
public class AppController {
	
	private final AppService appService;
	
	@Autowired
	public AppController(AppService appService) {
		this.appService = appService;
	}
	
	@GetMapping(value={"", "/"}, produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getAppSingle(@PathVariable String app, HttpServletRequest request) {
		return getApp(app, request);
	}
	
	@GetMapping(value="/path/**", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getApp(@PathVariable String app, HttpServletRequest request) {
		String port = appService.getPort(app);
		if (port == null) {
			return ResponseEntity.internalServerError().body("{ \"content\": \"No binding found!\" }");
		}
		
		String url = getUrl(request, app, port);
		
		try {
			ResponseEntity<String> response = appService.GET(url);
			if (response == null) {
				throw new Exception();
			}
			return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body("{ \"content\": \"Invalid URL!\" }");
		}
		
	}
	
	@PostMapping(value={"", "/"}, produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> postAppSingle(@PathVariable String app, HttpServletRequest request) {
		return postApp(app, request);
	}
	
	@PostMapping(value="/path/**", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> postApp(@PathVariable String app, HttpServletRequest request) {
		String port = appService.getPort(app);
		if (port == null) {
			return ResponseEntity.badRequest().body("{ \"content\": \"No binding found!\" }");
		}
		
		String url = getUrl(request, app, port);
		
		String contentType = request.getContentType();
		
		String requestBody;
		try {
			requestBody = request.getReader().lines().collect(Collectors.joining(""));
			
			if (contentType == "application/json") { 
				JsonParser.parseString(requestBody);
			}
		} catch (IOException e) {
			return ResponseEntity.badRequest().body("{ \"content\": \"Invalid request!\" }");
		} catch (JsonSyntaxException e) {
			return ResponseEntity.badRequest().body("{ \"content\": \"Invalid JSON!\" }");
		}
		
		try {
			ResponseEntity<String> response = appService.POST(url, requestBody, contentType);
			if (response == null) {
				throw new Exception();
			}
			return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body("{ \"content\": \"Error!\" }");
		}
	}
	
	private static String getUrl(HttpServletRequest request, String app, String port) {
		String requestURL = request.getRequestURL().append('?').append(request.getQueryString()).toString();
		
		String arguments = "";
		String[] elementsURL = requestURL.split("/app/" +  app + "/path/");
		if (elementsURL.length > 1) {
			arguments = "/" + elementsURL[1];
		}
		
		return "http://localhost:" + port + arguments;
	}
}
