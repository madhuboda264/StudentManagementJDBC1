package com.example.studentmanagement.app;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;

import com.example.studentmanagement.dao.StudentManagementDao;
import com.example.studentmanagement.model.Course;
import com.example.studentmanagement.model.Enrollment;
import com.example.studentmanagement.model.Student;

public final class StudentManagementApp {
    private StudentManagementApp() {
    }

    public static void main(String[] args) {
        StudentManagementDao dao = new StudentManagementDao();
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Student Management System (MySQL/JDBC)");
            boolean running = true;
            while (running) {
                printMenu();
                String choice = scanner.nextLine().trim();
                try {
                    switch (choice) {
                        case "1" -> addStudent(scanner, dao);
                        case "2" -> dao.listStudents();
                        case "3" -> updateStudent(scanner, dao);
                        case "4" -> dao.deleteStudent(readLong(scanner, "Student ID: "));
                        case "5" -> addCourse(scanner, dao);
                        case "6" -> dao.listCourses();
                        case "7" -> updateCourse(scanner, dao);
                        case "8" -> dao.deleteCourse(readLong(scanner, "Course ID: "));
                        case "9" -> dao.enroll(new Enrollment(
                                readLong(scanner, "Student ID: "),
                                readLong(scanner, "Course ID: "),
                                LocalDate.now()));
                        case "10" -> updateGrade(scanner, dao);
                        case "11" -> dao.dropEnrollment(readLong(scanner, "Student ID: "),
                                readLong(scanner, "Course ID: "));
                        case "12" -> dao.listEnrollments();
                        case "13" -> dao.listStudentsInCourse(readText(scanner, "Course code: "));
                        case "14" -> dao.printCourseStatistics();
                        case "0" -> running = false;
                        default -> System.out.println("Choose one of the listed options.");
                    }
                } catch (SQLException | IllegalArgumentException exception) {
                    System.err.println("Operation failed: " + exception.getMessage());
                }
            }
            System.out.println("Goodbye.");
        }
    }

    private static void printMenu() {
        System.out.println("""

                1. Add student       2. List students    3. Update student email
                4. Delete student    5. Add course       6. List courses
                7. Update course     8. Delete course    9. Enroll student
                10. Set enrollment grade                 11. Drop enrollment
                12. Enrollment report (JOIN)              13. Students in course (subquery)
                14. Course statistics (aggregation)       0. Exit
                """);
        System.out.print("Option: ");
    }

    private static void addStudent(Scanner scanner, StudentManagementDao dao)
            throws SQLException {
        String number = readText(scanner, "Student number: ");
        String firstName = readText(scanner, "First name: ");
        String lastName = readText(scanner, "Last name: ");
        String email = readText(scanner, "Email: ");
        LocalDate dateOfBirth = LocalDate.parse(readText(scanner, "Date of birth (YYYY-MM-DD): "));
        dao.addStudent(new Student(number, firstName, lastName, email, dateOfBirth));
        System.out.println("Student added.");
    }

    private static void updateStudent(Scanner scanner, StudentManagementDao dao)
            throws SQLException {
        long id = readLong(scanner, "Student ID: ");
        dao.updateStudentEmail(id, readText(scanner, "New email: "));
        System.out.println("Student updated.");
    }

    private static void addCourse(Scanner scanner, StudentManagementDao dao)
            throws SQLException {
        String code = readText(scanner, "Course code: ");
        String title = readText(scanner, "Course title: ");
        int credits = Integer.parseInt(readText(scanner, "Credits (1-6): "));
        dao.addCourse(new Course(code, title, credits));
        System.out.println("Course added.");
    }

    private static void updateCourse(Scanner scanner, StudentManagementDao dao)
            throws SQLException {
        long id = readLong(scanner, "Course ID: ");
        String title = readText(scanner, "New title: ");
        int credits = Integer.parseInt(readText(scanner, "New credits (1-6): "));
        dao.updateCourse(id, title, credits);
        System.out.println("Course updated.");
    }

    private static void updateGrade(Scanner scanner, StudentManagementDao dao)
            throws SQLException {
        long studentId = readLong(scanner, "Student ID: ");
        long courseId = readLong(scanner, "Course ID: ");
        BigDecimal grade = new BigDecimal(readText(scanner, "Grade (0-100): "));
        dao.updateGrade(studentId, courseId, grade);
        System.out.println("Grade updated.");
    }

    private static long readLong(Scanner scanner, String prompt) {
        return Long.parseLong(readText(scanner, prompt));
    }

    private static String readText(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}
