package com.example.beautysalonapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class Booking {
    private static int nextId = 1;
    private int id;
    private String procedureName;
    private String date;
    private String time;
    private String userName;

    public Booking(String procedureName, String date, String time, String userName) {
        this.id = nextId++;
        this.procedureName = procedureName;
        this.date = date;
        this.time = time;
        this.userName = userName;
    }

    public int getId() {
        return id;
    }

    public String getProcedureName() {
        return procedureName;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getUserName() {
        return userName;
    }

    public void saveToDatabase(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO bookings (procedure_name, date, time, user_name) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, procedureName);
            statement.setString(2, date);
            statement.setString(3, time);
            statement.setString(4, userName);
            statement.executeUpdate();
        }
    }
}
