package cms.Employee;
import cms.Login.LoginForm;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class EmployeeUI extends JFrame {

    private String employeeName;
    private String empUsername;
    private int employeeId;
    private JLabel employeeLabel;
    private JTable courierTable;
    private JScrollPane tableScrollPane;
    private JButton updateButton;
    private JButton logoutButton;

    public EmployeeUI(int employeeId) {
        System.out.println("cms.Employee.EmployeeUI constructor called with employeeId: " + employeeId);
        this.employeeId = employeeId;
        initializeUI();
        loadEmployeeDetails();
        loadCourierTable();
    }

    private void initializeUI() {
        setTitle("Employee Panel - Courier Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel employeePanel = new JPanel();
        employeePanel.setLayout(new BoxLayout(employeePanel, BoxLayout.Y_AXIS));

        employeeLabel = new JLabel("Loading employee details...");
        employeePanel.add(employeeLabel);
        add(employeePanel, BorderLayout.WEST);

        courierTable = new JTable();
        tableScrollPane = new JScrollPane(courierTable);
        add(tableScrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        updateButton = new JButton("Update Status");
        logoutButton = new JButton("Logout");

        bottomPanel.add(updateButton);
        bottomPanel.add(logoutButton);
        add(bottomPanel, BorderLayout.SOUTH);

        updateButton.addActionListener(e -> updateSelectedCourierStatus());

        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to log out?", "Logout Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(null, "Logging out...");
                SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true)); //Uncomment when LoginForm is available.
                dispose();
            }
        });

        setVisible(true);
    }

    private void loadEmployeeDetails() {
        System.out.println("Employee ID received in loadEmployeeDetails: " + employeeId);
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/cms", "root", "My134340Database")) {
            System.out.println("Database connection successful in loadEmployeeDetails.");

            String query = "SELECT emp_name, emp_username, contact FROM employee_table WHERE eid = ?";
            System.out.println("Executing query: " + query);

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            System.out.println("Employee ID passed to query: " + employeeId);
            preparedStatement.setInt(1, employeeId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                employeeName = resultSet.getString("emp_name");
                String email = resultSet.getString("emp_username");
                String phone = resultSet.getString("contact");

                System.out.println("Employee Name from DB: " + employeeName);
                System.out.println("Email from DB: " + email);
                System.out.println("Phone from DB: " + phone);

                SwingUtilities.invokeLater(() -> {
                    if (employeeName != null && email != null && phone != null) {
                        employeeLabel.setText("<html><b>Employee: </b>" + employeeName + "<br>" +
                                "<b>Username: </b>" + email + "<br>" +
                                "<b>Phone: </b>" + phone + "</html>");
                        employeeLabel.revalidate();
                        employeeLabel.repaint();
                    } else {
                        employeeLabel.setText("Employee details not available.");
                    }
                });

                System.out.println("Employee Details Loaded:");
                System.out.println("Name: " + employeeName);
                System.out.println("Username: " + email);
                System.out.println("Phone: " + phone);
            } else {
                JOptionPane.showMessageDialog(this, "Employee details not found!");
                System.out.println("No employee details found for eid: " + employeeId);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading employee details: " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage());
        }
    }

    private void loadCourierTable() {
        SwingUtilities.invokeLater(() -> {
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/cms", "root", "My134340Database")) {
                System.out.println("Database connection successful in loadCourierTable.");

                String query = "SELECT * FROM courier_table WHERE assigned_eid = ?";
                System.out.println("Executing query: " + query);

                PreparedStatement preparedStatement = connection.prepareStatement(query);
                System.out.println("Employee ID used for courier query: " + employeeId);
                preparedStatement.setInt(1, employeeId);

                ResultSet resultSet = preparedStatement.executeQuery();

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

                courierTable.setModel(model);
                System.out.println("Courier table loaded.");

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading courier data: " + ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage());
            }
        });
    }

    private void updateSelectedCourierStatus() {
        int selectedRow = courierTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to update.");
            return;
        }

        String newStatus = JOptionPane.showInputDialog(this, "Enter new status:");
        if (newStatus == null || newStatus.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Status cannot be empty.");
            return;
        }

        int cid = (int) courierTable.getValueAt(selectedRow, 0);
        updateCourierStatus(cid, newStatus);
    }

    private void updateCourierStatus(int cid, String newStatus) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/cms", "root", "My134340Database")) {
            System.out.println("Database connection successful in updateCourierStatus.");

            String updateQuery = "UPDATE courier_table SET status = ? WHERE cid = ?";
            System.out.println("Executing query: " + updateQuery);

            PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
            preparedStatement.setString(1, newStatus);
            preparedStatement.setInt(2, cid);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Status updated successfully.");
                loadCourierTable();
            } else {
                JOptionPane.showMessageDialog(this, "No rows affected. Please try again.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating status: " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        int employeeId = 201;
        SwingUtilities.invokeLater(() -> new EmployeeUI(employeeId).setVisible(true));
    }
}