package com.example.beautysalonapp;

import java.sql.*;
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


class VipUser extends User {
    public VipUser(String name) {
        super(name, 0.0, UserType.VIP);
    }

    @Override
    public void handleBooking(String procedureName, Connection connection) {
        getBookedProcedures().add(procedureName);
        System.out.println("Booking successful!");
    }
}

class OrdinaryUser extends User {
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

class BeautySalon {
    private static final int PROCEDURE_DURATION_HOURS = 1;
    private static ArrayList<BeautyProcedure> procedures = new ArrayList<>();
    private static ArrayList<User> users = new ArrayList<>();
    private static ArrayList<Booking> bookingHistory = new ArrayList<>();

    public static void addProcedure(BeautyProcedure procedure, Connection connection) {
        procedures.add(procedure);
        try {
            procedure.saveToDatabase(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addUser(User user, Connection connection) {
        users.add(user);
        try {
            user.saveToDatabase(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<BeautyProcedure> getProcedures(Connection connection) {
        ArrayList<BeautyProcedure> procedures = new ArrayList<>();

        try (Statement statement = connection.createStatement()) {
            String query = "SELECT * FROM procedures";
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                double price = resultSet.getDouble("price");
                String description = resultSet.getString("description");

                BeautyProcedure procedure = new BeautyProcedure(name, price, description);
                procedures.add(procedure);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return procedures;
    }

    public static ArrayList<User> getUsers(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            String query = "SELECT * FROM users";
            ResultSet resultSet = statement.executeQuery(query);

            ArrayList<User> userList = new ArrayList<>();

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                double balance = resultSet.getDouble("balance");
                String userTypeStr = resultSet.getString("user_type");
                User.UserType userType = User.UserType.valueOf(userTypeStr);

                User user;
                if (userType == User.UserType.VIP) {
                    user = new VipUser(name);
                } else {
                    user = new OrdinaryUser(name, balance, connection);
                }
                userList.add(user);
            }

            return userList;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public static ArrayList<Booking> getBookingHistory(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            String query = "SELECT * FROM bookings";
            ResultSet resultSet = statement.executeQuery(query);

            ArrayList<Booking> bookingList = new ArrayList<>();

            while (resultSet.next()) {
                String procedureName = resultSet.getString("procedure_name");
                String date = resultSet.getString("date");
                String time = resultSet.getString("time");
                String userName = resultSet.getString("user_name");

                Booking booking = new Booking(procedureName, date, time, userName);
                bookingList.add(booking);
            }

            return bookingList;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void showProcedures(Connection connection) {
        ArrayList<BeautyProcedure> procedureList = getProcedures(connection);

        System.out.println("Beauty Procedures List:");
        System.out.printf("%-20s %-15s %-50s%n", "Name", "Price", "Description");

        for (BeautyProcedure procedure : procedureList) {
            System.out.printf("%-20s %-15s %-50s%n", procedure.getName(), procedure.getPrice(), procedure.getDescription());
        }
    }

    public static void showBookings(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            String query = "SELECT bookings.id, procedure_name, date, time, users.name AS user_name, users.balance " +
                    "FROM bookings " +
                    "JOIN users ON bookings.user_name = users.name";
            ResultSet resultSet = statement.executeQuery(query);

            System.out.println("Booking History:");
            System.out.printf("%-5s %-20s %-10s %-5s %-20s %-10s%n", "ID", "Procedure Name", "Date", "Time", "User Name", "User Balance");

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String procedureName = resultSet.getString("procedure_name");
                String date = resultSet.getString("date");
                String time = resultSet.getString("time");
                String userName = resultSet.getString("user_name");
                double userBalance = resultSet.getDouble("balance");

                System.out.printf("%-5d %-20s %-10s %-5s %-20s %-10s%n", id, procedureName, date, time, userName, userBalance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void cancelBooking(int bookingId, Connection connection) {
        Booking bookingToRemove = getBookingById(bookingId, connection);

        if (bookingToRemove != null) {
            String procedureName = bookingToRemove.getProcedureName();
            String userName = bookingToRemove.getUserName();

            try {
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM bookings WHERE id = ?")) {
                    statement.setInt(1, bookingId);
                    statement.executeUpdate();
                }

                bookingHistory.remove(bookingToRemove);
                System.out.println("Booking canceled successfully.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Booking not found for the specified ID.");
        }
    }

    private static Booking getBookingById(int bookingId, Connection connection) {
        for (Booking booking : bookingHistory) {
            if (booking.getId() == bookingId) {
                return booking;
            }
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM bookings WHERE id = ?")) {
            statement.setInt(1, bookingId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String procedureName = resultSet.getString("procedure_name");
                String date = resultSet.getString("date");
                String time = resultSet.getString("time");
                String userName = resultSet.getString("user_name");

                return new Booking(procedureName, date, time, userName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    public static void bookProcedure(User user, BeautyProcedure procedure, String date, String time, Connection connection) {
        if (!isBookingConflict(procedure, date, time)) {
            if (user.getUserType() == User.UserType.VIP) {
                ((VipUser) user).bookProcedure(procedure.getName());
            } else {
                ((OrdinaryUser) user).bookProcedure(procedure.getName());
            }

            Booking newBooking = new Booking(procedure.getName(), date, time, user.getName());
            bookingHistory.add(newBooking);

            try {
                newBooking.saveToDatabase(connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            printBookingDetails(newBooking);
        }
    }

    private static void printBookingDetails(Booking booking) {
        System.out.println("Booking details:");
        System.out.println("ID: " + booking.getId());
        System.out.println("Procedure Name: " + booking.getProcedureName());
        System.out.println("Date: " + booking.getDate());
        System.out.println("Time: " + booking.getTime());
        System.out.println("User Name: " + booking.getUserName());
    }

    private static boolean isBookingConflict(BeautyProcedure procedure, String date, String time) {
        for (Booking booking : bookingHistory) {
            if (booking.getProcedureName().equals(procedure.getName()) &&
                    booking.getDate().equals(date) &&
                    isTimeConflict(booking.getTime(), time)) {
                System.out.println("Booking conflict! The procedure is already booked at that time. " +
                        "Try booking an hour later or choose another date.");
                return true;
            }
        }
        return false;
    }

    private static boolean isTimeConflict(String bookedTime, String newTime) {
        String[] bookedTimeParts = bookedTime.split(":");
        String[] newTimeParts = newTime.split(":");
        int bookedHour = Integer.parseInt(bookedTimeParts[0]);
        int bookedMinute = Integer.parseInt(bookedTimeParts[1]);
        int newHour = Integer.parseInt(newTimeParts[0]);
        int newMinute = Integer.parseInt(newTimeParts[1]);

        int bookedTimeInMinutes = bookedHour * 60 + bookedMinute;
        int newTimeInMinutes = newHour * 60 + newMinute;

        return Math.abs(bookedTimeInMinutes - newTimeInMinutes) < PROCEDURE_DURATION_HOURS * 60;
    }
    public static void updateProcedurePrice(String procedureName, double newPrice, Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE procedures SET price = ? WHERE name = ?")) {
            statement.setDouble(1, newPrice);
            statement.setString(2, procedureName);
            statement.executeUpdate();
            System.out.println("Procedure updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateUserName(String oldName, String newName, Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE users SET name = ? WHERE name = ?")) {
            statement.setString(1, newName);
            statement.setString(2, oldName);
            statement.executeUpdate();
            System.out.println("User updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateUserBalance(String userName, double newBalance, Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE users SET balance = ? WHERE name = ?")) {
            statement.setDouble(1, newBalance);
            statement.setString(2, userName);
            statement.executeUpdate();
            System.out.println("User balance updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateBookingDateAndTime(int bookingId, String newDate, String newTime, Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE bookings SET date = ?, time = ? WHERE id = ?")) {
            statement.setString(1, newDate);
            statement.setString(2, newTime);
            statement.setInt(3, bookingId);
            statement.executeUpdate();
            System.out.println("Booking date and time updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/bd", "postgres", "1234");
            createTables(connection);

            BeautySalon beautySalon = new BeautySalon();

            Scanner scanner = new Scanner(System.in);

            int choice;
            do {
                System.out.println("Hello! You have the following available functions:");
                System.out.println("1) To show beauty procedures list;");
                System.out.println("2) To add a beauty procedure;");
                System.out.println("3) To add a new user;");
                System.out.println("4) To book for a beauty procedure;");
                System.out.println("5) To cancel booking for a beauty procedure;");
                System.out.println("6) To show booking history;");
                System.out.println("7) To update a procedure;");
                System.out.println("8) To update a user;");
                System.out.println("9) To update booking date and time;");
                System.out.println("0) Exit");

                System.out.print("Enter your choice: ");
                while (!scanner.hasNextInt()) {
                    System.out.println("Invalid input. Please enter a number.");
                    scanner.next();
                }
                choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        beautySalon.showProcedures(connection);
                        break;
                    case 2:
                        System.out.print("Enter procedure name: ");
                        String newProcedureName = scanner.next();
                        System.out.print("Enter procedure price: ");
                        double newProcedurePrice = scanner.nextDouble();
                        System.out.print("Enter procedure description: ");
                        String newProcedureDesc = scanner.next();

                        BeautyProcedure newProcedure = new BeautyProcedure(newProcedureName, newProcedurePrice, newProcedureDesc);
                        BeautySalon.addProcedure(newProcedure, connection);
                        break;
                    case 3:
                        System.out.print("Enter user name: ");
                        String newUserName = scanner.next();
                        System.out.print("Enter user balance: ");
                        double newUserBalance = scanner.nextDouble();
                        System.out.print("Enter user type (1 for VIP, 2 for ORDINARY): ");
                        User.UserType newUserType = scanner.nextInt() == 1 ? User.UserType.VIP : User.UserType.ORDINARY;

                        User newUser;
                        if (newUserType == User.UserType.VIP) {
                            newUser = new VipUser(newUserName);
                        } else {
                            newUser = new OrdinaryUser(newUserName, newUserBalance, connection);
                        }

                        BeautySalon.addUser(newUser, connection);
                        break;
                    case 4:
                        System.out.print("Enter user name: ");
                        String userName = scanner.next();
                        User user = null;
                        for (User u : BeautySalon.getUsers(connection)) {
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
                        for (BeautyProcedure p : BeautySalon.getProcedures(connection)) {
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

                        BeautySalon.bookProcedure(user, selectedProcedure, date, time, connection);
                        break;
                    case 5:
                        System.out.print("Enter booking ID: ");
                        int cancelBookingId = scanner.nextInt();
                        BeautySalon.cancelBooking(cancelBookingId, connection);
                        break;
                    case 6:
                        beautySalon.showBookings(connection);
                        break;
                    case 7:
                        System.out.print("Enter procedure name to update: ");
                        String procedureToUpdate = scanner.next();
                        System.out.print("Enter new price: ");
                        double updatedProcedurePrice = scanner.nextDouble();
                        updateProcedurePrice(procedureToUpdate, updatedProcedurePrice, connection);
                        break;


                    case 8:
                        System.out.print("Enter user name to update: ");
                        String userToUpdate = scanner.next();
                        System.out.print("Enter new user name: ");
                        String updatedUserName = scanner.next();
                        updateUserName(userToUpdate, updatedUserName, connection);
                        System.out.print("Do you want to update the balance? (Y/N): ");
                        String updateBalanceChoice = scanner.next();
                        if (updateBalanceChoice.equalsIgnoreCase("Y")) {
                            System.out.print("Enter new balance: ");
                            double newBalance = scanner.nextDouble();
                            updateUserBalance(updatedUserName, newBalance, connection);
                        }
                        break;
                    case 9:
                        System.out.print("Enter booking ID to update: ");
                        int bookingIdToUpdate = scanner.nextInt();
                        System.out.print("Enter new date (dd/mm/yyyy): ");
                        String newDate = scanner.next();
                        System.out.print("Enter new time (hh:mm): ");
                        String newTime = scanner.next();
                        updateBookingDateAndTime(bookingIdToUpdate, newDate, newTime, connection);
                        break;

                    case 0:
                        System.out.println("Exiting...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } while (choice != 0);

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String createProceduresTableQuery = "CREATE TABLE IF NOT EXISTS procedures ("
                    + "name VARCHAR(50) PRIMARY KEY,"
                    + "price DOUBLE PRECISION NOT NULL,"
                    + "description VARCHAR(255) NOT NULL)";

            String createUsersTableQuery = "CREATE TABLE IF NOT EXISTS users ("
                    + "name VARCHAR(50) PRIMARY KEY,"
                    + "balance DOUBLE PRECISION NOT NULL,"
                    + "user_type VARCHAR(10) NOT NULL)";

            String createBookingsTableQuery = "CREATE TABLE IF NOT EXISTS bookings ("
                    + "id SERIAL PRIMARY KEY,"
                    + "procedure_name VARCHAR(50) NOT NULL,"
                    + "date VARCHAR(10) NOT NULL,"
                    + "time VARCHAR(5) NOT NULL,"
                    + "user_name VARCHAR(50) NOT NULL)";

            statement.executeUpdate(createProceduresTableQuery);
            statement.executeUpdate(createUsersTableQuery);
            statement.executeUpdate(createBookingsTableQuery);
        }
    }
}
