CREATE DATABASE IF NOT EXISTS student_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE student_management;

CREATE TABLE IF NOT EXISTS students (
    student_id BIGINT NOT NULL AUTO_INCREMENT,
    student_number VARCHAR(20) NOT NULL,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    email VARCHAR(254) NOT NULL,
    date_of_birth DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (student_id),
    CONSTRAINT uq_students_number UNIQUE (student_number),
    CONSTRAINT uq_students_email UNIQUE (email),
    INDEX idx_students_name (last_name, first_name)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS courses (
    course_id BIGINT NOT NULL AUTO_INCREMENT,
    course_code VARCHAR(20) NOT NULL,
    title VARCHAR(160) NOT NULL,
    credits TINYINT UNSIGNED NOT NULL,
    PRIMARY KEY (course_id),
    CONSTRAINT uq_courses_code UNIQUE (course_code),
    CONSTRAINT chk_courses_credits CHECK (credits BETWEEN 1 AND 6),
    INDEX idx_courses_title (title)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS enrollments (
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    enrolled_on DATE NOT NULL,
    grade DECIMAL(5, 2) NULL,
    PRIMARY KEY (student_id, course_id),
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id)
        REFERENCES students (student_id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id)
        REFERENCES courses (course_id) ON DELETE RESTRICT,
    CONSTRAINT chk_enrollments_grade CHECK (grade IS NULL OR grade BETWEEN 0 AND 100),
    INDEX idx_enrollments_course (course_id),
    INDEX idx_enrollments_grade (grade)
) ENGINE = InnoDB;
