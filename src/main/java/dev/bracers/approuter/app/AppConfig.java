package dev.bracers.approuter.app;

import java.io.BufferedReader;
import java.io.FileReader;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
	
	private final String bindingsFile = "bindings_file.txt";
	
	@Bean
	CommandLineRunner getBindingsFile(AppService appService) {
		return args -> {
			try(BufferedReader br = new BufferedReader(new FileReader(bindingsFile))) {				
				String line = br.readLine();
				while (line != null) {
					String[] values = line.split("=");
					appService.pushBinding(values[0], values[1]);
					line = br.readLine();
				}
			}
		};
	}
}
