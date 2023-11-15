package employee;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.sql.Date;

public class ViewInventoryGUI {

    private static Connection dBConn = null;
    private ResultSet result = null;

    private static Connection getDatabaseConnection() {
        if (dBConn == null) {
            try {
                String url = "jdbc:mysql://localhost:3306/grizzlycustomers";
                dBConn = DriverManager.getConnection(url, "root", "");

                JOptionPane.showMessageDialog(null, "DB Connection Established",
                        "CONNECTION STATUS", JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Could not connect to database\n" + ex,
                        "Connection Failure", JOptionPane.ERROR_MESSAGE);
            }

        }
        return dBConn;
    }

    private static void populateTable(DefaultTableModel model) {
        try {
            Connection connection = getDatabaseConnection();

            // Create a SQL statement
            java.sql.Statement statement = connection.createStatement();

            // Execute a query to retrieve equipment ID and name
            String query = "SELECT equipmentID, equipmentName FROM equipment";
            ResultSet result = statement.executeQuery(query);

            // Iterate through the result set and add rows to the model
            while (result.next()) {
                int equipmentID = result.getInt("equipmentID");
                String equipmentName = result.getString("equipmentName");

                String rentalStatus = checkRentalStatus(equipmentID); // Method for rental status

                // Add a row to the model
                model.addRow(new Object[]{equipmentID, equipmentName, rentalStatus});
            }

            // Close the statement and result set
            result.close();
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static String checkRentalStatus(int equipmentID) {
        try {
            Connection connection = getDatabaseConnection();
            java.sql.Statement statement = connection.createStatement();

            // Query to check if equipmentID is present in rentals table
            String checkQuery = "SELECT * FROM rentals WHERE equipmentID = " + equipmentID;
            ResultSet checkResult = statement.executeQuery(checkQuery);

            if (checkResult.next()) {
                // EquipmentID is present in rentals table, now check date range
                Date startDate = checkResult.getDate("startDate");
                Date endDate = checkResult.getDate("endDate");

                // Get current date
                LocalDate currentDate = LocalDate.now();

                // Check if current date is within the rental period
                if (currentDate.compareTo(startDate.toLocalDate()) >= 0 && currentDate.compareTo(endDate.toLocalDate()) <= 0) {
                    // Booked
                    return "Booked";
                } else {
                    // Not within the rental period
                    return "Available";
                }
            } else {
                // EquipmentID not present in rentals table
                return "Available";
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            return "Error";
        }
    }

    public static void main(String[] args) {
        getDatabaseConnection();

        // Create a JFrame for the pop-up window
        JFrame frame = new JFrame("Equipment Inventory");
        frame.setSize(400, 300);

        // Create a label for the heading
        JLabel headingLabel = new JLabel("GrizzlyEntertainment", JLabel.CENTER);
        headingLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // Create a DefaultTableModel to hold data for the JTable
        DefaultTableModel model = new DefaultTableModel();

        // Define the column names
        String[] columnNames = {"Equipment ID", "Equipment Name", "Rental Status"};

        // Set the column names in the model
        model.setColumnIdentifiers(columnNames);

        // Create a JTable with the DefaultTableModel
        JTable table = new JTable(model);

        // Create a JScrollPane to hold the table
        JScrollPane scrollPane = new JScrollPane(table);

        // Create a JPanel to hold the label and table
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(headingLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add the JPanel to the frame
        frame.add(panel);

        // Set frame properties
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Populate the table
        populateTable(model);
    }
}




