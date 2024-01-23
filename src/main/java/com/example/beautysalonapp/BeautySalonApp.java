package com.example.beautysalonapp;

import java.util.ArrayList;
import java.util.Scanner;

class BeautyProcedure {
    private String name;
    private double price;
    private String description;

    public BeautyProcedure(String name, double price, String description) {
        this.name = name;
        this.price = price;
        this.description = description;
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
}

class User {
    private String name;
    private double balance;
    public ArrayList<String> bookedProcedures;
    private UserType userType;

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



    public UserType getUserType() {
        return userType;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void bookProcedure(String procedureName) {
        bookedProcedures.add(procedureName);
    }

    public void cancelProcedure(String procedureName) {
        bookedProcedures.remove(procedureName);
    }
}

class VIP extends User {
    public VIP(String name, double balance) {
        super(name, balance, UserType.VIP);
    }

    public void bookProcedure(String procedureName) {
        bookedProcedures.add(procedureName);
    }
}

class Ordinary extends User {
    public Ordinary(String name, double balance) {
        super(name, balance, UserType.ORDINARY);
    }
    // @Override
    public void bookProcedure(BeautyProcedure procedure) {
        bookedProcedures.add(procedure.getName());
        if (getBalance() >= procedure.getPrice()) {
            setBalance(getBalance() - procedure.getPrice());
            System.out.println("Booking successful! Balance: $" + getBalance());
        } else {
            System.out.println("Insufficient balance. Please recharge your account.");
        }
    }

}

class Booking {
    private static int nextId = 1;
    private int id;
    private String procedureName;
    private String date;
    private String time;

    public Booking(String procedureName, String date, String time) {
        this.id = nextId++;
        this.procedureName = procedureName;
        this.date = date;
        this.time = time;
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
}

class BeautySalon {
    private static final int PROCEDURE_DURATION_HOURS = 1;
    private ArrayList<BeautyProcedure> procedures;
    private ArrayList<User> users;
    private ArrayList<Booking> bookingHistory;

    public BeautySalon() {
        this.procedures = new ArrayList<>();
        this.users = new ArrayList<>();
        this.bookingHistory = new ArrayList<>();
    }

    public void addProcedure(BeautyProcedure procedure) {
        procedures.add(procedure);
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void showProcedures() {
        for (BeautyProcedure procedure : procedures) {
            System.out.println("Name: " + procedure.getName() + ", Price: $" + procedure.getPrice() +
                    ", Description: " + procedure.getDescription());
        }
    }

    public void bookProcedure(User user, BeautyProcedure procedure, String date, String time) {
        if (user.getUserType() == User.UserType.VIP) {
            ((VIP) user).bookProcedure(procedure.getName());
            bookingHistory.add(new Booking(procedure.getName(), date, time));
            System.out.println("Booking successful! Balance: $" + user.getBalance());
        } else if (user.getUserType() == User.UserType.ORDINARY) {
            if (!isBookingConflict(procedure, date, time)) {
                ((Ordinary) user).bookProcedure(procedure.getName());
                bookingHistory.add(new Booking(procedure.getName(), date, time));
                user.setBalance(user.getBalance() - procedure.getPrice());
                System.out.println("Booking successful! Balance: $" + user.getBalance());
            } else {
                System.out.println("Invalid date, the procedure has been already booked. " +
                        "You may book for the next hour or day.");
            }
        } else {
            System.out.println("Invalid user type.");
        }
    }

    private boolean isBookingConflict(BeautyProcedure procedure, String date, String time) {
        for (Booking booking : bookingHistory) {
            if (booking.getProcedureName().equals(procedure.getName()) &&
                    booking.getDate().equals(date) &&
                    isTimeConflict(booking.getTime(), time)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTimeConflict(String bookedTime, String newTime) {
        String[] bookedTimeParts = bookedTime.split(":");
        String[] newTimeParts = newTime.split(":");
        int bookedHour = Integer.parseInt(bookedTimeParts[0]);
        int bookedMinute = Integer.parseInt(bookedTimeParts[1]);
        int newHour = Integer.parseInt(newTimeParts[0]);
        int newMinute = Integer.parseInt(newTimeParts[1]);
        int bookedSeconds = bookedHour * 3600 + bookedMinute * 60;
        int newSeconds = newHour * 3600 + newMinute * 60;

        return Math.abs(bookedSeconds - newSeconds) < 3600;
    }

    public void cancelBooking(User user, BeautyProcedure procedure) {
        user.cancelProcedure(procedure.getName());
        System.out.println("Booking canceled successfully.");
    }

    public ArrayList<BeautyProcedure> getProcedures() {
        return procedures;
    }

    public ArrayList<User> getUsers() {
        return users;
    }
}

public class BeautySalonApp {
    public static void main(String[] args) {
        BeautySalon beautySalon = new BeautySalon();

        BeautyProcedure vip = new BeautyProcedure("VIP", 20, "Hair cuts ");
        BeautyProcedure ordinary = new BeautyProcedure("Ordinary", 10, "Bald Haircut");

        beautySalon.addProcedure(vip);
        beautySalon.addProcedure(ordinary);

        User vipUser = new User("VIP Kazashka", 0.0, User.UserType.VIP);
        User ordinaryUser = new User("Ordinary Kazashka", 0, User.UserType.ORDINARY);

        beautySalon.addUser(vipUser);
        beautySalon.addUser(ordinaryUser);


        Scanner scanner = new Scanner(System.in);

        int choice;
        do {
            System.out.println("Hello! You have the following available functions:");
            System.out.println("1) To show beauty procedures list;");
            System.out.println("2) To add a beauty procedure;");
            System.out.println("3) To add a new user;");
            System.out.println("4) To book for a beauty procedure;");
            System.out.println("5) To cancel booking for a beauty procedure;");
            System.out.println("0) Exit");

            System.out.print("Enter your choice: ");
            while (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next();
            }
            choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    beautySalon.showProcedures();
                    break;
                case 2:
                    System.out.print("Enter procedure name: ");
                    String newProcedureName = scanner.next();
                    System.out.print("Enter procedure price: ");
                    double newProcedurePrice = scanner.nextDouble();
                    System.out.print("Enter procedure description: ");
                    String newProcedureDesc = scanner.next();

                    BeautyProcedure newProcedure = new BeautyProcedure(newProcedureName, newProcedurePrice, newProcedureDesc);
                    beautySalon.addProcedure(newProcedure);
                    break;
                case 3:
                    System.out.print("Enter user name: ");
                    String newUserName = scanner.next();
                    System.out.print("Enter user balance: ");
                    double newUserBalance = scanner.nextDouble();
                    System.out.print("Enter user type (1 for VIP, 2 for ORDINARY): ");
                    User.UserType newUserType = scanner.nextInt() == 1 ? User.UserType.VIP : User.UserType.ORDINARY;

                    User newUser = newUserType == User.UserType.VIP ? new VIP(newUserName, newUserBalance) : new Ordinary(newUserName, newUserBalance);
                    beautySalon.addUser(newUser);
                    break;
                case 4:
                    System.out.print("Enter user name: ");
                    String userName = scanner.next();
                    User user = null;
                    for (User u : beautySalon.getUsers()) {
                        if (u.getName().equals(userName)) {
                            user = u;
                            break;
                        }
                    }

                    if (user == null) {
                        System.out.println("User not found!");
                        break;
                    }

                    System.out.print("Enter procedure name: ");
                    String procedureName = scanner.next();
                    BeautyProcedure selectedProcedure = null;
                    for (BeautyProcedure p : beautySalon.getProcedures()) {
                        if (p.getName().equals(procedureName)) {
                            selectedProcedure = p;
                            break;
                        }
                    }

                    if (selectedProcedure == null) {
                        System.out.println("Procedure not found!");
                        break;
                    }

                    System.out.print("Enter date (dd/mm/yyyy): ");
                    String date = scanner.next();
                    System.out.print("Enter time (hh:mm): ");
                    String time = scanner.next();

                    beautySalon.bookProcedure(user, selectedProcedure, date, time);
                    break;
                case 5:
                    System.out.print("Enter user name: ");
                    String cancelUserName = scanner.next();
                    User cancelUser = null;
                    for (User u : beautySalon.getUsers()) {
                        if (u.getName().equals(cancelUserName)) {
                            cancelUser = u;
                            break;
                        }
                    }

                    if (cancelUser == null) {
                        System.out.println("User not found!");
                        break;
                    }

                    System.out.print("Enter procedure name: ");
                    String cancelProcedureName = scanner.next();
                    BeautyProcedure cancelProcedure = null;
                    for (BeautyProcedure p : beautySalon.getProcedures()) {
                        if (p.getName().equals(cancelProcedureName)) {
                            cancelProcedure = p;
                            break;
                        }
                    }

                    if (cancelProcedure == null) {
                        System.out.println("Procedure not found!");
                        break;
                    }

                    beautySalon.cancelBooking(cancelUser, cancelProcedure);
                    break;
                case 0:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 0);
    }
}
