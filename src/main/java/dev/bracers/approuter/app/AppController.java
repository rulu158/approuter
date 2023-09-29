package dev.bracers.approuter.app;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonParser;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path="/app/{app}", produces="application/json")
public class AppController {
	
	private final AppService appService;
	
	@Autowired
	public AppController(AppService appService) {
		this.appService = appService;
	}
	
	@GetMapping(value={"", "/"})
	public String getAppSingle(@PathVariable String app, HttpServletRequest request) {
		return getApp(app, request);
	}
	
	@GetMapping("/path/**")
	public String getApp(@PathVariable String app, HttpServletRequest request) {
		String port = appService.getPort(app);
		if (port == null) {
			return "{ \"content\": \"No binding found!\" }";
		}
		
		String url = getUrl(request, app, port);
		
		try {
			String jsonResponse = appService.GET(url);
			if (jsonResponse == null) {
				throw new Exception();
			}
			return jsonResponse;
		} catch (Exception e) {
			return "{ \"content\": \"Invalid URL!\" }";
		}
		
	}
	
	@PostMapping(value={"", "/"})
	public String postAppSingle(@PathVariable String app, HttpServletRequest request) {
		return postApp(app, request);
	}
	
	@PostMapping("/path/**")
	public String postApp(@PathVariable String app, HttpServletRequest request) {
		String port = appService.getPort(app);
		if (port == null) {
			return "{ \"content\": \"No binding found!\" }";
		}
		
		String url = getUrl(request, app, port);
		
		String jsonRequest;
		try {
			jsonRequest = request.getReader().lines().collect(Collectors.joining(""));
			JsonParser.parseString(jsonRequest);
		} catch (Exception e) {
			return "{ \"content\": \"Invalid JSON!\" }";
		}
		
		try {
			String jsonResponse = appService.POST(url, jsonRequest);
			if (jsonResponse == null) {
				throw new Exception();
			}
			return jsonResponse;
		} catch (Exception e) {
			return "{ \"content\": \"Invalid URL!\" }";
		}
	}
	
	private static String getUrl(HttpServletRequest request, String app, String port) {
		String requestURL = request.getRequestURL().toString();
		
		String arguments = "";
		String[] elementsURL = requestURL.split("/app/" +  app + "/path/");
		if (elementsURL.length > 1) {
			arguments = "/" + elementsURL[1];
		}

		return "http://localhost:" + port + arguments;
	}
}
