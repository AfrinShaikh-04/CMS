package cms.Register;

import cms.Login.LoginForm;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class RegisterForm extends JFrame {

    private JTextField usernameField, nameField, contactField, addressField;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeComboBox;
    private LoginForm loginForm;

    public RegisterForm(LoginForm loginForm) {
        this.loginForm = loginForm;
        initializeUI();
    }
    private void initializeUI() {
        setTitle("Registration Form");
        setSize(550, 550); // Slightly decreased size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(240, 240, 240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(12, 12, 12, 12); // Slightly decreased spacing
        gbc.anchor = GridBagConstraints.WEST;

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 16)); // Slightly smaller font
        add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(250, 35)); // Slightly smaller text field
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16)); // Slightly smaller font
        add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(passwordLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(250, 35));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(nameLabel, gbc);

        gbc.gridx = 1;
        nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(250, 35));
        nameField.setFont(new Font("Arial", Font.PLAIN, 16));
        add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel contactLabel = new JLabel("Contact:");
        contactLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(contactLabel, gbc);

        gbc.gridx = 1;
        contactField = new JTextField();
        contactField.setPreferredSize(new Dimension(250, 35));
        contactField.setFont(new Font("Arial", Font.PLAIN, 16));
        add(contactField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(addressLabel, gbc);

        gbc.gridx = 1;
        addressField = new JTextField();
        addressField.setPreferredSize(new Dimension(250, 35));
        addressField.setFont(new Font("Arial", Font.PLAIN, 16));
        add(addressField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel userTypeLabel = new JLabel("User Type:");
        userTypeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(userTypeLabel, gbc);

        gbc.gridx = 1;
        userTypeComboBox = new JComboBox<>(new String[]{"Customer", "Employee"});
        userTypeComboBox.setPreferredSize(new Dimension(250, 35));
        userTypeComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        add(userTypeComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton registerButton = new JButton("Register");
        registerButton.setPreferredSize(new Dimension(150, 45)); // Slightly smaller button
        registerButton.setFont(new Font("Arial", Font.BOLD, 18)); // Slightly smaller font
        registerButton.setBackground(new Color(59, 89, 182));
        registerButton.setForeground(Color.WHITE);
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });
        add(registerButton, gbc);

        gbc.gridy = 7;
        JLabel loginLinkLabel = new JLabel("<html><a href='#'>Already have an account? Login here.</a></html>");
        loginLinkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLinkLabel.setForeground(Color.BLUE);
        loginLinkLabel.setFont(new Font("Arial", Font.PLAIN, 14)); // Slightly smaller font
        loginLinkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                openLoginPage();
            }
        });
        add(loginLinkLabel, gbc);

        revalidate();
        repaint();
        setVisible(true);
    }

    private void openLoginPage() {
        loginForm.setVisible(true);
        dispose();
    }

    private void registerUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String name = nameField.getText().trim();
        String contact = contactField.getText().trim();
        String address = addressField.getText().trim();
        String userType = (String) userTypeComboBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty() || name.isEmpty() || contact.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to register?", "Confirm Registration", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/cms", "root", "My134340Database")) {
            String query;
            if (userType.equalsIgnoreCase("customer")) {
                query = "INSERT INTO customer_table (cust_name, cust_username, cust_password, contact, address) VALUES (?, ?, ?, ?, ?)";
            } else {
                query = "INSERT INTO employee_table (emp_name, emp_username, emp_password, contact, address) VALUES (?, ?, ?, ?, ?)";
            }

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, name);
                stmt.setString(2, username);
                stmt.setString(3, password);
                stmt.setString(4, contact);
                stmt.setString(5, address);

                int rowsInserted = stmt.executeUpdate();
                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(this, "Registration successful! Please login.");
                    openLoginPage();
                } else {
                    JOptionPane.showMessageDialog(this, "Error during registration.");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
       LoginForm loginForm = new LoginForm();
        new RegisterForm(loginForm);
    }
}
