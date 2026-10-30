# Student Management JDBC

A Java 17 console application backed by MySQL 8. The schema keeps students and courses
in separate tables and models their many-to-many relationship with `enrollments`.
Enrollment grades belong to the student-course relationship rather than either entity.

Java classes are separated into `app`, `config`, `dao`, and `model` packages under
`com.example.studentmanagement`.

## Entities and relationships

- A student can enroll in many courses, and a course can have many students.
- `students` to `enrollments` and `courses` to `enrollments` are one-to-many
  relationships; the composite enrollment key prevents duplicate enrollment.
- The tables are normalized: student details, course details, and enrollment-specific
  data are stored separately. Non-key columns depend on the key of their own table.

## Database setup

1. Start MySQL Server 8.
2. From PowerShell, create the database and tables:

   ```powershell
   Get-Content database\schema.sql | mysql -u root -p
   ```

   Enter your MySQL root password when prompted. The schema is also available at
   [database/schema.sql](database/schema.sql).
3. Configure the connection in PowerShell. For the credentials supplied for this
   setup, set the password to `root`:

   ```powershell
   $env:MYSQL_USER = "root"
   $env:MYSQL_PASSWORD = "root"
   ```

   Optional settings are `MYSQL_HOST` (default `localhost`), `MYSQL_PORT`
   (default `3306`), and `MYSQL_DATABASE` (default `student_management`).
   The password is read from the environment and is not stored in source code.
4. Run the application from the project directory:

   ```powershell
   cd C:\InternTasks\StudentManagementJDBC1
   mvn clean compile exec:java
   ```

   Run this Maven command from the project root, not from the `app` source folder.
   Do not use the single-file `javac StudentManagementApp.java` command: the app
   depends on classes in other packages and the MySQL JDBC driver.

## Tests

The JUnit integration test uses the configured MySQL database and removes its
temporary records after running. In PowerShell, set the connection settings in the
same terminal before running Maven:

```powershell
$env:MYSQL_USER = "root"
$env:MYSQL_PASSWORD = "root"
mvn test
```

## Screenshot guide for assignment proof

Use the screenshots below as evidence of the completed task. Each screenshot should be
clear and should show only the relevant output. The following order is recommended.

### 1) Test case proof

Run the following commands:

```powershell
cd C:\InternTasks\StudentManagementJDBC1
$env:MYSQL_USER = "root"
$env:MYSQL_PASSWORD = "root"
mvn test
```

Take a screenshot of the terminal output showing:

- `Running com.example.studentmanagement.dao.StudentManagementDaoIntegrationTest`
- `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`
- `BUILD SUCCESS`

This proves that the MySQL-backed integration test passed successfully.

### 2) Java application startup proof

Run:

```powershell
cd C:\InternTasks\StudentManagementJDBC1
$env:MYSQL_USER = "root"
$env:MYSQL_PASSWORD = "root"
mvn clean compile exec:java
```

Take a screenshot of the Java menu that appears after startup. The screenshot should clearly
show the menu with the following options:

- 1. Add student
- 2. List students
- 5. Add course
- 9. Enroll student
- 12. Enrollment report (JOIN)
- 13. Students in course (subquery)
- 14. Course statistics (aggregation)

This proves that the Java application launches correctly and is connected to the database.

### 3) CRUD and menu operation proof

Use the running app and perform a quick flow:

1. Choose `1` and add a student.
2. Choose `2` to list students and verify the student appears.
3. Choose `5` to add a course.
4. Choose `9` to enroll the student in the course.
5. Choose `12` to run the JOIN-based enrollment report.
6. Choose `13` to run the subquery-based student lookup.
7. Choose `14` to run the aggregation-based course statistics report.

Take one screenshot that includes the results of these actions together. This screenshot should display:

- Student added
- Course added
- Student appears in list
- Enrollment report output
- Students in course output
- Course statistics output

This proves create, read, and query functionality in the Java application.

### 4) Schema screenshot proof

Open [database/schema.sql](database/schema.sql) and take a screenshot of the file content.
The screenshot must clearly show:

- `students` table
- `courses` table
- `enrollments` table
- primary keys
- foreign keys
- unique constraints
- check constraints
- indexes

This proves the normalized relational database design and the implementation of keys and constraints.

### 5) Optional update proof

If needed, you may also capture a screenshot for update operations:

- Choose `3` to update a student email.
- Choose `10` to set an enrollment grade.

These screenshots are optional extra evidence, but they clearly show update behavior in the application.

## Database design and features

- `students`: unique student number and email, required names and date of birth.
- `courses`: unique course code and a 1–6 credit constraint.
- `enrollments`: composite student/course primary key, enrollment date, optional
  0–100 grade, cascading student deletion, and restricted course deletion.
- Indexes support name/title lookups and enrollment course/grade queries.
- The menu supports create, read, update, and delete operations for students,
  courses, and enrollments.
- The enrollment report joins the three tables; the course lookup uses a subquery;
  course statistics use `COUNT` and `AVG` with a `LEFT JOIN` so empty courses remain
  visible.
- All user-provided values are passed through JDBC prepared statements. Enrollment
  validates both records and inserts within an explicit transaction, rolling back
  if any step fails.
