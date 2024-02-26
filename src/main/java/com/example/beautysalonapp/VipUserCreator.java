package com.example.beautysalonapp;

class VipUserCreator extends UserCreator {
    @Override
    public User createUser(String name, double balance) {
        return new VipUser(name, balance);
    }
}
