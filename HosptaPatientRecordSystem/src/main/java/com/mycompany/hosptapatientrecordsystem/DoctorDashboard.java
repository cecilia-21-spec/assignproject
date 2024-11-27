package com.mycompany.hosptapatientrecordsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class DoctorDashboard extends JFrame {

    private String doctorUsername; 
    private JPanel centerPanel;   

    public DoctorDashboard(String username) {
        this.doctorUsername = username;

        setTitle("Doctor Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Layout and panels
        setLayout(new BorderLayout());

        // Top Panel: Welcome message
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(0, 123, 255));
        JLabel welcomeLabel = new JLabel("Welcome, Dr. " + username + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        topPanel.add(welcomeLabel);

        // Center Panel: Dynamic content area
        centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setBackground(Color.LIGHT_GRAY);

        // Bottom Navigation Panel
        JPanel bottomNavPanel = new JPanel(new GridLayout(1, 3));
        JButton profileButton = new JButton("View Profile");
        JButton appointmentsButton = new JButton("View Appointments");
        JButton logoutButton = new JButton("Logout");

        profileButton.addActionListener(e -> viewDoctorProfile());
        appointmentsButton.addActionListener(e -> viewAppointments());
        logoutButton.addActionListener(e -> logout());

        bottomNavPanel.add(profileButton);
        bottomNavPanel.add(appointmentsButton);
        bottomNavPanel.add(logoutButton);

        // Add panels to frame
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomNavPanel, BorderLayout.SOUTH);
    }

    // View doctor profile
    private void viewDoctorProfile() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT full_name, medical_field, phone_number, username FROM users WHERE username = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, doctorUsername);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String fullName = rs.getString("full_name");
                String field = rs.getString("medical_field");
                String phone = rs.getString("phone_number");

                JPanel profilePanel = new JPanel();
                profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
                profilePanel.setBackground(Color.WHITE);

                profilePanel.add(new JLabel("Full Name: " + fullName));
                profilePanel.add(new JLabel("Medical Field: " + field));
                profilePanel.add(new JLabel("Phone Number: " + phone));
                profilePanel.add(new JLabel("Username: " + doctorUsername));

                updateCenterPanel(profilePanel);
            } else {
                JOptionPane.showMessageDialog(this, "Profile not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error retrieving profile: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewAppointments() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT a.id, p.name AS patient_name, a.appointment_date, a.status " +
                           "FROM appointments a " +
                           "JOIN patients p ON a.patient_id = p.id " +
                           "WHERE a.doctor_name = ?";
            PreparedStatement pst = conn.prepareStatement(query);

            String doctorName = "Dr. Emily Jones"; 
            pst.setString(1, doctorName);

            ResultSet rs = pst.executeQuery();

            // Table model to display appointments
            DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Patient Name", "Date", "Status"}, 0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("patient_name"),
                        rs.getDate("appointment_date"),
                        rs.getString("status")
                });
            }

            JTable table = new JTable(model);
            table.setRowHeight(25);
            JScrollPane scrollPane = new JScrollPane(table);

            updateCenterPanel(scrollPane);

            // Add table row selection
            table.getSelectionModel().addListSelectionListener(event -> {
                if (!event.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                    int appointmentId = (int) table.getValueAt(table.getSelectedRow(), 0);
                    viewPatientDetails(appointmentId);
                }
            });

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error retrieving appointments: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // View patient details
    private void viewPatientDetails(int appointmentId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT p.name, p.medical_history, a.prescription " +
                    "FROM patients p JOIN appointments a ON p.id = a.patient_id WHERE a.id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, appointmentId);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                String history = rs.getString("medical_history");
                String prescription = rs.getString("prescription");

                JTextArea historyArea = new JTextArea(history);
                historyArea.setEditable(false);

                JTextField prescriptionField = new JTextField(prescription);

                JButton saveButton = new JButton("Save Prescription");
                saveButton.addActionListener(e -> {
                    // Open connection inside the action listener
                    try (Connection conn1 = DatabaseConnection.getConnection()) {
                        String updateQuery = "UPDATE appointments SET prescription = ? WHERE id = ?";
                        PreparedStatement updatePst = conn1.prepareStatement(updateQuery);
                        updatePst.setString(1, prescriptionField.getText());
                        updatePst.setInt(2, appointmentId);
                        updatePst.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Prescription updated.");
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Error updating prescription: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });

                JPanel patientPanel = new JPanel();
                patientPanel.setLayout(new BoxLayout(patientPanel, BoxLayout.Y_AXIS));
                patientPanel.add(new JLabel("Patient Name: " + name));
                patientPanel.add(new JLabel("Medical History:"));
                patientPanel.add(new JScrollPane(historyArea));
                patientPanel.add(new JLabel("Prescription:"));
                patientPanel.add(prescriptionField);
                patientPanel.add(saveButton);

                updateCenterPanel(patientPanel);
            } else {
                JOptionPane.showMessageDialog(this, "Patient details not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error retrieving patient details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Logout
    private void logout() {
        JOptionPane.showMessageDialog(this, "Logged out successfully.", "Logout", JOptionPane.INFORMATION_MESSAGE);
        dispose();
        new LoginForm(); 
    }

    // Update center panel
    private void updateCenterPanel(JComponent component) {
        centerPanel.removeAll();
        centerPanel.add(component, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DoctorDashboard("DrJohn").setVisible(true);
        });
    }
}
