package com.example.studentmanagement.dao;

import com.example.studentmanagement.config.Database;
import com.example.studentmanagement.model.Course;
import com.example.studentmanagement.model.Enrollment;
import com.example.studentmanagement.model.Student;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public final class StudentManagementDao {
    public StudentManagementDao() {
    }

    public void addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO students (student_number, first_name, last_name, email, date_of_birth) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, student.studentNumber());
            statement.setString(2, student.firstName());
            statement.setString(3, student.lastName());
            statement.setString(4, student.email());
            statement.setDate(5, Date.valueOf(student.dateOfBirth()));
            statement.executeUpdate();
        }
    }

    public void listStudents() throws SQLException {
        String sql = "SELECT student_id, student_number, first_name, last_name, email, date_of_birth "
                + "FROM students ORDER BY last_name, first_name";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            System.out.printf("%-5s %-14s %-24s %-30s %s%n",
                    "ID", "Number", "Name", "Email", "Birth date");
            while (results.next()) {
                System.out.printf("%-5d %-14s %-24s %-30s %s%n",
                        results.getLong("student_id"), results.getString("student_number"),
                        results.getString("first_name") + " " + results.getString("last_name"),
                        results.getString("email"), results.getDate("date_of_birth"));
            }
        }
    }

    public void updateStudentEmail(long studentId, String email) throws SQLException {
        String sql = "UPDATE students SET email = ? WHERE student_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setLong(2, studentId);
            requireUpdated(statement.executeUpdate(), "Student");
        }
    }

    public void deleteStudent(long studentId) throws SQLException {
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, studentId);
            requireUpdated(statement.executeUpdate(), "Student");
        }
    }

    public void addCourse(Course course) throws SQLException {
        String sql = "INSERT INTO courses (course_code, title, credits) VALUES (?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, course.courseCode());
            statement.setString(2, course.title());
            statement.setInt(3, course.credits());
            statement.executeUpdate();
        }
    }

    public void listCourses() throws SQLException {
        String sql = "SELECT course_id, course_code, title, credits FROM courses ORDER BY course_code";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            System.out.printf("%-5s %-14s %-36s %s%n", "ID", "Code", "Title", "Credits");
            while (results.next()) {
                System.out.printf("%-5d %-14s %-36s %d%n",
                        results.getLong("course_id"), results.getString("course_code"),
                        results.getString("title"), results.getInt("credits"));
            }
        }
    }

    public void updateCourse(long courseId, String title, int credits) throws SQLException {
        String sql = "UPDATE courses SET title = ?, credits = ? WHERE course_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            statement.setInt(2, credits);
            statement.setLong(3, courseId);
            requireUpdated(statement.executeUpdate(), "Course");
        }
    }

    public void deleteCourse(long courseId) throws SQLException {
        String sql = "DELETE FROM courses WHERE course_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, courseId);
            requireUpdated(statement.executeUpdate(), "Course");
        }
    }

    public void enroll(Enrollment enrollment) throws SQLException {
        String sql = "INSERT INTO enrollments (student_id, course_id, enrolled_on) VALUES (?, ?, ?)";
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                requireExists(connection, "students", "student_id", enrollment.studentId());
                requireExists(connection, "courses", "course_id", enrollment.courseId());
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setLong(1, enrollment.studentId());
                    statement.setLong(2, enrollment.courseId());
                    statement.setDate(3, Date.valueOf(enrollment.enrolledOn()));
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
                throw exception;
            }
        }
    }

    public void updateGrade(long studentId, long courseId, BigDecimal grade) throws SQLException {
        String sql = "UPDATE enrollments SET grade = ? WHERE student_id = ? AND course_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, grade);
            statement.setLong(2, studentId);
            statement.setLong(3, courseId);
            requireUpdated(statement.executeUpdate(), "Enrollment");
        }
    }

    public void dropEnrollment(long studentId, long courseId) throws SQLException {
        String sql = "DELETE FROM enrollments WHERE student_id = ? AND course_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, studentId);
            statement.setLong(2, courseId);
            requireUpdated(statement.executeUpdate(), "Enrollment");
        }
    }

    public void listEnrollments() throws SQLException {
        String sql = "SELECT s.student_number, s.first_name, s.last_name, c.course_code, "
                + "c.title, e.enrolled_on, e.grade FROM enrollments e "
                + "JOIN students s ON s.student_id = e.student_id "
                + "JOIN courses c ON c.course_id = e.course_id "
                + "ORDER BY c.course_code, s.last_name, s.first_name";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            System.out.printf("%-14s %-24s %-12s %-30s %-12s %s%n",
                    "Student #", "Student", "Course", "Title", "Enrolled", "Grade");
            while (results.next()) {
                BigDecimal grade = results.getBigDecimal("grade");
                System.out.printf("%-14s %-24s %-12s %-30s %-12s %s%n",
                        results.getString("student_number"),
                        results.getString("first_name") + " " + results.getString("last_name"),
                        results.getString("course_code"), results.getString("title"),
                        results.getDate("enrolled_on"), grade == null ? "Not graded" : grade);
            }
        }
    }

    public void listStudentsInCourse(String courseCode) throws SQLException {
        String sql = "SELECT student_number, first_name, last_name, email FROM students "
                + "WHERE student_id IN (SELECT e.student_id FROM enrollments e "
                + "JOIN courses c ON c.course_id = e.course_id WHERE c.course_code = ?) "
                + "ORDER BY last_name, first_name";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, courseCode);
            try (ResultSet results = statement.executeQuery()) {
                System.out.println("Students enrolled in " + courseCode + ":");
                while (results.next()) {
                    System.out.printf("%s | %s %s | %s%n",
                            results.getString("student_number"), results.getString("first_name"),
                            results.getString("last_name"), results.getString("email"));
                }
            }
        }
    }

    public void printCourseStatistics() throws SQLException {
        String sql = "SELECT c.course_code, c.title, COUNT(e.student_id) AS enrollment_count, "
                + "ROUND(AVG(e.grade), 2) AS average_grade FROM courses c "
                + "LEFT JOIN enrollments e ON e.course_id = c.course_id "
                + "GROUP BY c.course_id, c.course_code, c.title ORDER BY c.course_code";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            System.out.printf("%-14s %-32s %-12s %s%n",
                    "Course", "Title", "Enrollments", "Average grade");
            while (results.next()) {
                System.out.printf("%-14s %-32s %-12d %s%n",
                        results.getString("course_code"), results.getString("title"),
                        results.getLong("enrollment_count"),
                        results.getBigDecimal("average_grade") == null
                                ? "No grades" : results.getBigDecimal("average_grade"));
            }
        }
    }

    private void requireExists(Connection connection, String table, String idColumn, long id)
            throws SQLException {
        String sql = "SELECT 1 FROM " + table + " WHERE " + idColumn + " = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException(table + " record " + id + " does not exist.");
                }
            }
        }
    }

    private static void requireUpdated(int rowCount, String entity) throws SQLException {
        if (rowCount == 0) {
            throw new SQLException(entity + " was not found; no changes were made.");
        }
    }
}
