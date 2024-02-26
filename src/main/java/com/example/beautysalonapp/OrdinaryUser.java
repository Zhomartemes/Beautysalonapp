package com.example.beautysalonapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class OrdinaryUser extends User implements UserInterface {
    private Connection connection;

    public OrdinaryUser(String name, double balance, Connection connection) {
        super(name, balance, UserType.ORDINARY);
        this.connection = connection;
    }

    @Override
    public void handleBooking(String procedureName, Connection connection) {
        double procedurePrice = getProcedurePrice(procedureName, connection);

        if (procedurePrice > 0 && getBalance(connection) >= procedurePrice) {
            getBookedProcedures().add(procedureName);
            setBalance(getBalance(connection) - procedurePrice, connection);
            System.out.println("Booking successful!");

            Booking booking = getBookingByProcedureName(procedureName, connection);

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

    @Override
    public void saveToDatabase(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO users (name, balance, user_type) VALUES (?, ?, ?) " +
                        "ON CONFLICT (name) DO UPDATE SET balance = EXCLUDED.balance")) {
            statement.setString(1, getName());
            statement.setDouble(2, getBalance(connection));
            statement.setString(3, getUserType().name());
            statement.executeUpdate();
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

    private Booking getBookingByProcedureName(String procedureName, Connection connection) {
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

    public void setBalance(double balance, Connection connection) {
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
