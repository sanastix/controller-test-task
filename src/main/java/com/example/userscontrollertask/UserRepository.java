package com.example.userscontrollertask;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("select u from User u where u.birthDate >= ?1 and u.birthDate <= ?2")
    List<User> findUsersByBirthDateRange(LocalDate fromDate, LocalDate toDate);

}
