package com.mycompany.hosptapatientrecordsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginForm extends JFrame {
    // Declare components
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<String> cbRole;
    private JButton btnLogin;

    public LoginForm() {
        // Set up JFrame properties
        setTitle("Hospital Patient Records System - Login");
        setSize(1200, 700); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  

        // Create panel and layout
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());  
        panel.setBackground(new Color(240, 248, 255)); 

        // Create GridBagConstraints to control component positioning
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); 
        // Create and add components
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(new Font("Arial", Font.BOLD, 14));
        lblUsername.setForeground(new Color(0, 51, 102)); 
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lblUsername, gbc);

        txtUsername = new JTextField(20);
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(txtUsername, gbc);

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("Arial", Font.BOLD, 14));
        lblPassword.setForeground(new Color(0, 51, 102));
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(lblPassword, gbc);

        txtPassword = new JPasswordField(20);
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(txtPassword, gbc);

        JLabel lblRole = new JLabel("Role:");
        lblRole.setFont(new Font("Arial", Font.BOLD, 14));
        lblRole.setForeground(new Color(0, 51, 102));
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblRole, gbc);

        String[] roles = { "Doctor", "Receptionist"};
        cbRole = new JComboBox<>(roles);
        cbRole.setFont(new Font("Arial", Font.PLAIN, 14));
        cbRole.setBackground(new Color(255, 255, 255));  
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(cbRole, gbc);

        // Login Button with hospital-themed color and styling
        btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setBackground(new Color(0, 204, 102)); 
        btnLogin.setForeground(Color.WHITE);  
        btnLogin.setFocusPainted(false);  
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(btnLogin, gbc);

        // Add panel to frame
        add(panel);

        // Set login button action
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginAction();
            }
        });
    }

    private void loginAction() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());
        String role = cbRole.getSelectedItem().toString();

        // Connect to the database and check credentials
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Prepare SQL query
            String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, password);
            pst.setString(3, role);

            // Execute query
            ResultSet rs = pst.executeQuery();

            // Check if user exists
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Login Successful! Welcome, " + role);

                // Role-based access
                
               if (role.equals("Doctor")) {
                    new DoctorDashboard(username).setVisible(true); 
                
                } else  {
                    new ReceptionistDashboard().setVisible(true);
                }
                dispose();  // Close login form
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials. Please try again.");
            }

        
            rs.close();
            pst.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database connection error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        // Run the login form
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginForm().setVisible(true);
            }
        });
    }
}
