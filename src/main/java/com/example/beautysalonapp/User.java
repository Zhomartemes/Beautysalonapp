package com.example.beautysalonapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

class User {
    private String name;
    protected double balance;
    private ArrayList<String> bookedProcedures;
    private UserType userType;
    private Connection connection;

    public enum UserType {
        ORDINARY, VIP
    }

    public User(String name, double balance, UserType userType) {
        this.name = name;
        this.balance = balance;
        this.bookedProcedures = new ArrayList<>();
        this.userType = userType;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public ArrayList<String> getBookedProcedures() {
        return bookedProcedures;
    }

    public UserType getUserType() {
        return userType;
    }

    public void bookProcedure(String procedureName) {
        bookedProcedures.add(procedureName);
    }

    public boolean cancelProcedure(String procedureName) {
        return bookedProcedures.remove(procedureName);
    }

    public void saveToDatabase(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO users (name, balance, user_type) VALUES (?, ?, ?)")) {
            statement.setString(1, name);
            statement.setDouble(2, balance);
            statement.setString(3, userType.name());
            statement.executeUpdate();
        }
    }

    public void handleBooking(String procedureName, Connection connection) {
        double procedurePrice = getProcedurePrice(procedureName, connection);

        if (procedurePrice > 0 && getBalance() >= procedurePrice) {
            bookProcedure(procedureName);
            setBalance(getBalance() - procedurePrice);
            System.out.println("Booking successful!");

            Booking booking = getBookingByProcedureName(procedureName);

            if (booking != null) {
                printBookingDetails(booking);
            } else {
                System.out.println("Booking details not available.");
            }
        } else {
            if (procedurePrice <= 0) {
                System.out.println("Procedure not found.");
            } else {
                System.out.println("Insufficient balance. Please recharge your account.");
            }
        }
    }

    protected double getProcedurePrice(String procedureName, Connection connection) {
        for (BeautyProcedure procedure : BeautySalon.getProcedures(connection)) {
            if (procedure.getName().equals(procedureName)) {
                return procedure.getPrice();
            }
        }
        return 0.0;
    }

    private Booking getBookingByProcedureName(String procedureName) {
        for (Booking booking : BeautySalon.getBookingHistory(connection)) {
            if (booking.getProcedureName().equals(procedureName)) {
                return booking;
            }
        }
        return null;
    }

    private static void printBookingDetails(Booking booking) {
        System.out.println("Booking details:");
        System.out.println("ID: " + booking.getId());
        System.out.println("Procedure Name: " + booking.getProcedureName());
        System.out.println("Date: " + booking.getDate());
        System.out.println("Time: " + booking.getTime());
        System.out.println("User Name: " + booking.getUserName());
    }

    public void setBalance(double balance) {
        this.balance = balance;
        try {
            saveToDatabase(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getBalance(Connection connection) {
        return balance;
    }
}
