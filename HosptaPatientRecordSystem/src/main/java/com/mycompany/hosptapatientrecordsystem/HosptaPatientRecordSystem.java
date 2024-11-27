package com.mycompany.hosptapatientrecordsystem;

import javax.swing.SwingUtilities;

public class HosptaPatientRecordSystem {

    public static void main(String[] args) {
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginForm().setVisible(true);
            }
        });
    }
}
