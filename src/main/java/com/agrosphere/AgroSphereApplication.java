package com.agrosphere;
import org.springframework.boot.SpringApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAsync
public class AgroSphereApplication {
    public static void main(String[] args) { SpringApplication.run(AgroSphereApplication.class, args); }
}
