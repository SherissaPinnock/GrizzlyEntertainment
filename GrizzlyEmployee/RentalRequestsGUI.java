import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RentalRequestsGUI {

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

            // Execute a query to retrieve data from messages table
            String query = "SELECT customerID, firstName, lastName, message FROM messages";
            ResultSet result = statement.executeQuery(query);

            // Iterate through the result set and add rows to the model
            while (result.next()) {
                int customerID = result.getInt("customerID");
                String firstName = result.getString("firstName");
                String lastName = result.getString("lastName");
                String request = result.getString("message");

                // Add a row to the model
                model.addRow(new Object[]{customerID, firstName, lastName, request});
            }

            // Close the statement and result set
            result.close();
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    private static void configureTable(JTable table) {
        // Enable word wrap for the "Request" column
        TableColumnModel columnModel = table.getColumnModel();
        TableColumn requestColumn = columnModel.getColumn(3); // Assuming "Request" is the fourth column
        requestColumn.setCellRenderer(new WordWrapCellRenderer());

        // Set a custom row height 
        table.setRowHeight(50); // Set to the desired height
    }
    
    // Custom cell renderer for word wrap
    static class WordWrapCellRenderer extends JTextArea implements TableCellRenderer {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		WordWrapCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            setSize(table.getColumnModel().getColumn(column).getWidth(),
                    getPreferredSize().height);
            return this;
        }
    }
    
    

    public static void main(String[] args) {
        getDatabaseConnection();

        // Create a JFrame for the pop-up window
        JFrame frame = new JFrame("Rental Requests");
        frame.setSize(600, 400);

        // Create a label for the heading
        JLabel headingLabel = new JLabel("GrizzlyEntertainment", JLabel.CENTER);
        headingLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // Create a DefaultTableModel to hold data for the JTable
        DefaultTableModel model = new DefaultTableModel();

        // Define the column names
        String[] columnNames = {"CustomerID", "First Name", "Last Name", "Request"};

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
        
     // Configure the table (enable word wrap for specific columns)
        configureTable(table);
            
    }
    
}
