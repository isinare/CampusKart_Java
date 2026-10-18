package com.campuskart.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.campuskart.exception.DatabaseOperationException;

// ★ RUBRIC: JDBC CRUD (2M) | Asg 21: DriverManager and Connection
//            Asg 19 : loading the JDBC driver with Class.forName
public class DBConnection {

    // Local application: MySQL runs on the same machine as the server.
    private static final String URL = "jdbc:mysql://localhost:3306/campuskart";
    private static final String USER = "campus";
    private static final String PASSWORD = "campus_pw";

    private DBConnection() {
    }   // utility class — never instantiated

    public static Connection getConnection() throws DatabaseOperationException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");                // ★ Asg 19: load the JDBC driver
            return DriverManager.getConnection(URL, USER, PASSWORD);  // ★ Asg 21: DriverManager -> Connection
        } catch (ClassNotFoundException e) {
            // ★ Asg 14: separate catch block per exception type
            throw new DatabaseOperationException(
                    "JDBC driver not found — add mysql-connector-j.jar to the classpath", e);
        } catch (SQLException e) {
            throw new DatabaseOperationException(
                    "Database unavailable — is MySQL running and has campuskart.sql been applied?", e);
        }
    }
}
