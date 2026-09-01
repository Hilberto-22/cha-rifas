package br.com.charifa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ChaRifaApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChaRifaApplication.class, args);
    }
}
