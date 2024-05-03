package com.example.userscontrollertask;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    @Test
    public void testCreateUser() throws Exception {
        // Prepare a valid user
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setBirthDate(LocalDate.of(1990, 1, 1));

        // Mock the userService to return the user upon save
        given(userService.saveUser(any(User.class))).willReturn(user);

        // Perform POST request to create the user
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john.doe@example.com\",\"birthDate\":\"1990-01-01\"}"))
                .andExpect(status().isCreated()) // Expecting a 201 Created response
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.birthDate").value("1990-01-01"));
    }

    @Test
    public void testCreateUser_InvalidAge() throws Exception {
        // Create a user object with birthdate that violates minimum age requirement
        User user = new User();
        user.setId(1);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setBirthDate(LocalDate.now()); // This would violate the minimum age requirement

        // Perform POST request to create user with invalid age
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"firstName\":\"John\",\"lastName\":\"Doe\",\"birthDate\":\"2024-05-04\"}"))
                        .andExpect(status().isBadRequest()); // Expecting a 400 Bad Request response
    }

    @Test
    public void testGetUsersByBirthDateRange() throws Exception {
        // Mock user data
        User user = new User();
        user.setEmail("some.mail@some.com");
        user.setFirstName("FName");
        user.setLastName("LName");
        user.setBirthDate(LocalDate.parse("1995-05-15"));
        List<User> someUsers = List.of(user);

        // Mock userService to return users within the specified date range
        LocalDate fromDate = LocalDate.parse("1992-07-17");
        LocalDate toDate = LocalDate.parse("2001-07-16");
        given(userService.getUsersByBirthDateRange(fromDate, toDate)).willReturn(someUsers);

        // Perform GET request with specified date range
        mvc.perform(get("/users/search")
                        .param("from", "1992-07-17")
                        .param("to", "2001-07-16")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))) // Assert for size of the response array
                .andExpect(jsonPath("$[0].birthDate").value(user.getBirthDate().toString())); // Assert for birthDate value

    }

    @Test
    public void testUpdateUserPartially() throws Exception {
        Integer userId = 1;
        User user = new User();
        user.setId(userId);
        given(userService.getUserById(userId)).willReturn(Optional.of(user));

        User updatedUser = new User();
        updatedUser.setEmail("updated@example.com");

        // Perform PATCH request to partially update the user
        mvc.perform(patch("/users/part/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"updated@example.com\"}"))
                .andExpect(status().isOk()); // Expecting a 200 OK response

        // Verify that the user details are updated
        verify(userService).saveUser(user);
        assertEquals("updated@example.com", user.getEmail());
    }

    @Test
    public void testUpdateUserPartially_UserNotFound() throws Exception {
        Integer userId = 1;
        given(userService.getUserById(userId)).willReturn(Optional.empty());

        // Perform PATCH request to partially update the user
        mvc.perform(patch("/users/part/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"updated@example.com\"}"))
                .andExpect(status().isNotFound()); // Expecting a 404 Not Found response
    }

    @Test
    public void testUpdateUserFully() throws Exception {
        Integer userId = 1;
        User user = new User();
        user.setId(userId);
        given(userService.getUserById(userId)).willReturn(Optional.of(user));

        User updatedUser = new User();
        updatedUser.setEmail("updated@example.com");
        updatedUser.setFirstName("John");
        updatedUser.setLastName("Doe");
        updatedUser.setBirthDate(LocalDate.of(1990, 1, 1));

        // Perform PUT request to fully update the user
        mvc.perform(put("/users/full/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"updated@example.com\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"birthDate\":\"1990-01-01\"}"))
                .andExpect(status().isOk()); // Expecting a 200 OK response

        // Verify that the user details are fully updated
        verify(userService).saveUser(user);
        assertEquals("updated@example.com", user.getEmail());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals(LocalDate.of(1990, 1, 1), user.getBirthDate());
    }

    @Test
    public void testUpdateUserFully_UserNotFound() throws Exception {
        // Define user ID
        Integer userId = 1;

        // Mocking user not found in the service
        given(userService.getUserById(userId)).willReturn(Optional.empty());

        // Perform PUT request to fully update the user
        mvc.perform(put("/users/full/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"updated@example.com\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"birthDate\":\"1990-01-01\"}"))
                .andExpect(status().isNotFound()); // Expecting a 404 Not Found response
    }

    @Test
    public void testGetUserById() throws Exception {
        // Create a user with an ID
        User user = new User();
        user.setId(5);
        user.setFirstName("Joe");

        given(userService.getUserById(user.getId())).willReturn(Optional.of(user));

        // Perform a GET request to retrieve the user by ID
        mvc.perform(get("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(user.getFirstName())); // Assert the user's firstName
    }

    @Test
    public void testDeleteUser() throws Exception {
        Integer userId = 1;

        // Mocking user existence in the service
        given(userService.getUserById(userId)).willReturn(Optional.of(new User()));

        // Perform DELETE request to delete the user
        mvc.perform(delete("/users/delete/{id}", userId))
                .andExpect(status().isOk()); // Expecting a 200 OK response

        // Verify that the deleteUserById method is called with the correct ID
        verify(userService).deleteUserById(userId);
    }

    @Test
    public void testDeleteUser_UserNotFound() throws Exception {
        Integer userId = 1;

        // Mocking user not found in the service
        given(userService.getUserById(userId)).willReturn(Optional.empty());

        // Perform DELETE request to delete the user
        mvc.perform(delete("/users/delete/{id}", userId))
                .andExpect(status().isNotFound()); // Expecting a 404 Not Found response

        // Verify that the deleteUserById method is not called
        verify(userService, never()).deleteUserById(userId);
    }

}
