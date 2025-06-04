package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public CommandLineRunner seedUsers(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                User u1 = new User();
                u1.setUsername("Jan Kowalski");
                u1.setEmail("jan.kowalski@example.com");
                u1.setAvatarUrl("https://randomuser.me/api/portraits/men/1.jpg");
                userRepository.save(u1);
                User u2 = new User();
                u2.setUsername("Anna Nowak");
                u2.setEmail("anna.nowak@example.com");
                u2.setAvatarUrl("https://randomuser.me/api/portraits/women/2.jpg");
                userRepository.save(u2);
            }
        };
    }
}