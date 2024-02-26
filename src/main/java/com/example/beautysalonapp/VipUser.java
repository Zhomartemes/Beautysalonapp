package com.example.beautysalonapp;

import java.sql.Connection;

class VipUser extends User implements UserInterface {
    public VipUser(String name, double balance) {
        super(name, balance, UserType.VIP);
    }

    @Override
    public void handleBooking(String procedureName, Connection connection) {
        getBookedProcedures().add(procedureName);
        System.out.println("Booking successful!");
    }


}
