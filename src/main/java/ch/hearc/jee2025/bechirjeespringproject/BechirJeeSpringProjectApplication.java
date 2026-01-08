package ch.hearc.jee2025.bechirjeespringproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BechirJeeSpringProjectApplication {

    public static void main(String[] args) {
        System.out.println("Before Start");
        SpringApplication.run(BechirJeeSpringProjectApplication.class, args);
        System.out.println("After Start");
    }

}
