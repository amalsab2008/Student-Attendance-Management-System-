import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Main {
    // A simple class to store records
    static class AttendanceRecord {
        String name;
        String subject;
        int totalClasses;
        int attended;

        AttendanceRecord(String name, String subject, int totalClasses, int attended) {
            this.name = name;
            this.subject = subject;
            this.totalClasses = totalClasses;
            this.attended = attended;
        }

        double getPercentage() {
            return (attended * 100.0) / totalClasses;
        }
    }

    // List to store records temporarily
    static ArrayList<AttendanceRecord> records = new ArrayList<>();

    public static void main(String[] args) {
        JFrame frame = new JFrame("Attendance System");
        frame.setSize(500, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(6, 2, 10, 10));

        // Labels and input fields
        JLabel nameLabel = new JLabel("Student Name:");
        JTextField nameField = new JTextField();

        JLabel subjectLabel = new JLabel("Subject:");
        String[] subjects = {"Physics", "Chemistry", "Maths"};
        JComboBox<String> subjectBox = new JComboBox<>(subjects);

        JLabel totalLabel = new JLabel("Total Classes:");
        JTextField totalField = new JTextField();

        JLabel attendedLabel = new JLabel("Classes Attended:");
        JTextField attendedField = new JTextField();

        // Buttons
        JButton addButton = new JButton("Add Record");
        JButton updateButton = new JButton("Update Record");

        // Message area
        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);

        // Add components to the window
        frame.add(nameLabel); frame.add(nameField);
        frame.add(subjectLabel); frame.add(subjectBox);
        frame.add(totalLabel); frame.add(totalField);
        frame.add(attendedLabel); frame.add(attendedField);
        frame.add(addButton); frame.add(updateButton);
        frame.add(new JLabel("Records:")); frame.add(new JScrollPane(outputArea));

        // Add Record button functionality
        addButton.addActionListener(e -> {
            try {
                String name = nameField.getText();
                String subject = subjectBox.getSelectedItem().toString();
                int total = Integer.parseInt(totalField.getText());
                int attended = Integer.parseInt(attendedField.getText());

                AttendanceRecord record = new AttendanceRecord(name, subject, total, attended);
                records.add(record);

                outputArea.append(name + " | " + subject + " | " + record.getPercentage() + "%\n");

                JOptionPane.showMessageDialog(frame, "Record Added Successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Please enter valid input!");
            }
        });

        // Update Record button functionality
        updateButton.addActionListener(e -> {
            try {
                String name = nameField.getText();
                String subject = subjectBox.getSelectedItem().toString();
                int total = Integer.parseInt(totalField.getText());
                int attended = Integer.parseInt(attendedField.getText());

                boolean found = false;
                for (AttendanceRecord r : records) {
                    if (r.name.equalsIgnoreCase(name) && r.subject.equals(subject)) {
                        r.totalClasses = total;
                        r.attended = attended;
                        found = true;
                        JOptionPane.showMessageDialog(frame, "Record Updated!");
                    }
                }

                if (!found) {
                    JOptionPane.showMessageDialog(frame, "No record found for update!");
                }

                // Refresh output
                outputArea.setText("");
                for (AttendanceRecord r : records) {
                    outputArea.append(r.name + " | " + r.subject + " | " + r.getPercentage() + "%\n");
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Invalid input!");
            }
        });

        frame.setVisible(true);
    }
}



