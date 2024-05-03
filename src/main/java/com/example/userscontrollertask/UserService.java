package com.example.userscontrollertask;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getUsersByBirthDateRange(LocalDate from, LocalDate to) {
        return userRepository.findUsersByBirthDateRange(from, to);
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public void deleteUserById(Integer id) {
        userRepository.deleteById(id);
    }

}
