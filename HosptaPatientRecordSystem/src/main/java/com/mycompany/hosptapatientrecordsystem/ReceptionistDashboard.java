package com.mycompany.hosptapatientrecordsystem;


import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JDateChooser;

public class ReceptionistDashboard extends JFrame {

    private JTextField txtSearch;
    private JButton btnSearch, btnAddPatient, btnEditPatient, btnDeletePatient, btnBookAppointment, btnTrackAppointments;
    private JTable patientTable;
    private DefaultTableModel tableModel;

    private JButton btnViewPrescriptions, btnTransferPatient;

    public ReceptionistDashboard() {
        setTitle("Receptionist Dashboard");
        setSize(1400, 800);  
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Components
        JLabel label = new JLabel("Welcome to Receptionist Dashboard!");
        label.setFont(new Font("Arial", Font.BOLD, 16));
        txtSearch = new JTextField(20);
        btnSearch = new JButton("Search");
        btnAddPatient = new JButton("Add Patient");
        btnEditPatient = new JButton("Edit Patient");
        btnDeletePatient = new JButton("Delete Patient");
        btnBookAppointment = new JButton("Book Appointment");
        btnTrackAppointments = new JButton("Track Appointments");
        btnViewPrescriptions = new JButton("View Prescriptions");
        btnTransferPatient = new JButton("Transfer Patient");

        tableModel = new DefaultTableModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("Name");
        tableModel.addColumn("Age");
        tableModel.addColumn("Phone");
        tableModel.addColumn("Medical History");
        patientTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(patientTable);

      
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(label);
        topPanel.add(txtSearch);
        topPanel.add(btnSearch);
        topPanel.add(btnAddPatient);
        topPanel.add(btnEditPatient);
        topPanel.add(btnDeletePatient);
        topPanel.add(btnBookAppointment);
        topPanel.add(btnTrackAppointments);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(btnViewPrescriptions);
        bottomPanel.add(btnTransferPatient);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Action listeners
        btnSearch.addActionListener(e -> searchPatient());
        btnAddPatient.addActionListener(e -> addPatient());
        btnEditPatient.addActionListener(e -> editPatient());
        btnDeletePatient.addActionListener(e -> deletePatient());
        btnBookAppointment.addActionListener(e -> bookAppointment());
        btnTrackAppointments.addActionListener(e -> trackAppointments());
        btnViewPrescriptions.addActionListener(e -> viewPrescriptions());
        btnTransferPatient.addActionListener(e -> transferPatient());

        loadAllPatients();
    }

    private void loadAllPatients() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM patients";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getString("phone"),
                    rs.getString("medical_history")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading patients: " + e.getMessage());
        }
    }

    private void searchPatient() {
        String searchQuery = txtSearch.getText();
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM patients WHERE name LIKE ? OR id = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, "%" + searchQuery + "%");
            pst.setString(2, searchQuery);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getString("phone"),
                    rs.getString("medical_history")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error searching for patient: " + e.getMessage());
        }
    }

    private void addPatient() {
        JTextField nameField = new JTextField();
        JTextField ageField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextArea historyField = new JTextArea(3, 20);

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Age:"));
        panel.add(ageField);
        panel.add(new JLabel("Phone:"));
        panel.add(phoneField);
        panel.add(new JLabel("Medical History:"));
        panel.add(new JScrollPane(historyField));

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Patient", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO patients (name, age, phone, medical_history) VALUES (?, ?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, nameField.getText());
                pst.setInt(2, Integer.parseInt(ageField.getText()));
                pst.setString(3, phoneField.getText());
                pst.setString(4, historyField.getText());
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Patient added successfully!");
                loadAllPatients();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void viewPrescriptions() {
    int selectedRow = patientTable.getSelectedRow();

    // Ensure a patient is selected
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a patient to view prescriptions.");
        return;
    }

    int patientId = (int) patientTable.getValueAt(selectedRow, 0);
    StringBuilder prescriptionsText = new StringBuilder("Prescriptions:\n\n");

    try (Connection conn = DatabaseConnection.getConnection()) {
        String sql = "SELECT id, appointment_date, doctor_name, prescription " +
                     "FROM appointments WHERE patient_id = ?";

        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, patientId);

            try (ResultSet rs = pst.executeQuery()) {
                boolean hasPrescriptions = false;

                // Loop through the result set and append details to the text
                while (rs.next()) {
                    hasPrescriptions = true;
                    prescriptionsText.append("Appointment ID: ").append(rs.getInt("id")).append("\n")
                        .append("Date: ").append(rs.getDate("appointment_date")).append("\n")
                        .append("Doctor: ").append(rs.getString("doctor_name")).append("\n")
                        .append("Prescription: ").append(rs.getString("prescription")).append("\n\n");
                }

                if (!hasPrescriptions) {
                    prescriptionsText.append("No prescriptions found for the selected patient.");
                }
            }
        }

        // Create a JTextArea for displaying prescriptions
        JTextArea textArea = new JTextArea(prescriptionsText.toString());
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

 
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300)); 


        JOptionPane.showMessageDialog(this, scrollPane, "Prescriptions", JOptionPane.INFORMATION_MESSAGE);

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, 
            "Error fetching prescriptions: " + e.getMessage(), 
            "Database Error", 
            JOptionPane.ERROR_MESSAGE
        );
    }
}


private void transferPatient() {
    int selectedRow = patientTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a patient to transfer.");
        return;
    }

    int patientId = (int) patientTable.getValueAt(selectedRow, 0);
    String newHospital = JOptionPane.showInputDialog(this, 
        "Enter the name of the hospital to transfer the patient:");

    if (newHospital != null && !newHospital.trim().isEmpty()) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE patients SET transferred_to = ? WHERE id = ?";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setString(1, newHospital.trim());
                pst.setInt(2, patientId);
                int rowsAffected = pst.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Patient transferred successfully to " + newHospital + ".");
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "No patient found with the selected ID.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error transferring patient: " + e.getMessage(),
                                          "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    } else {
        JOptionPane.showMessageDialog(this, "Invalid hospital name.", "Input Error", JOptionPane.WARNING_MESSAGE);
    }
}

    
    private void editPatient() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a patient to edit.");
            return;
        }

        int patientId = (int) patientTable.getValueAt(selectedRow, 0);
        JTextField nameField = new JTextField((String) patientTable.getValueAt(selectedRow, 1));
        JTextField ageField = new JTextField(patientTable.getValueAt(selectedRow, 2).toString());
        JTextField phoneField = new JTextField((String) patientTable.getValueAt(selectedRow, 3));
        JTextArea historyField = new JTextArea((String) patientTable.getValueAt(selectedRow, 4));

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Age:"));
        panel.add(ageField);
        panel.add(new JLabel("Phone:"));
        panel.add(phoneField);
        panel.add(new JLabel("Medical History:"));
        panel.add(new JScrollPane(historyField));

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Patient", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE patients SET name = ?, age = ?, phone = ?, medical_history = ? WHERE id = ?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, nameField.getText());
                pst.setInt(2, Integer.parseInt(ageField.getText()));
                pst.setString(3, phoneField.getText());
                pst.setString(4, historyField.getText());
                pst.setInt(5, patientId);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Patient updated successfully!");
                loadAllPatients();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void deletePatient() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a patient to delete.");
            return;
        }

        int patientId = (int) patientTable.getValueAt(selectedRow, 0);
        int result = JOptionPane.showConfirmDialog(this, "Are you sure?", "Delete Patient", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM patients WHERE id = ?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, patientId);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Patient deleted successfully!");
                loadAllPatients();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void bookAppointment() {
   
    int selectedRow = patientTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a patient first.");
        return;
    }

    int patientId = (int) patientTable.getValueAt(selectedRow, 0);

    JDateChooser dateChooser = new JDateChooser();  
    JComboBox<String> doctorComboBox = new JComboBox<>();  
    JTextArea reasonField = new JTextArea(3, 20);

 
    try (Connection conn = DatabaseConnection.getConnection()) {
        String sql = "SELECT full_name FROM users WHERE role = 'Doctor'";
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            doctorComboBox.addItem(rs.getString("full_name"));
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error fetching doctors: " + e.getMessage());
    }

 
    JPanel panel = new JPanel(new GridLayout(4, 2));
    panel.add(new JLabel("Appointment Date:"));
    panel.add(dateChooser);
    panel.add(new JLabel("Doctor:"));
    panel.add(doctorComboBox);
    panel.add(new JLabel("Reason:"));
    panel.add(new JScrollPane(reasonField));


    int result = JOptionPane.showConfirmDialog(this, panel, "Book Appointment", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
        // Geting selected date (will return Date object)
        java.util.Date appointmentDate = dateChooser.getDate();
        String doctorName = (String) doctorComboBox.getSelectedItem(); 
        String reason = reasonField.getText();

        // Validating input
        if (appointmentDate == null || doctorName.isEmpty() || reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = sdf.format(appointmentDate);

       
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO appointments (patient_id, appointment_date, doctor_name, reason) VALUES (?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, patientId);
            pst.setString(2, formattedDate);  
            pst.setString(3, doctorName);
            pst.setString(4, reason);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Appointment booked successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error booking appointment: " + e.getMessage());
        }
    }
}


private void trackAppointments() {
    // Creating a model for the table to display appointments
    DefaultTableModel model = new DefaultTableModel();
    model.addColumn("Patient Name");
    model.addColumn("Doctor Name");
    model.addColumn("Appointment Date");
    model.addColumn("Reason");

   
    try (Connection conn = DatabaseConnection.getConnection()) {
        
        String sql = "SELECT p.name AS patient_name, a.appointment_date, a.doctor_name, a.reason " +
                     "FROM appointments a " +
                     "JOIN patients p ON a.patient_id = p.id";  
        
        PreparedStatement pst = conn.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();

        // Populating the table model with data from the result set
        while (rs.next()) {
            String patientName = rs.getString("patient_name");
            String doctorName = rs.getString("doctor_name");
            String appointmentDate = rs.getString("appointment_date"); 
            String reason = rs.getString("reason");

            model.addRow(new Object[]{patientName, doctorName, appointmentDate, reason});
        }

     
        JTable appointmentsTable = new JTable(model);

        
        appointmentsTable.setFillsViewportHeight(true);
        appointmentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

   
        JScrollPane scrollPane = new JScrollPane(appointmentsTable);

        // Show the table in a dialog or a new window
        JOptionPane.showMessageDialog(this, scrollPane, "Track Appointments", JOptionPane.INFORMATION_MESSAGE);
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error fetching appointments: " + e.getMessage());
        e.printStackTrace(); 
    }
}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReceptionistDashboard().setVisible(true));
    }
}
