package ch.admin.bag.covidcertificate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@ServletComponentScan
@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
@EnableAsync
@Slf4j
public class CCPrintingServiceApplication {

	public static void main(String[] args) {

		Environment env = SpringApplication.run(CCPrintingServiceApplication.class, args).getEnvironment();

		String protocol = "http";
		if (env.getProperty("server.ssl.key-store") != null) {
			protocol = "https";
		}
		log.info("\n----------------------------------------------------------\n\t" +
						"Yeah!!! {} is running! \n\t" +
						"\n" +
						"\tSwaggerUI: \t{}://localhost:{}/swagger-ui.html\n\t" +
						"Profile(s): \t{}" +
						"\n----------------------------------------------------------",
				env.getProperty("spring.application.name"),
				protocol,
				env.getProperty("server.port"),
				env.getActiveProfiles());

	}
}
