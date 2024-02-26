package com.example.beautysalonapp;

import java.sql.Connection;

class OrdinaryUserCreator extends UserCreator {
    private Connection connection;

    public OrdinaryUserCreator(Connection connection) {
        this.connection = connection;
    }

    @Override
    public User createUser(String name, double balance) {
        return new OrdinaryUser(name, balance, connection);
    }
}
