package com.example.studentmanagement.model;

import java.time.LocalDate;

public record Enrollment(long studentId, long courseId, LocalDate enrolledOn) {
}
