package com.campuskart.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.campuskart.exception.DatabaseOperationException;
import com.campuskart.model.Admin;
import com.campuskart.model.Student;
import com.campuskart.model.User;

// ★ RUBRIC: JDBC CRUD (2M) — every method follows the same pattern:
//   Asg 21: Connection via DriverManager    Asg 22: PreparedStatement + ResultSet (parameterized SQL only)
//   Asg 19: SELECT                          Asg 20: INSERT / UPDATE
//   Asg 23: ResultSet navigation with next()
//   Asg 14/15: try / catch / finally — resources closed in finally, each in its own nested try-catch
public class UserDAO {

    // READ — login (feature F1)
    public User authenticate(String email, String password) throws DatabaseOperationException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(
                    "SELECT user_id, full_name, email, role, enrollment_no, verification_status "
                  + "FROM users WHERE email = ? AND password = ?");
            ps.setString(1, email);              // ★ Asg 22: bound parameter — never concatenated (no SQL injection)
            ps.setString(2, password);
            rs = ps.executeQuery();              // ★ Asg 19: SELECT
            if (rs.next()) {                     // ★ Asg 23: next() moves the cursor to a row
                return mapUser(rs);
            }
            return null;                         // no such login
        } catch (SQLException e) {
            throw new DatabaseOperationException("Login failed: " + e.getMessage(), e);
        } finally {                              // ★ Asg 15: finally ALWAYS runs
            try { if (rs != null) rs.close(); } catch (SQLException e) { }    // ★ Asg 14: nested try-catch
            try { if (ps != null) ps.close(); } catch (SQLException e) { }
            try { if (conn != null) conn.close(); } catch (SQLException e) { }
        }
    }

    // READ — used by the posting rule (only an APPROVED student may post)
    public User findById(int userId) throws DatabaseOperationException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(
                    "SELECT user_id, full_name, email, role, enrollment_no, verification_status "
                  + "FROM users WHERE user_id = ?");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not load user: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { }
            try { if (ps != null) ps.close(); } catch (SQLException e) { }
            try { if (conn != null) conn.close(); } catch (SQLException e) { }
        }
    }

    // CREATE — registration (feature F1). New students always start PENDING.
    public void register(Student student) throws DatabaseOperationException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(
                    "INSERT INTO users (full_name, email, password, role, enrollment_no, department, verification_status) "
                  + "VALUES (?, ?, ?, 'STUDENT', ?, ?, 'PENDING')");
            ps.setString(1, student.getFullName());
            ps.setString(2, student.getEmail());
            ps.setString(3, student.getPassword());
            ps.setString(4, student.getEnrollmentNo());
            ps.setString(5, student.getDepartment());
            ps.executeUpdate();                  // ★ Asg 20: INSERT (executeUpdate returns rows affected)
        } catch (SQLException e) {
            throw new DatabaseOperationException("Registration failed (email or enrollment may already exist): "
                    + e.getMessage(), e);
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) { }
            try { if (conn != null) conn.close(); } catch (SQLException e) { }
        }
    }

    // UPDATE — admin approves or rejects a student (feature F2)
    public void updateVerification(int userId, String status) throws DatabaseOperationException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(
                    "UPDATE users SET verification_status = ? WHERE user_id = ? AND role = 'STUDENT'");
            ps.setString(1, status);
            ps.setInt(2, userId);
            ps.executeUpdate();                  // ★ Asg 20: UPDATE — returns the number of rows changed
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not update verification: " + e.getMessage(), e);
        } finally {
            try { if (ps != null) ps.close(); } catch (SQLException e) { }
            try { if (conn != null) conn.close(); } catch (SQLException e) { }
        }
    }

    // READ — admin dashboard: every student still PENDING (feature F2)
    public ArrayList<User> listPendingStudents() throws DatabaseOperationException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<User> list = new ArrayList<User>();   // ★ Asg 5: ArrayList holds the query result
        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(
                    "SELECT user_id, full_name, email, role, enrollment_no, verification_status "
                  + "FROM users WHERE role = 'STUDENT' AND verification_status = 'PENDING' ORDER BY user_id");
            rs = ps.executeQuery();
            while (rs.next()) {                  // ★ Asg 23: walk the ResultSet row by row
                list.add(mapUser(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Could not list pending students: " + e.getMessage(), e);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { }
            try { if (ps != null) ps.close(); } catch (SQLException e) { }
            try { if (conn != null) conn.close(); } catch (SQLException e) { }
        }
        return list;
    }

    // one SQL row -> one object (★ Asg 7: the same method returns two different subclasses)
    private User mapUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("user_id");
        String name = rs.getString("full_name");
        String email = rs.getString("email");
        if ("ADMIN".equals(rs.getString("role"))) {
            return new Admin(id, name, email);
        }
        return new Student(id, name, email, rs.getString("enrollment_no"), rs.getString("verification_status"));
    }
}
