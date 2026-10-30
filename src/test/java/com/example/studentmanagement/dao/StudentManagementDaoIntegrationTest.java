package com.example.studentmanagement.dao;

import com.example.studentmanagement.config.Database;
import com.example.studentmanagement.model.Course;
import com.example.studentmanagement.model.Enrollment;
import com.example.studentmanagement.model.Student;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentManagementDaoIntegrationTest {
    @Test
    void crudTransactionsAndQueriesWorkWithMySql() throws Exception {
        StudentManagementDao dao = new StudentManagementDao();
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String studentNumber = "T" + suffix;
        String studentEmail = suffix + "@test.invalid";
        String courseCode = "T" + suffix;
        long studentId = 0;
        long courseId = 0;

        try (Connection ignored = Database.getConnection()) {
            // Confirm database configuration before creating records or entering cleanup.
        }

        try {
            dao.addStudent(new Student(studentNumber, "Test", "Student",
                    studentEmail, LocalDate.of(2000, 1, 1)));
            dao.addCourse(new Course(courseCode, "Test Course " + suffix, 3));
            studentId = findId("students", "student_number", studentNumber);
            courseId = findId("courses", "course_code", courseCode);
            long createdStudentId = studentId;
            long createdCourseId = courseId;

            dao.enroll(new Enrollment(createdStudentId, createdCourseId, LocalDate.now()));
            assertThrows(SQLException.class,
                    () -> dao.enroll(new Enrollment(createdStudentId, Long.MAX_VALUE, LocalDate.now())));

            dao.updateStudentEmail(createdStudentId, "updated-" + studentEmail);
            dao.updateCourse(createdCourseId, "Updated Course " + suffix, 4);
            dao.updateGrade(createdStudentId, createdCourseId, new java.math.BigDecimal("91.50"));

            String students = captureOutput(dao::listStudents);
            assertTrue(students.contains(studentNumber));
            assertTrue(students.contains("updated-" + studentEmail));

            String courses = captureOutput(dao::listCourses);
            assertTrue(courses.contains("Updated Course " + suffix));

            String enrollments = captureOutput(dao::listEnrollments);
            assertTrue(enrollments.contains(studentNumber));
            assertTrue(enrollments.contains(courseCode));
            assertTrue(enrollments.contains("91.50"));

            String studentsInCourse = captureOutput(() -> dao.listStudentsInCourse(courseCode));
            assertTrue(studentsInCourse.contains(studentNumber));

            String statistics = captureOutput(dao::printCourseStatistics);
            assertTrue(statistics.contains(courseCode));
            assertTrue(statistics.contains("91.50"));

            assertThrows(SQLException.class, () -> dao.deleteCourse(createdCourseId));
            dao.dropEnrollment(createdStudentId, createdCourseId);
            dao.deleteStudent(createdStudentId);
            dao.deleteCourse(createdCourseId);
            studentId = 0;
            courseId = 0;
        } finally {
            cleanup(studentId, studentNumber, courseId, courseCode);
        }
    }

    private static long findId(String table, String column, String value) throws SQLException {
        String sql = "SELECT " + (table.equals("students") ? "student_id" : "course_id")
                + " FROM " + table + " WHERE " + column + " = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    throw new SQLException("Test record was not created in " + table);
                }
                return results.getLong(1);
            }
        }
    }

    private static String captureOutput(SqlAction action) throws SQLException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream captured = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            System.setOut(captured);
            action.run();
        } finally {
            System.setOut(original);
        }
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private static void cleanup(long studentId, String studentNumber,
                                long courseId, String courseCode) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            if (studentId != 0) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM enrollments WHERE student_id = ?")) {
                    statement.setLong(1, studentId);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM students WHERE student_id = ?")) {
                    statement.setLong(1, studentId);
                    statement.executeUpdate();
                }
            } else {
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM students WHERE student_number = ?")) {
                    statement.setString(1, studentNumber);
                    statement.executeUpdate();
                }
            }
            if (courseId != 0) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM courses WHERE course_id = ?")) {
                    statement.setLong(1, courseId);
                    statement.executeUpdate();
                }
            } else {
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM courses WHERE course_code = ?")) {
                    statement.setString(1, courseCode);
                    statement.executeUpdate();
                }
            }
        }
    }

    @FunctionalInterface
    private interface SqlAction {
        void run() throws SQLException;
    }
}
