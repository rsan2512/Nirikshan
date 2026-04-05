package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Your PostgreSQL details
    private static final String URL      = "jdbc:postgresql://localhost:5432/pwqpvs_db";
    private static final String USER     = "postgres";
    private static final String PASSWORD = "Roshan25"; // ← change this

    // This method gives a fresh connection every time it is called
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}