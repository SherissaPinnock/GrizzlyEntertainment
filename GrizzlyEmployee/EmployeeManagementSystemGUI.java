import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDateChooser;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.toedter.calendar.JCalendar;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class EmployeeManagementSystemGUI {

    private JFrame menuFrame;
    private MiniCalendarDemo miniCalendar;
    JLabel selectedDateLabel;

    private Set<String> scheduledDates = new HashSet<>();

    public EmployeeManagementSystemGUI() throws SQLException {
        initializeUI();

    }

    private void initializeUI() throws SQLException {
        menuFrame = new JFrame("Employee Management System");

        JLabel welcomeLabel = new JLabel("Welcome to Grizzly's Management System");
        welcomeLabel.setFont(new Font("Times New Roman", Font.BOLD, 32));
        welcomeLabel.setBounds(250, 50, 800, 50);
        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(new Color(0xFB4E7B));
        menuPanel.setLayout(null);
        menuFrame.add(menuPanel);
        menuFrame.add(welcomeLabel);

        JButton viewEmpButton = createButton("View all rental Requests", 400, 200);
        viewEmpButton.addActionListener(e -> {
            try {
                viewRentalRequest();
            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });

        // JButton viewEquipments = createButton("View Equipment", 400, 270);
        // viewEquipments.addActionListener(e -> {
        // viewEquipment();
        // });

        JButton addEmpButton = createButton("Schedule Equipment", 400, 270);
        addEmpButton.addActionListener(e -> {
            scheduleEquipment();
        });

        JButton editEmpButton = createButton("Open Messages", 400, 340);
        editEmpButton.addActionListener(e -> {
            openMessages();
        });

        JButton deleteEmpButton = createButton("Create Invoice", 400, 410);
        deleteEmpButton.addActionListener(e -> createInvoice());

        JButton exitButton = createButton("Exit", 400, 480);
        exitButton.addActionListener(e -> menuFrame.dispose());

        menuFrame.setSize(1100, 750);
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setLayout(null); // Using no layout managers
        menuFrame.setVisible(true);
        menuFrame.getContentPane().setBackground(new Color(0x009E99));

    }

    private void viewEquipment() {

    }

    private JButton createButton(String text, int x, int y) {
        JButton button = new JButton(text);
        button.setBounds(x, y, 300, 40);
        button.setFont(new Font("Times New Roman", Font.PLAIN, 20));
        button.setFocusPainted(false);
        menuFrame.add(button);
        return button;
    }

    public void viewRentalRequest() throws SQLException {
        menuFrame.setVisible(false);
    }

    public void scheduleEquipment() {
        menuFrame.setVisible(false);

        JFrame frame = new JFrame("Schedule Equipment");
        JPanel panel = new JPanel(new GridLayout(12, 3));

        JLabel idLabel = new JLabel("Enter ID");
        idLabel.setFont(new Font("Times New Roman", Font.PLAIN, 20));

        JTextField idVal = new JTextField();
        idVal.setFont(new Font("Times New Roman", Font.PLAIN, 20));
        idVal.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) {
                    e.consume();
                    JOptionPane.showMessageDialog(frame, "Invalid character. Please enter only numeric values.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JLabel nameLabel = new JLabel("Select Equipment Name");
        nameLabel.setFont(new Font("Times New Roman", Font.PLAIN, 20));

        String[] equipmentNames = { "staging", "lighting", "power", "sound" };
        JComboBox<String> nameComboBox = new JComboBox<>(equipmentNames);
        nameComboBox.setFont(new Font("Times New Roman", Font.PLAIN, 20));

        JLabel phoneLabel = new JLabel("Enter Phone Number");
        phoneLabel.setFont(new Font("Times New Roman", Font.PLAIN, 20));

        JTextField phoneVal = new JTextField();
        phoneVal.setFont(new Font("Times New Roman", Font.PLAIN, 20));
        phoneVal.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) {
                    e.consume();
                    JOptionPane.showMessageDialog(frame, "Invalid character. Please enter only numeric values.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JLabel emailLabel = new JLabel("Enter Email Address");
        emailLabel.setFont(new Font("Times New Roman", Font.PLAIN, 20));

        JTextField emailVal = new JTextField();
        emailVal.setFont(new Font("Times New Roman", Font.PLAIN, 20));

        JLabel startDateLabel = new JLabel("Select Start Date");
        startDateLabel.setFont(new Font("Times New Roman", Font.PLAIN, 20));

        JDateChooser startDateChooser = new JDateChooser();
        startDateChooser.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                if ("date".equals(e.getPropertyName())) {
                    updateSelectedDateLabel(startDateChooser, selectedStartDateLabel);
                }
            }

            private void updateSelectedDateLabel(JDateChooser dateChooser, JLabel label) {
                Date selectedDate = dateChooser.getDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String selectedDateString = sdf.format(selectedDate);
                label.setText("Selected Start Date: " + selectedDateString);
            }
        });

        selectedStartDateLabel = new JLabel("Selected Start Date: ");
        selectedStartDateLabel.setFont(new Font("Times New Roman", Font.PLAIN, 20));

        JLabel endDateLabel = new JLabel("Select End Date");
        endDateLabel.setFont(new Font("Times New Roman", Font.PLAIN, 20));

        JDateChooser endDateChooser = new JDateChooser();
        endDateChooser.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                if ("date".equals(e.getPropertyName())) {
                    updateSelectedDateLabel(endDateChooser, selectedEndDateLabel);
                }
            }

            private void updateSelectedDateLabel(JDateChooser dateChooser, JLabel label) {
                Date selectedDate = dateChooser.getDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String selectedDateString = sdf.format(selectedDate);
                label.setText("Selected End Date: " + selectedDateString);
            }
        });

        selectedEndDateLabel = new JLabel("Selected End Date: ");
        selectedEndDateLabel.setFont(new Font("Times New Roman", Font.PLAIN, 20));

        JPanel calendarPanel = new JPanel();
        calendarPanel.add(startDateLabel);
        calendarPanel.add(startDateChooser);
        calendarPanel.add(selectedStartDateLabel);
        calendarPanel.add(endDateLabel);
        calendarPanel.add(endDateChooser);
        calendarPanel.add(selectedEndDateLabel);

        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Times New Roman", Font.PLAIN, 20));
        backButton.addActionListener(actionListener -> {
            frame.dispose();
            menuFrame.setVisible(true);
        });

        JButton submitButton = new JButton("Submit");
        submitButton.setFont(new Font("Times New Roman", Font.PLAIN, 20));
        submitButton.addActionListener(actionListener -> {
            String idStr = idVal.getText();
            String name = nameComboBox.getSelectedItem().toString();
            String phoneNum = phoneVal.getText();
            String email = emailVal.getText();
            String selectedDateString = selectedDateLabel.getText().replace("Selected Date: ", "");

            if (!scheduledDates.contains(selectedDateString)) {
                scheduledDates.add(selectedDateString);
                String successMessage = "Equipment scheduled successfully for date: " + selectedDateString;
                JOptionPane.showMessageDialog(frame, successMessage, "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                String errorMessage = "Equipment already scheduled for this date";
                JOptionPane.showMessageDialog(frame, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(idLabel);
        panel.add(idVal);
        panel.add(nameLabel);
        panel.add(nameComboBox);
        panel.add(phoneLabel);
        panel.add(phoneVal);
        panel.add(emailLabel);
        panel.add(emailVal);
        panel.add(calendarPanel);
        panel.add(backButton);
        panel.add(submitButton);

        panel.setBackground(new Color(254, 251, 246));
        panel.setBorder(new EmptyBorder(50, 50, 50, 50));

        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1100, 750));
        frame.pack();
        frame.setVisible(true);
    }

    private void openMessages() {
        SwingUtilities.invokeLater(() -> {
            TableCreator.createAndShowGUI();
        });
    }

    private void createInvoice() {

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new EmployeeManagementSystemGUI();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
    }
}
