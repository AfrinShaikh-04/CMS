package cms.Admin;
import cms.Login.LoginForm;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminUI extends JFrame {

    private String adminName;
    private String adminEmail;
    private String adminPhone;
    private JButton employeeButton;
    private JButton courierButton;
    private JButton customerButton;
    private JLabel adminLabel;
    private JTable dataTable;
    private JScrollPane tableScrollPane;
    private JButton logoutButton;
    private JButton updateButton;
    // Declare at the top with other class-level variables
    private JPanel topPanel;

    private String currentTableName;

    public AdminUI(String adminName) {
        this.adminName = adminName;
        initializeUI();
        loadAdminDetails();
    }

    private void initializeUI() {
        setTitle("Admin Panel - Courier Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Reduce gaps in BorderLayout
        setLayout(new BorderLayout(0, 2)); // Reduced gap from 5 to 2 pixels

        // Top Panel with Admin Info (initialize the class-level topPanel)
        topPanel = new JPanel(new GridLayout(3, 1, 0, 2)); // Now it's the class-level variable
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5)); // Tighter margin around panel
        adminLabel = new JLabel();
        topPanel.add(adminLabel);
        add(topPanel, BorderLayout.NORTH);

        // Left Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        employeeButton = new JButton("Employees");
        courierButton = new JButton("Couriers");
        customerButton = new JButton("Customers");

        employeeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        courierButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        customerButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttonPanel.add(employeeButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(courierButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(customerButton);

        add(buttonPanel, BorderLayout.WEST);

        // Center Table Panel
        dataTable = new JTable();
        tableScrollPane = new JScrollPane(dataTable);
        add(tableScrollPane, BorderLayout.CENTER);

        // Bottom Panel for Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        updateButton = new JButton("Update");
        logoutButton = new JButton("Logout");

        bottomPanel.add(updateButton);
        bottomPanel.add(logoutButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // Button Action Listeners
        employeeButton.addActionListener(e -> {
            currentTableName = "employee_table";
            loadTableData(currentTableName);
        });

        courierButton.addActionListener(e -> {
            currentTableName = "courier_table";
            loadTableData(currentTableName);
        });

        customerButton.addActionListener(e -> {
            currentTableName = "customer_table";
            loadTableData(currentTableName);
        });

        updateButton.addActionListener(e -> updateSelectedTable());

        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to log out?", "Logout Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(null, "Logging out...");
                SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
                dispose();
            }
        });

        setVisible(true);
    }


    private void loadAdminDetails() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/cms", "root", "My134340Database");
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM admin WHERE admin_name = ?")) {

            preparedStatement.setString(1, adminName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                adminEmail = resultSet.getString("admin_username");
                adminPhone = resultSet.getString("admin_phone");

                // Clear previous content
                topPanel.removeAll();

                // Add new labels for different lines
                JLabel nameLabel = new JLabel("Name: " + adminName);
                JLabel emailLabel = new JLabel("Email: " + adminEmail);
                JLabel phoneLabel = new JLabel("Phone: " + adminPhone);

                topPanel.add(nameLabel);
                topPanel.add(emailLabel);
                topPanel.add(phoneLabel);

                // Refresh the panel to reflect changes
                topPanel.revalidate();
                topPanel.repaint();
            } else {
                adminLabel.setText("Admin details not found.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading admin details: " + ex.getMessage());
        }
    }

    private void loadTableData(String tableName) {
        SwingUtilities.invokeLater(() -> {
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/cms", "root", "My134340Database");
                 Statement statement = connection.createStatement()) {

                String query = "SELECT * FROM " + tableName;

                try (ResultSet resultSet = statement.executeQuery(query)) {
                    DefaultTableModel model = new DefaultTableModel();
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    for (int i = 1; i <= columnCount; i++) {
                        model.addColumn(metaData.getColumnName(i));
                    }

                    while (resultSet.next()) {
                        Object[] row = new Object[columnCount];
                        for (int i = 1; i <= columnCount; i++) {
                            row[i - 1] = resultSet.getObject(i);
                        }
                        model.addRow(row);
                    }
                    dataTable.setModel(model);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage());
            }
        });
    }

    private void updateSelectedTable() {
        if (dataTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.");
            return;
        }

        if (currentTableName != null) {
            if (currentTableName.equals("courier_table")) {
                updateAssignedEid();
            } else if (currentTableName.equals("employee_table")) {
                updateAssignedCourier();
            } else {
                JOptionPane.showMessageDialog(this, "Updates are not allowed for this table.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "No table selected.");
        }
    }

    private void updateAssignedEid() {
        int selectedRow = dataTable.getSelectedRow();
        DefaultTableModel model = (DefaultTableModel) dataTable.getModel();

        int cid = (int) model.getValueAt(selectedRow, 0);
        String newAssignedEidString = JOptionPane.showInputDialog(this, "Enter new assigned Eid:");

        if (newAssignedEidString == null || newAssignedEidString.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Assigned eid cannot be empty.");
            return;
        }

        try {
            int newAssignedEid = Integer.parseInt(newAssignedEidString);

            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/cms", "root", "My134340Database");
                 PreparedStatement preparedStatement = connection.prepareStatement("UPDATE courier_table SET assigned_eid = ? WHERE cid = ?")) {

                preparedStatement.setInt(1, newAssignedEid);
                preparedStatement.setInt(2, cid);

                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Assigned eid updated successfully.");
                    loadTableData("courier_table");
                } else {
                    JOptionPane.showMessageDialog(this, "Update failed. No rows updated.");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter a valid integer for assigned eid.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating assigned eid: " + ex.getMessage());
        }
    }

    // Update Assigned Courier in employee_table
    private void updateAssignedCourier() {
        int selectedRow = dataTable.getSelectedRow();
        DefaultTableModel model = (DefaultTableModel) dataTable.getModel();

        int employeeId = (int) model.getValueAt(selectedRow, 0);
        String newAssignedCourier = JOptionPane.showInputDialog(this, "Enter new assigned courier:");

        if (newAssignedCourier != null && !newAssignedCourier.isEmpty()) {
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/cms", "root", "My134340Database");
                 PreparedStatement preparedStatement = connection.prepareStatement("UPDATE employee_table SET assigned_courier = ? WHERE eid = ?")) {

                preparedStatement.setString(1, newAssignedCourier);
                preparedStatement.setInt(2, employeeId);

                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Assigned courier updated successfully.");
                    loadTableData("employee_table");
                } else {
                    JOptionPane.showMessageDialog(this, "Update failed. No rows updated.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating assigned courier: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Assigned courier cannot be empty.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Dummy Data for Testing
            AdminUI adminUI = new AdminUI("Admin04");
            adminUI.setVisible(true);
        });
    }
}
