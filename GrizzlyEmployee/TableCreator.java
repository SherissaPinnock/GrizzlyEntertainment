import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class TableCreator {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });
    }

    static void createAndShowGUI() {
        JFrame frame = new JFrame("Advanced Table Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.setLayout(new BorderLayout());

        // Create a table model with default data and column names
        DefaultTableModel tableModel = new DefaultTableModel();
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Create a scroll pane to hold the table
        JScrollPane scrollPane = new JScrollPane(table);

        // Add the scroll pane to the frame
        frame.add(scrollPane, BorderLayout.CENTER);

        // Add a filter section
        JPanel filterPanel = new JPanel();
        JTextField filterField = new JTextField(10);
        JButton filterButton = new JButton("Filter");
        filterPanel.add(new JLabel("Message ID:"));
        filterPanel.add(filterField);
        filterPanel.add(filterButton);

        frame.add(filterPanel, BorderLayout.NORTH);

        // Add a messaging section
        JTextArea messageTextArea = new JTextArea(5, 30);
        JButton sendButton = new JButton("Send Message");
        JPanel messagingPanel = new JPanel();

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    String message = messageTextArea.getText();
                    if (!message.isEmpty()) {
                        String customerName = (String) table.getValueAt(selectedRow, 1);
                        JOptionPane.showMessageDialog(frame, "Message sent to " + customerName);
                        messageTextArea.setText("");
                    } else {
                        JOptionPane.showMessageDialog(frame, "Please enter a message");
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Please select a customer to send a message");
                }
            }
        });

        messagingPanel.add(new JLabel("Send Message:"));
        messagingPanel.add(messageTextArea);
        messagingPanel.add(sendButton);

        frame.add(messagingPanel, BorderLayout.SOUTH);

        // Create a TableRowSorter for the table
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Add an ActionListener to the Filter button
        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filterText = filterField.getText();
                if (filterText.length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    try {
                        int messageId = Integer.parseInt(filterText);
                        sorter.setRowFilter(RowFilter.regexFilter(String.valueOf(messageId), 0));
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "Invalid Message ID");
                    }
                }
            }
        });

        // Fetch data from the server and update the table
        SwingUtilities.invokeLater(() -> {
            // add server details
        });

        frame.setVisible(true);
    }
}
