package cms.Customer;

import cms.Login.LoginForm;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CustomerUI extends JFrame {

    private String custUsername;
    private int customerId;
    private JLabel customerDetailsLabel;
    private JTable courierTable;
    private DefaultTableModel tableModel;
    private JButton addCourierButton;
    private JButton logoutButton;

    public CustomerUI(String custUsername) {
        this.custUsername = custUsername;
        initializeUI();
        loadCustomerDetails();
        loadCourierData();
    }

    private void initializeUI() {
        setTitle("Customer Panel - Courier Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Panel for customer details
        JPanel customerDetailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        customerDetailsLabel = new JLabel("Loading customer details...");
        customerDetailsPanel.add(customerDetailsLabel);
        add(customerDetailsPanel, BorderLayout.NORTH); // Add customer details panel to the top

        // Table for courier data
        tableModel = new DefaultTableModel();
        courierTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(courierTable);
        add(tableScrollPane, BorderLayout.CENTER); // Add table to the center

        // Panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addCourierButton = new JButton("Add Courier");
        addCourierButton.addActionListener(e -> openAddCourierForm());
        buttonPanel.add(addCourierButton);

        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        buttonPanel.add(logoutButton);

        add(buttonPanel, BorderLayout.SOUTH); // Add button panel to the bottom

        revalidate();
        repaint();

        setVisible(true);
    }

    private void loadCustomerDetails() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/cms", "root", "My134340Database")) {
            String query = "SELECT * FROM customer_table WHERE cust_username = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, custUsername);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                customerId = resultSet.getInt("cust_id");
                String customerName = resultSet.getString("cust_name");
                String email = resultSet.getString("cust_username");
                String phone = resultSet.getString("contact");
                String address = resultSet.getString("address");

                // Update the label to show customer details (Corrected)
                customerDetailsLabel.setText("<html><b>Customer Name: </b>" + customerName + "<br>" +
                        "<b>Username: </b>" + email + "<br>" +
                        "<b>Contact: </b>" + phone + "<br>" +  // Corrected: Single Contact Label
                        "<b>Address: </b>" + address + "</html>"); // Corrected: Added Address Label

                System.out.println("Customer found! ID: " + customerId + ", Name: " + customerName);
            } else {
                JOptionPane.showMessageDialog(this, "Customer details not found!");
                System.out.println("Customer details not found for username: " + custUsername);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading customer details: " + ex.getMessage());
            System.err.println("SQL Exception in loadCustomerDetails: " + ex.getMessage());
        } catch (Exception generalException) {
            generalException.printStackTrace();
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + generalException.getMessage());
            System.err.println("General Exception in loadCustomerDetails: " + generalException.getMessage());
        }
    }

    private void loadCourierData() {
        SwingUtilities.invokeLater(() -> {
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/cms", "root", "My134340Database")) {
                String query = "SELECT sender_name, receiver_name, pickup_add, destination, weight, status FROM courier_table WHERE cust_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, customerId);
                ResultSet resultSet = preparedStatement.executeQuery();

                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                tableModel.setColumnCount(0);
                for (int i = 1; i <= columnCount; i++) {
                    tableModel.addColumn(metaData.getColumnName(i));
                }

                tableModel.setRowCount(0);
                while (resultSet.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        row[i - 1] = resultSet.getObject(i);
                    }
                    tableModel.addRow(row);
                }

                tableModel.fireTableDataChanged();
                System.out.println("Courier data loaded successfully!");

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading courier data: " + ex.getMessage());
            }
        });
    }

    private void openAddCourierForm() {
        JTextField senderNameField = new JTextField(20);
        JTextField receiverNameField = new JTextField(20);
        JTextField pickupAddressField = new JTextField(20);
        JTextField destinationField = new JTextField(20);
        JTextField weightField = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.add(new JLabel("Sender Name:"));
        panel.add(senderNameField);
        panel.add(new JLabel("Receiver Name:"));
        panel.add(receiverNameField);
        panel.add(new JLabel("Pickup Address:"));
        panel.add(pickupAddressField);
        panel.add(new JLabel("Destination:"));
        panel.add(destinationField);
        panel.add(new JLabel("Weight (kg):"));
        panel.add(weightField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Add New Courier", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String senderName = senderNameField.getText().trim();
            String receiverName = receiverNameField.getText().trim();
            String pickupAdd = pickupAddressField.getText().trim();
            String destination = destinationField.getText().trim();
            String weightStr = weightField.getText().trim();

            if (senderName.isEmpty() || receiverName.isEmpty() || pickupAdd.isEmpty() || destination.isEmpty() || weightStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }

            try {
                double weight = Double.parseDouble(weightStr);
                addCourierToDatabase(senderName, receiverName, pickupAdd, destination, weight);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Weight must be a valid number.");
            }
        }
    }

    private void addCourierToDatabase(String senderName, String receiverName, String pickupAdd, String destination, double weight) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/cms", "root", "My134340Database")) {

            String insertQuery = "INSERT INTO courier_table (sender_name, receiver_name, pickup_add, destination, weight, status, cust_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setString(1, senderName);
            preparedStatement.setString(2, receiverName);
            preparedStatement.setString(3, pickupAdd);
            preparedStatement.setString(4, destination);
            preparedStatement.setDouble(5, weight);
            preparedStatement.setString(6, "Pending");
            preparedStatement.setInt(7, customerId);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Courier added successfully!");
                loadCourierData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add courier. Please try again.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding courier: " + ex.getMessage());
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, "Logging out...");
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
            dispose();
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CustomerUI("krystal123@gmail.com")); // Replace with a valid username from your database
    }
}
