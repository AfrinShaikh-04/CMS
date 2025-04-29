package cms.Login;
import cms.Register.RegisterForm;
import cms.Admin.AdminUI;
import cms.Employee.EmployeeUI;
import cms.Customer.CustomerUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginForm extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeComboBox;
    private JLabel registerLabel;

    public LoginForm() {
        setTitle("Courier Management System - Login");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        JLabel emailLabel = new JLabel("Email/Username:");
        emailLabel.setFont(new Font("Arial", Font.BOLD, 16));

        emailField = new JTextField();
        emailField.setPreferredSize(new Dimension(250, 35));
        emailField.setFont(new Font("Arial", Font.PLAIN, 16));

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 16));

        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(250, 35));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));

        JLabel userTypeLabel = new JLabel("User Type:");
        userTypeLabel.setFont(new Font("Arial", Font.BOLD, 16));

        userTypeComboBox = new JComboBox<>(new String[]{"Select User Type", "Admin", "Employee", "Customer"});
        userTypeComboBox.setPreferredSize(new Dimension(250, 35));
        userTypeComboBox.setFont(new Font("Arial", Font.PLAIN, 16));

        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(150, 40));
        loginButton.setBackground(new Color(59, 89, 182));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.addActionListener(new LoginAction());

        registerLabel = new JLabel("<html><u>Register</u></html>");
        registerLabel.setForeground(Color.BLUE);
        registerLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                openRegisterPage();
            }
        });

        // Layout setup
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        add(emailLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(passwordLabel, gbc);

        gbc.gridx = 1;
        add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(userTypeLabel, gbc);

        gbc.gridx = 1;
        add(userTypeComboBox, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        add(loginButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        add(registerLabel, gbc);
    }

    // Open registration page
    private void openRegisterPage() {
        RegisterForm registerPage = new RegisterForm(this); // Pass current instance of LoginForm
        registerPage.setVisible(true);
        this.setVisible(false); // Hide the login page
    }


    // Login action to authenticate and navigate
    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = emailField.getText().trim().toLowerCase();
            String password = new String(passwordField.getPassword());
            String userType = (String) userTypeComboBox.getSelectedItem();

            if (userType.equals("Select User Type")) {
                JOptionPane.showMessageDialog(null, "Please select a user type.");
                return;
            }

            String tableName, usernameColumn, passwordColumn, nameColumn, idColumn;

            switch (userType.toLowerCase()) {
                case "admin":
                    tableName = "admin";
                    usernameColumn = "admin_username";
                    passwordColumn = "admin_password";
                    nameColumn = "admin_name";
                    idColumn = "admin_id";
                    break;
                case "employee":
                    tableName = "employee_table";
                    usernameColumn = "emp_username";
                    passwordColumn = "emp_password";
                    nameColumn = "emp_name";
                    idColumn = "eid";
                    break;
                case "customer":
                    tableName = "customer_table";
                    usernameColumn = "cust_username";
                    passwordColumn = "cust_password";
                    nameColumn = "cust_name";
                    idColumn = "cust_id";
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Invalid user type.");
                    return;
            }

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cms", "root", "My134340Database")) {
                String query = "SELECT * FROM " + tableName + " WHERE LOWER(" + usernameColumn + ") = ? AND " + passwordColumn + " = ?";
                System.out.println("Executing query: " + query);
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, email);
                    stmt.setString(2, password);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        System.out.println("Result Set Data:");
                        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                            System.out.println(rs.getMetaData().getColumnName(i) + ": " + rs.getString(i));
                        }
                        String name = rs.getString(nameColumn);
                        int userId = rs.getInt(idColumn);
                        System.out.println("User ID (eid) retrieved from database: " + userId);

                        JOptionPane.showMessageDialog(null, "Welcome " + userType + ", " + name + "!");

                        switch (userType.toLowerCase()) {
                            case "admin":
                                new AdminUI(name);
                                break;
                            case "employee":
                                try {
                                    SwingUtilities.invokeLater(() -> new EmployeeUI(userId).setVisible(true)); // Corrected line
                                } catch (Exception employeeUiException) {
                                    employeeUiException.printStackTrace();
                                    JOptionPane.showMessageDialog(null, "Error launching employee UI: " + employeeUiException.getMessage());
                                }
                                break;
                            case "customer":
                                try {
                                   new CustomerUI(rs.getString("cust_username"));
                                } catch (Exception customerUiException) {
                                    customerUiException.printStackTrace();
                                    JOptionPane.showMessageDialog(null, "Error launching customer UI: " + customerUiException.getMessage());
                                }
                                break;
                        }
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid username or password.");
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Database connection error: " + ex.getMessage());
            } catch (Exception generalException) {
                generalException.printStackTrace();
                JOptionPane.showMessageDialog(null, "An unexpected error occurred: " + generalException.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginForm loginForm = new LoginForm();
            loginForm.setVisible(true);
        });
    }

}
