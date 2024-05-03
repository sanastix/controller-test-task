package com.example.userscontrollertask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Value("${minimum.age}")    // Value taken from properties file
    private int minimumAge;

    /*
        The following functionality according to the requirements:
        1. Create user. It allows to register users who are more than [18] years old.
            The value [18] should be taken from properties file.
        2. Update one/some user fields
        3. Update all user fields
        4. Delete user
        5. Search for users by birthdate range. Add the validation which checks that “From” is less than “To”.
            Should return a list of objects
     */

    // To create users
    @PostMapping
    public ResponseEntity<?> createUser(@Validated @RequestBody User user) {
        // Validating age
        LocalDate minAgeToDate = LocalDate.now().minusYears(minimumAge);
        if (user.getBirthDate().isAfter(minAgeToDate)) {
            return buildCustomErrorResponse(HttpStatus.BAD_REQUEST, "{\"error\": \"User must be minimum " + minimumAge + " years old.\"}");
        }

        // Saving new user
        User createdUser = userService.saveUser(user);
        return buildResponse(HttpStatus.CREATED, createdUser);
    }

    // To search users by birthdate range
    @GetMapping("/search")
    public ResponseEntity<?> searchUsersByBirthDateRange(@RequestParam("from") LocalDate fromDate,
                                                         @RequestParam("to") LocalDate toDate) {
        // Validating date
        if (fromDate.isAfter(toDate)) {
            return buildCustomErrorResponse(HttpStatus.BAD_REQUEST, "{\"error\": \"'From' date must be before 'To' date.\"}");
        }

        // Searching users
        List<User> users = userService.getUsersByBirthDateRange(fromDate, toDate);
        return buildResponse(HttpStatus.OK, users);
    }

    // To partially update a user
    @PatchMapping("/part/{id}")
    public ResponseEntity<?> updateUserPartially(@PathVariable Integer id,
                                                 @RequestBody User updatedUser) {
        Optional<User> optionalUser = userService.getUserById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Optional.ofNullable(updatedUser.getEmail()).ifPresent(user::setEmail);
            Optional.ofNullable(updatedUser.getFirstName()).ifPresent(user::setFirstName);
            Optional.ofNullable(updatedUser.getLastName()).ifPresent(user::setLastName);
            Optional.ofNullable(updatedUser.getBirthDate()).ifPresent(user::setBirthDate);
            Optional.ofNullable(updatedUser.getAddress()).ifPresent(user::setAddress);
            Optional.ofNullable(updatedUser.getPhoneNumber()).ifPresent(user::setPhoneNumber);
            userService.saveUser(user);
            return buildResponse(HttpStatus.OK, user);
        } else {
            return buildGlobalErrorResponse(HttpStatus.NOT_FOUND);
        }
    }

    // To fully update a user
    @PutMapping("/full/{id}")
    public ResponseEntity<?> updateUserFully(@PathVariable Integer id,
                                             @Validated @RequestBody User updatedUser){
        Optional<User> optionalUser = userService.getUserById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setEmail(updatedUser.getEmail());
            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());
            user.setBirthDate(updatedUser.getBirthDate());
            user.setAddress(updatedUser.getAddress());
            user.setPhoneNumber(updatedUser.getPhoneNumber());
            userService.saveUser(user);
            return buildResponse(HttpStatus.OK, user);
        } else {
            return buildGlobalErrorResponse(HttpStatus.NOT_FOUND);
        }
    }

    // To delete user
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        Optional<User> optionalUser = userService.getUserById(id);
        if (optionalUser.isPresent()) {
            userService.deleteUserById(id);
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .build();
        } else {
            return buildGlobalErrorResponse(HttpStatus.NOT_FOUND);
        }
    }

    /*
        The additional functionality
     */

    // To retrieve a user by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        Optional<User> optionalUser = userService.getUserById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return buildResponse(HttpStatus.OK, user);
        } else {
            return buildGlobalErrorResponse(HttpStatus.NOT_FOUND);
        }
    }

    /*
        Helper methods
     */

    private ResponseEntity<?> buildResponse(HttpStatus status, Object body) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    private ResponseEntity<?> buildGlobalErrorResponse(HttpStatus status) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .build();
    }

    private ResponseEntity<?> buildCustomErrorResponse(HttpStatus status, Object body) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

}
