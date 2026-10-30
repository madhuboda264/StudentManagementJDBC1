package com.example.studentmanagement.model;

import java.time.LocalDate;

public record Student(
        String studentNumber,
        String firstName,
        String lastName,
        String email,
        LocalDate dateOfBirth) {
}
