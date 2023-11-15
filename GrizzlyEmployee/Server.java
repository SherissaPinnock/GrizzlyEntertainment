
package controller;

import java.io.EOFException;

import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.persistence.Query;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.mysql.cj.jdbc.result.ResultSetMetaData;

import model.Customer;
import model.Equipment;
import model.Message;
import model.Transaction;

public class Server {
    private ServerSocket serverSocket;
    private static Connection conn = null;
    private int clientCount;
    private static SessionFactory factory;
    private static final Logger logger = LogManager.getLogger(Server.class);

    public Server() {
        this.createConnection();
        this.waitForRequests();
    }

    // Private method to create a connection
    private void createConnection() {
        // Create new instance of the ServerSocket listening on port 8888
        try {
            serverSocket = new ServerSocket(3308);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Connection getDatabaseConnection() {
        if (conn == null) {
            try {
                // Class.forName("com.mysql.cj.jdbc.Driver");
                String url = "jdbc:mysql://localhost:3306/grizzlycustomers";
                conn = DriverManager.getConnection(url, "root", "usbw");

                JOptionPane.showMessageDialog(null, "DB Connection Established",
                        "CONNECTION STATUS", JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Could not connect to database\n" + ex,
                        "Connection Failure", JOptionPane.ERROR_MESSAGE);
            }

        }
        return conn;
    }

    public static boolean registerCustomer(Customer cust) {
        try {
            String sql = "INSERT INTO grizzlycustomers.customers (customerID, firstName, lastName, customerPassword, email, phone, accountBalance, streetName, parish) "
                    +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, cust.getCustomerID());
            statement.setString(2, cust.getFirstName());
            statement.setString(3, cust.getLastName());
            statement.setString(4, cust.getCustomerpassword());
            statement.setString(5, cust.getEmail());
            statement.setString(6, cust.getPhone());
            statement.setDouble(7, cust.getAccountBalance());
            statement.setString(8, cust.getStreetName());
            statement.setString(9, cust.getParish());

            int inserted = statement.executeUpdate();

            if (inserted == 1) {
                // GetGeneratedKeys() is used to take the auto incremented customer ID
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int customerID = generatedKeys.getInt(1);
                    System.out.println("Registered successfully! Your new customer ID is: " + customerID);
                    JOptionPane.showMessageDialog(null,
                            "Registered successfully! Your new customer ID is: " + customerID);

                    // Create a Customer object and set the customerID
                    cust.setCustomerID(customerID);
                    return true;
                } else {
                    System.err.println("Failed to retrieve the generated customer ID.");
                }
            } else {
                System.err.println("Registration failed.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("SQL Error Message: " + e.getMessage());
        }
        return false;
    }

    // This method checks to see if the customer's password and id match
    public static boolean validateCustomerLogin(String id, String password) {
        try {
            String query = "SELECT * FROM customers WHERE customerID = ? AND customerPassword = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                System.out.println("Login successful");
            } else {
                System.out.println("Login failed");
                return false;
            }
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // This method retrieves customer details by ID
    public static Customer getCustomerDetails(String customerID) {
        try {
            String query = "SELECT * FROM customers WHERE customerID = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, customerID);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Customer customer = new Customer();
                // Populate the customer object with data from the ResultSet
                customer.setCustomerID(resultSet.getInt("customerID"));
                customer.setFirstName(resultSet.getString("firstName"));
                customer.setLastName(resultSet.getString("lastName"));
                customer.setCustomerpassword(resultSet.getString("customerPassword"));
                customer.setEmail(resultSet.getString("email"));
                customer.setPhone(resultSet.getString("phone"));
                customer.setAccountBalance(resultSet.getDouble("accountBalance"));

                return customer;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if customer details are not found
    }

    public static List<Transaction> retrieveTransactionInfo(int customerID /* JTable table */) {
        List<Transaction> transactions = new ArrayList<>();

        try {
            String sql = "SELECT rentalID, startdate, enddate, equipmentID, amount_paid FROM rentals WHERE customerID = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, customerID);

            ResultSet resultSet = preparedStatement.executeQuery();
            ResultSetMetaData rsmd = (ResultSetMetaData) resultSet.getMetaData();

            DefaultTableModel model = new DefaultTableModel();
            // table.setModel(model);

            int cols = rsmd.getColumnCount();
            String[] colName = new String[cols];
            for (int i = 0; i < cols; i++) {
                colName[i] = rsmd.getColumnName(i + 1);
                model.setColumnIdentifiers(colName);

            }
            while (resultSet.next()) {
                int rentalID = resultSet.getInt("rentalID");
                Date startDate = resultSet.getDate("startdate");
                Date endDate = resultSet.getDate("enddate");
                int equipmentID = resultSet.getInt("equipmentID");
                double amountPaid = resultSet.getDouble("amount_paid");

                System.out.println("Rental ID" + rentalID
                        + "startDate: " + startDate +
                        " | endDate: " + endDate +
                        " | equipmentID: " + equipmentID +
                        " | amountPaid: " + amountPaid);

                Transaction transaction = new Transaction(rentalID, startDate, endDate, equipmentID, amountPaid);
                // Adds the data to the list
                System.out.println(transaction);
                transactions.add(transaction);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    // This method implements the functionality: Customers should be able to view a
    // single Transaction
    public static List<Transaction> retrieveSingleTransaction(int rentalID) {
        List<Transaction> transactions = new ArrayList<>();
        try {

            String sql = "SELECT rentalID, startdate, enddate, equipmentID, amount_paid FROM rentals WHERE rentalID = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            // preparedStatement.setInt(1, customerID);
            preparedStatement.setInt(1, rentalID);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                rentalID = resultSet.getInt("rentalID");
                Date startDate = resultSet.getDate("startdate");
                Date endDate = resultSet.getDate("enddate");
                int equipmentID = resultSet.getInt("equipmentID");
                double amountPaid = resultSet.getDouble("amount_paid");
                Transaction transaction = new Transaction(rentalID, startDate, endDate, equipmentID, amountPaid);
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // System.out.println("Customer ID: " + customerID); // Print the customerID
        System.out.println("Rental ID: " + rentalID); // Print the rentalID

        return transactions;
    }

    // Method that allows customers to leave messages on the system
    public static void insertMessage(Message message, Customer cust) {
        try {
            String sql = "INSERT INTO grizzlycustomers.messages(customerID, firstName, lastName, message)"
                    + "VALUES('" + cust.getCustomerID() + "', '" + message.getFirstName() + "', '"
                    + message.getLastName()
                    + "', '" + message.getMessage() + "')";

            Statement stat = conn.createStatement();
            int rowsAffected = stat.executeUpdate(sql);

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null,
                        "Message entered successfully, We will respond to your inquiry shortly");
            } else
                JOptionPane.showMessageDialog(null, "No record inserted");

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private boolean performAvailabilityCheck(int equipmentID, Date startDate, Date endDate) {

        String sql = "SELECT COUNT(*) FROM rentals WHERE equipmentID = ? AND (? <= enddate) AND (? >= startdate)";

        try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            // Set parameters
            preparedStatement.setInt(1, equipmentID); // Replace 'yourEquipmentId' with the actual equipment ID
            preparedStatement.setDate(2, new java.sql.Date(endDate.getTime()));
            preparedStatement.setDate(3, new java.sql.Date(startDate.getTime()));

            // Execute the query
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count == 0; // Equipment is available if no overlapping entries found
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return false in case of an exception or if no result is found
        return false;
    }

    public static List<Transaction> retrieveMessage(int messageID) {
        List<Transaction> transactions = new ArrayList<>();

        try {
            String sql = "SELECT messageID, customerID, firstName, lastName, message FROM messages WHERE messageID = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, messageID);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int retrievedMessageID = resultSet.getInt("messageID");
                int customerID = resultSet.getInt("customerID");
                String firstName = resultSet.getString("firstName");
                String lastName = resultSet.getString("lastName");
                String message = resultSet.getString("message");

                System.out.println("Message ID: " + retrievedMessageID +
                        " | Customer ID: " + customerID +
                        " | First Name: " + firstName +
                        " | Last Name: " + lastName +
                        " | Message: " + message);

                Transaction transaction = new Transaction(retrievedMessageID /* other parameters needed */);
                transactions.add(transaction);
            }

            conn.close(); // Close the connection
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exceptions here
        }

        return transactions;
    }

    public static void deleteRecord(int id) {
        try {
            String sql = "Delete from customers WHERE customerID=?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, id);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "Account deleted. Goodbye");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to Delete");
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public boolean scheduleEquipment(int customerID, int equipmentID, int employeeID, Date startDate, Date endDate,
            String eventname) {
        try {
            String sql = "INSERT INTO equipment_schedule (customerID, equipmentID, employeeID, startDate, endDate, eventname) VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, customerID);
            statement.setInt(2, equipmentID);
            statement.setInt(3, employeeID);

            // Assuming startDate and endDate are java.util.Date
            java.sql.Date sqlStartDate = new java.sql.Date(startDate.getTime());
            java.sql.Date sqlEndDate = new java.sql.Date(endDate.getTime());

            statement.setDate(4, sqlStartDate);
            statement.setDate(5, sqlEndDate);
            statement.setString(6, eventname);

            int inserted = statement.executeUpdate();

            if (inserted == 1) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int scheduleID = generatedKeys.getInt(1);
                    System.out.println("Equipment scheduled successfully! Schedule ID: " + scheduleID);
                    JOptionPane.showMessageDialog(null, "Equipment scheduled successfully! Schedule ID: " + scheduleID);
                    return true;
                } else {
                    System.err.println("Failed to retrieve the generated schedule ID.");
                }
            } else {
                System.err.println("Equipment scheduling failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("SQL Error Message: " + e.getMessage());
        }
        return false;
    }

    // -----------HIBERNATE--------------
    // the code below will be used to create hibernate sessions so that an equipment
    // can be entered and retrived from the database
    static {
        factory = new Configuration().configure("hibernate.cfg.xml").addAnnotatedClass(Equipment.class)
                .buildSessionFactory();
    }

    // this will insert the customerID and equipment attributes into the database
    public static void insertEquipment(Equipment equip) {
        Session session = factory.getCurrentSession();

        org.hibernate.Transaction transaction = session.beginTransaction();
        session.save(equip); // saves the data into the database
        try {
            ((Connection) transaction).commit();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        session.close();
    }

    // this will retrieve the equipment data and place it in the JTable based on the
    // condition
    public static List<Equipment> retrieveEquipment(String availabilityStatus, String equipmentCategory) {
        Session session = factory.getCurrentSession();
        List<Equipment> equipments = new ArrayList<>();
        try {
            session.beginTransaction();
            String hql = "FROM Equipment e WHERE e.availabilityStatus = :status AND e.equipmentCategory = :category";
            Query query = session.createQuery(hql);
            query.setParameter("status", availabilityStatus);
            query.setParameter("category", equipmentCategory);

            equipments = query.getResultList();// this will store the hql in the equipment object

            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return equipments;
    }

    /*
     * public static List<Equipment> getEquipment(int equipmentID)
     * {
     * Session session = factory.getCurrentSession();
     * List<Equipment> equipments = new ArrayList<>();
     * try {
     * session.beginTransaction();
     * String hql = "FROM Equipment e WHERE e.equipmentID = :equip";
     * Query query = session.createQuery(hql);
     * query.setParameter("equip", equipmentID);
     * 
     * equipments = query.getResultList();// this will store the hql in the
     * equipment object
     * 
     * session.getTransaction().commit();
     * } catch (Exception e) {
     * e.printStackTrace();
     * } finally {
     * session.close();
     * }
     * return equipments;
     * }
     */

    public static Equipment getEquipment(int equipmentID) {
        try {
            String query = "SELECT * FROM equipment WHERE equipmentID = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setInt(1, equipmentID);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Equipment equipment = new Equipment();
                // Populate the customer object with data from the ResultSet
                equipment.setEquipmentID(resultSet.getInt("equipmentID"));
                equipment.setEquipmentName(resultSet.getString("equipmentName"));
                equipment.setEquipmentCategory(resultSet.getString("equipmentCategory"));
                equipment.setEquipmentCostPerDay(resultSet.getInt("equipmentCostPerDay"));
                equipment.setEquipmentDescription(resultSet.getString("EquipmentDescription"));
                equipment.setAvailiablityStatus(resultSet.getString("availabilityStatus"));

                return equipment;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if customer details are not found
    }

    private void waitForRequests() {
        while (true) {
            try {
                Socket connectionSocket = serverSocket.accept();
                Thread clientThread = new Thread(new ClientHandler(connectionSocket));
                clientThread.start();
                clientCount++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        new Server();
    }

    // Multithreading Implementation
    private class ClientHandler implements Runnable {
        private Socket connectionSocket;
        private ObjectOutputStream objOs;
        private ObjectInputStream objIs;
        private boolean loginResult;

        public ClientHandler(Socket connectionSocket) {
            this.connectionSocket = connectionSocket;
        }

        // Method to Configure Streams
        private void configureStreams() {
            try {
                // Instantiate the output stream, using the getOutputStream method
                // of the Socket object as argument to the constructor
                objOs = new ObjectOutputStream(connectionSocket.getOutputStream());
                // Instantiate the input stream, using the getOutputStream method
                // of the Socket object as argument to the constructor
                objIs = new ObjectInputStream(connectionSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            String action = "";
            getDatabaseConnection();
            Customer cust = null;
            configureStreams();
            try {
                action = (String) objIs.readObject();

                switch (action) {
                    case "Register Customer":
                        cust = (Customer) objIs.readObject();
                        registerCustomer(cust);
                        objOs.writeObject(true);
                        break;
                    case "Customer Login":
                        cust = (Customer) objIs.readObject();
                        String providedPassword = cust.getCustomerpassword();
                        String providedID = Integer.toString(cust.getCustomerID());
                        boolean loginResult = validateCustomerLogin(providedID, providedPassword);
                        this.loginResult = loginResult;
                        objOs.writeObject(loginResult);
                        closeConnection();
                        break;
                    case "Get Customer Details":
                        System.out.println("Received action now doing it");
                        // Read customer id
                        int id = (int) objIs.readObject();
                        String idString = String.valueOf(id);
                        // cust= (Customer) objIs.readObject();
                        cust = getCustomerDetails(idString);
                        System.out.println("Sending: " + cust);
                        objOs.writeObject(cust);
                        System.out.println("Sent: " + cust);
                        break;
                    case "View All Transactions":
                        // Read customer id
                        id = (int) objIs.readObject();
                        // Calling the method to receive the transaction from the db and store it
                        List<Transaction> transactions = retrieveTransactionInfo(id);

                        // Sending the object to the client
                        objOs.writeObject(transactions);
                        break;
                    case "View Single Transaction":
                        // id = (int) objIs.readObject();
                        int rentalID = (int) objIs.readObject();
                        // Calling the method to receive the transaction from the db and store it
                        List<Transaction> singleTransaction = retrieveSingleTransaction(rentalID);
                        System.out.println("Sent: " + singleTransaction);
                        // Sending the object to the client
                        objOs.writeObject(singleTransaction);
                        break;
                    case "Insert Message":
                        cust = (Customer) objIs.readObject();
                        Message message = (Message) objIs.readObject();
                        insertMessage(message, cust);
                        objOs.writeObject(true);
                    case "View Equipment":
                        String category = (String) objIs.readObject();
                        String status = (String) objIs.readObject();
                        List<Equipment> equipmentList = retrieveEquipment(status, category);
                        objOs.writeObject(equipmentList);
                        System.out.println("Sent: " + equipmentList);
                        break;
                    case "Check Availability":
                        int equipmentID = (int) objIs.readObject();
                        Date startdate = (Date) objIs.readObject();
                        Date enddate = (Date) objIs.readObject();
                        boolean availabilityResult = performAvailabilityCheck(equipmentID, startdate, enddate);
                        objOs.writeObject(availabilityResult);
                        if (availabilityResult == true) {
                            Equipment equipment = getEquipment(equipmentID);
                            System.out.println(equipment);
                            double EquipmentCostPerDay = equipment.getEquipmentCostPerDay();// retrieves the cost for
                                                                                            // the equipment

                            long numOfDays = calculateNumDays(startdate, enddate);// this finds the amount of days
                                                                                  // rented
                            double quotation = EquipmentCostPerDay * numOfDays;
                            System.out.println(quotation);
                            Object[] response = new Object[] { true, quotation };
                            objOs.writeObject(response);
                            System.out.println("Sent: " + response);
                        }

                        logger.info("Sent: " + availabilityResult);

                        break;
                    case "Delete Account":
                        int customerID = (int) objIs.readObject();
                        deleteRecord(customerID);
                        break;

                    case "Schedule Equipment":
                        int empID = (int) objIs.readObject();
                        String equipName = (String) objIs.readObject();
                        String equipType = (String) objIs.readObject();
                        String phone = (String) objIs.readObject();
                        String email = (String) objIs.readObject();
                        Date resDate = (Date) objIs.readObject();

                        boolean scheduleResult = scheduleEquipment(empID, equipName, equipType, phone, email, resDate);
                        objOs.writeObject(scheduleResult);
                        break;

                }

            } catch (EOFException ex) {
                System.out.println("Client has terminated connections with the server");
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }

        }

        public long calculateNumDays(java.sql.Date startDate, java.sql.Date endDate) {
            long millisecondsPerDay = 24 * 60 * 60 * 1000; // Number of milliseconds in a day

            // Calculate the time difference in milliseconds between endDate and startDate
            long timeDifference = endDate.getTime() - startDate.getTime();

            // Calculate the number of days by dividing the time difference by
            // millisecondsPerDay
            long numberOfDays = timeDifference / millisecondsPerDay;

            // Add 1 to include the start date in the count
            numberOfDays += 1;

            return numberOfDays;
        }

        private void closeConnection() {
            try {
                objOs.close();
                objOs.close();
                connectionSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * "Check Availability":
     * int equipmentID= (int) objIs.readObject();
     * Date startdate= (Date) objIs.readObject();
     * Date enddate= (Date) objIs.readObject();
     * boolean availabilityResult=performAvailabilityCheck(equipmentID, startdate,
     * enddate);
     * objOs.writeObject(availabilityResult);
     * System.out.println("Sent: "+ availabilityResult);
     * logger.info("Sent: "+ availabilityResult);
     * if(availabilityResult==true)
     * {
     * Equipment equipment=(Equipment) getEquipment(equipmentID);
     * double EquipmentCostPerDay= equipment.getEquipmentCostPerDay();//retrieves
     * the cost for the equipment
     * 
     * long numOfDays= calculateNumDays(startdate, enddate);// this finds the amount
     * of days rented
     * double quotation= EquipmentCostPerDay * numOfDays;
     * objOs.writeObject(quotation);//writes out cost to client
     * }
     */

}
