package com.company;

import javax.swing.*;

public class Attendance {
    private JFrame frame;

    public Attendance() {
        frame = new JFrame("Attendance System");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel label = new JLabel("Welcome to Attendance System");
        frame.add(label);

        frame.setVisible(true);
    }
}
