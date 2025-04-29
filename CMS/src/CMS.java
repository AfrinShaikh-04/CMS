import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import cms.Login.LoginForm;
import cms.Register.RegisterForm;
import cms.Admin.AdminUI;
import cms.Employee.EmployeeUI;
import cms.Customer.CustomerUI;

public class CMS extends JFrame {

    public CMS() {
        setTitle("Welcome to Courier Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        BufferedImage backgroundImage = null;
        try {
            backgroundImage = ImageIO.read(new File("D:/bg1.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading background image.");
        }

        BufferedImage finalBackgroundImage = backgroundImage;
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (finalBackgroundImage != null) {
                    g.drawImage(finalBackgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());

        JLabel welcomeLabel = new JLabel("Welcome to our Courier Management System");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        backgroundPanel.add(welcomeLabel, gbc);

        add(backgroundPanel);

        setVisible(true);

        Timer timer = new Timer(2000, e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true)); //replace with your login form call
            System.out.println("Login page called"); //replace with your login form call.

        });
        timer.setRepeats(false);
        timer.start();
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(CMS::new);
    }
}