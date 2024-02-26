package com.example.beautysalonapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

interface Prototype {
    Prototype clone();
}

class BeautyProcedure implements Prototype {
    private String name;
    private double price;
    private String description;

    public BeautyProcedure(String name, double price, String description) {
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public BeautyProcedure(BeautyProcedure procedure) {
        this.name = procedure.name;
        this.price = procedure.price;
        this.description = procedure.description;
    }

    @Override
    public BeautyProcedure clone() {
        return new BeautyProcedure(this);
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public void saveToDatabase(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO procedures (name, price, description) VALUES (?, ?, ?)")) {
            statement.setString(1, name);
            statement.setDouble(2, price);
            statement.setString(3, description);
            statement.executeUpdate();
        }
    }
}
