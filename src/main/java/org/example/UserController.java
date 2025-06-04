package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return userRepository.save(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
    }

    @PostMapping("/seed")
    public void seedUsers() {
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
    }
} 