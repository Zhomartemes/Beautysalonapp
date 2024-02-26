package com.example.beautysalonapp;

import java.sql.Connection;
import java.sql.SQLException;

interface UserInterface {
    void saveToDatabase(Connection connection) throws SQLException;

    void handleBooking(String procedureName, Connection connection);
}
