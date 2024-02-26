package com.example.beautysalonapp;

import java.sql.Connection;
import java.sql.SQLException;

abstract class UserCreator {
    public abstract User createUser(String name, double balance);

    public void saveUserToDatabase(User user, Connection connection) throws SQLException {
        user.saveToDatabase(connection);
    }
}
