package ro.amihai.dht;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = { "ro.amihai.*" })
@EnableScheduling
public class DhtApplication {

	public static void main(String[] args) {
		SpringApplication.run(DhtApplication.class, args);
	}
}
