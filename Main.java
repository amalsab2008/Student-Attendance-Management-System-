import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class Main extends JFrame {

    private JTabbedPane tabbedPane;
    private JPanel loginPanel, studentPanel, attendancePanel, reportPanel, teacherPanel, subjectPanel, userPanel;

    // Login components
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton loginBtn;

    // Current user info
    private String currentUser;
    private String currentRole;
    private int currentUserId;
    private JLabel lblLoggedInAs;

    // Student Management Components
    private JTextField firstNameField, lastNameField, rollField, studentSearchField;
    private JButton addStudentBtn, updateStudentBtn, deleteStudentBtn, searchStudentBtn, refreshStudentBtn;
    private JTable studentTable;
    private DefaultTableModel studentTableModel;

    // Attendance Management Components
    private JComboBox<String> subjectComboBox;
    private JSpinner dateSpinner;
    private JTable attendanceTable;
    private JButton markAttendanceBtn;
    private DefaultTableModel attendanceTableModel;

    // Attendance Reporting Components
    private JTextField reportStudentRollField;
    private JTextArea reportArea;
    private JButton generateReportBtn;
    private JComboBox<String> reportSubjectComboBox;
    private JSpinner reportFromDate, reportToDate;

    // Teacher Management Components
    private JTextField teacherFirstNameField, teacherLastNameField, teacherEmailField;
    private JButton addTeacherBtn, updateTeacherBtn, deleteTeacherBtn;
    private JTable teacherTable;
    private DefaultTableModel teacherTableModel;

    // Subject Management Components
    private JTextField subjectNameField;
    private JButton addSubjectBtn, deleteSubjectBtn;
    private JTable subjectTable;
    private DefaultTableModel subjectTableModel;

    // User Management Components
    private JComboBox<String> userRoleComboBox;
    private JTextField newUsernameField;
    private JPasswordField newPasswordField;
    private JButton createUserBtn, deleteUserBtn;
    private JTable userTable;
    private DefaultTableModel userTableModel;

    public Main() {
        setTitle("Student Attendance System");
        setSize(1100, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create login panel first
        loginPanel = createLoginPanel();
        add(loginPanel);

        setVisible(true);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Student Attendance System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(15);

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(15);

        JLabel roleLabel = new JLabel("Role:");
        roleComboBox = new JComboBox<>(new String[]{"Admin", "Teacher", "Student"});

        loginBtn = new JButton("Login");

        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        panel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(roleLabel, gbc);

        gbc.gridx = 1;
        panel.add(roleComboBox, gbc);

        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(loginBtn, gbc);

        loginBtn.addActionListener(e -> login());

        // Enter key to login
        passwordField.addActionListener(e -> login());

        return panel;
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.");
            return;
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password); // In a real application, you should hash passwords
            stmt.setString(3, role);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentUser = username;
                currentRole = rs.getString("role");
                currentUserId = rs.getInt("user_id");

                // Remove login panel and show main application
                remove(loginPanel);
                initializeMainApplication();
                lblLoggedInAs.setText("Logged in as: " + currentUser + " (" + currentRole + ")");

                JOptionPane.showMessageDialog(this, "Login successful! Welcome " + username);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username, password, or role.");
            }
        } catch (SQLException ex) {
            showError("Login", ex);
        }
    }

    private void initializeMainApplication() {
        tabbedPane = new JTabbedPane();

        // Create panels based on user role
        if ("Admin".equals(currentRole)) {
            studentPanel = createStudentPanel();
            teacherPanel = createTeacherPanel();
            subjectPanel = createSubjectPanel();
            userPanel = createUserPanel();
            attendancePanel = createAttendancePanel();
            reportPanel = createReportPanel();

            tabbedPane.addTab("Student Management", studentPanel);
            tabbedPane.addTab("Teacher Management", teacherPanel);
            tabbedPane.addTab("Subject Management", subjectPanel);
            tabbedPane.addTab("User Management", userPanel);
            tabbedPane.addTab("Attendance", attendancePanel);
            tabbedPane.addTab("Reports", reportPanel);
        } else if ("Teacher".equals(currentRole)) {
            attendancePanel = createAttendancePanel();
            reportPanel = createReportPanel();
            loadSubjects();

            tabbedPane.addTab("Attendance", attendancePanel);
            tabbedPane.addTab("Reports", reportPanel);
        } else if ("Student".equals(currentRole)) {
            reportPanel = createReportPanel();
            loadSubjects();
            // For students, automatically fill their roll number
            try (Connection conn = DatabaseManager.getConnection()) {
                String sql = "SELECT student_roll FROM students WHERE user_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, currentUserId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    reportStudentRollField.setText(rs.getString("student_roll"));
                    reportStudentRollField.setEditable(false);

                }
            } catch (SQLException ex) {
                showError("Loading student info", ex);
            }

            tabbedPane.addTab("My Attendance", reportPanel);
        }

        // Add logout button
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> logout());

// Use the class-level label
        lblLoggedInAs = new JLabel();
        logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> logout());
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.add(lblLoggedInAs);
        southPanel.add(logoutBtn);

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        // Initial load based on role
        if ("Admin".equals(currentRole)) {
            loadStudents();
            loadTeachers();
            loadSubjects(); // This is the fixed method
            loadUsers();
        }

        // Listeners for populating tables when a tab is selected
        tabbedPane.addChangeListener(e -> {
            if ("Admin".equals(currentRole)) {
                if (tabbedPane.getSelectedComponent() == studentPanel) {
                    loadStudents();
                } else if (tabbedPane.getSelectedComponent() == teacherPanel) {
                    loadTeachers();
                } else if (tabbedPane.getSelectedComponent() == subjectPanel) {
                    loadSubjects();
                } else if (tabbedPane.getSelectedComponent() == userPanel) {
                    loadUsers();
                }
            }
            if (tabbedPane.getSelectedComponent() == attendancePanel) {
                populateAttendanceTable();
            } else if (tabbedPane.getSelectedComponent() == reportPanel) {
                // The loadSubjectsForReports method is no longer needed
            }
        });

        // Row select -> fill form
        if (studentTable != null) {
            studentTable.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int r = studentTable.getSelectedRow();
                    if (r != -1) {
                        firstNameField.setText(studentTableModel.getValueAt(r, 1).toString());
                        lastNameField.setText(studentTableModel.getValueAt(r, 2).toString());
                        rollField.setText(studentTableModel.getValueAt(r, 3).toString());
                    }
                }
            });
        }

        if (teacherTable != null) {
            teacherTable.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int r = teacherTable.getSelectedRow();
                    if (r != -1) {
                        teacherFirstNameField.setText(teacherTableModel.getValueAt(r, 1).toString());
                        teacherLastNameField.setText(teacherTableModel.getValueAt(r, 2).toString());
                        teacherEmailField.setText(teacherTableModel.getValueAt(r, 3).toString());
                    }
                }
            });
        }

        if (subjectTable != null) {
            subjectTable.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int r = subjectTable.getSelectedRow();
                    if (r != -1) {
                        subjectNameField.setText(subjectTableModel.getValueAt(r, 1).toString());
                    }
                }
            });
        }
        if (userTable != null) {
            userTable.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int r = userTable.getSelectedRow();
                    if (r != -1) {
                        newUsernameField.setText(userTableModel.getValueAt(r, 1).toString());
                        userRoleComboBox.setSelectedItem(userTableModel.getValueAt(r, 2).toString());
                    }
                }
            });
        }

        revalidate();
        repaint();
    }

    private void logout() {
        currentUser = null;
        currentRole = null;
        currentUserId = -1;
        lblLoggedInAs.setText("");
        remove(tabbedPane);
        add(loginPanel);
        usernameField.setText("");
        passwordField.setText("");
        roleComboBox.setSelectedIndex(0);

        revalidate();
        repaint();
    }

    // ----------------- Panels -----------------
    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));

        firstNameField = new JTextField();
        lastNameField  = new JTextField();
        rollField      = new JTextField();

        addStudentBtn    = new JButton("Add");
        updateStudentBtn = new JButton("Update");
        deleteStudentBtn = new JButton("Delete");

        form.add(new JLabel("First Name:"));  form.add(firstNameField);
        form.add(new JLabel("Last Name:"));   form.add(lastNameField);
        form.add(new JLabel("Roll No:"));     form.add(rollField);
        form.add(addStudentBtn);              form.add(updateStudentBtn);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        studentSearchField = new JTextField(20);
        searchStudentBtn = new JButton("Search");
        refreshStudentBtn = new JButton("Refresh");
        south.add(new JLabel("Search (name or roll): "));
        south.add(studentSearchField);
        south.add(searchStudentBtn);
        south.add(refreshStudentBtn);
        south.add(deleteStudentBtn);

        studentTableModel = new DefaultTableModel(new String[]{"ID","First Name","Last Name","Roll No."}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        studentTable = new JTable(studentTableModel);

        addStudentBtn.addActionListener(e -> addStudent());
        updateStudentBtn.addActionListener(e -> updateStudent());
        deleteStudentBtn.addActionListener(e -> deleteStudent());
        searchStudentBtn.addActionListener(e -> searchStudent());
        refreshStudentBtn.addActionListener(e -> loadStudents());

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(studentTable), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createTeacherPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));

        teacherFirstNameField = new JTextField();
        teacherLastNameField  = new JTextField();
        teacherEmailField     = new JTextField();

        addTeacherBtn    = new JButton("Add");
        updateTeacherBtn = new JButton("Update");
        deleteTeacherBtn = new JButton("Delete");

        form.add(new JLabel("First Name:"));  form.add(teacherFirstNameField);
        form.add(new JLabel("Last Name:"));   form.add(teacherLastNameField);
        form.add(new JLabel("Email:"));       form.add(teacherEmailField);
        form.add(addTeacherBtn);              form.add(updateTeacherBtn);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(deleteTeacherBtn);

        teacherTableModel = new DefaultTableModel(new String[]{"ID","First Name","Last Name","Email"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        teacherTable = new JTable(teacherTableModel);

        addTeacherBtn.addActionListener(e -> addTeacher());
        updateTeacherBtn.addActionListener(e -> updateTeacher());
        deleteTeacherBtn.addActionListener(e -> deleteTeacher());

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(teacherTable), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createSubjectPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));

        subjectNameField = new JTextField();
        addSubjectBtn = new JButton("Add Subject");
        deleteSubjectBtn = new JButton("Delete Selected");

        form.add(new JLabel("Subject Name:"));  form.add(subjectNameField);
        form.add(addSubjectBtn);                 form.add(deleteSubjectBtn);

        subjectTableModel = new DefaultTableModel(new String[]{"ID", "Subject Name"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        subjectTable = new JTable(subjectTableModel);

        addSubjectBtn.addActionListener(e -> addSubject());
        deleteSubjectBtn.addActionListener(e -> deleteSubject());

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(subjectTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));

        newUsernameField = new JTextField();
        newPasswordField = new JPasswordField();
        userRoleComboBox = new JComboBox<>(new String[]{"Admin", "Teacher", "Student"});
        createUserBtn = new JButton("Create User");
        deleteUserBtn = new JButton("Delete Selected");

        form.add(new JLabel("Username:"));  form.add(newUsernameField);
        form.add(new JLabel("Password:"));  form.add(newPasswordField);
        form.add(new JLabel("Role:"));      form.add(userRoleComboBox);
        form.add(createUserBtn);             form.add(deleteUserBtn);

        userTableModel = new DefaultTableModel(new String[]{"ID", "Username", "Role"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        userTable = new JTable(userTableModel);

        createUserBtn.addActionListener(e -> createUser());
        deleteUserBtn.addActionListener(e -> deleteUser());

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

        subjectComboBox = new JComboBox<>();
        dateSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        markAttendanceBtn = new JButton("Save Attendance for this Session");

        top.add(new JLabel("Subject:")); top.add(subjectComboBox);
        top.add(new JLabel("Date:"));    top.add(dateSpinner);
        top.add(markAttendanceBtn);

        attendanceTable = new JTable(new DefaultTableModel(new String[]{"Student ID","First Name","Last Name","Roll No.","Status"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return ("Teacher".equals(currentRole) || "Admin".equals(currentRole)) && c == 4;
            }
        });

        if ("Teacher".equals(currentRole) || "Admin".equals(currentRole)) {
            JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Present","Absent"});
            attendanceTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(statusCombo));
        }

        markAttendanceBtn.addActionListener(e -> markAttendance());

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(attendanceTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));

        reportStudentRollField = new JTextField(12);
        reportSubjectComboBox  = new JComboBox<>();
        reportFromDate = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
        reportToDate   = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
        reportFromDate.setEditor(new JSpinner.DateEditor(reportFromDate, "yyyy-MM-dd"));
        reportToDate.setEditor(new JSpinner.DateEditor(reportToDate, "yyyy-MM-dd"));
        generateReportBtn = new JButton("Generate Report");

        top.add(new JLabel("Roll:"));        top.add(reportStudentRollField);
        top.add(new JLabel("Subject:"));     top.add(reportSubjectComboBox);
        top.add(new JLabel("From:"));        top.add(reportFromDate);
        top.add(new JLabel("To:"));          top.add(reportToDate);
        top.add(generateReportBtn);

        reportArea = new JTextArea();
        reportArea.setEditable(false);
        generateReportBtn.addActionListener(e -> generateReport());

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        return panel;
    }

    // --------------- Student Logic ---------------
    private void loadStudents() {
        studentTableModel.setRowCount(0);
        String sql = "SELECT * FROM students ORDER BY student_id";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                studentTableModel.addRow(new Object[]{
                        rs.getInt("student_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("student_roll")
                });
            }
        } catch (SQLException ex) {
            showError("Loading students", ex);
        }
    }

    private void addStudent() {
        String f = firstNameField.getText().trim();
        String l = lastNameField.getText().trim();
        String r = rollField.getText().trim();
        if (f.isEmpty() || l.isEmpty() || r.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required."); return;
        }
        String sql = "INSERT INTO students(first_name,last_name,student_roll) VALUES(?,?,?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, f); ps.setString(2, l); ps.setString(3, r);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Student added.");
            firstNameField.setText(""); lastNameField.setText(""); rollField.setText("");
            loadStudents();
            populateAttendanceTable();
        } catch (SQLException ex) { showError("Adding student", ex); }
    }

    private void updateStudent() {
        int row = studentTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a student."); return; }
        int id = (int) studentTableModel.getValueAt(row, 0);
        String f = firstNameField.getText().trim();
        String l = lastNameField.getText().trim();
        String r = rollField.getText().trim();
        if (f.isEmpty() || l.isEmpty() || r.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required."); return;
        }
        String sql = "UPDATE students SET first_name=?, last_name=?, student_roll=? WHERE student_id=?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, f); ps.setString(2, l); ps.setString(3, r); ps.setInt(4, id);
            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Updated.");
                loadStudents(); populateAttendanceTable();
            }
        } catch (SQLException ex) { showError("Updating student", ex); }
    }

    private void deleteStudent() {
        int row = studentTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a student."); return; }
        int id = (int) studentTableModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete this student?", "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM students WHERE student_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Deleted.");
            loadStudents(); populateAttendanceTable();
        } catch (SQLException ex) { showError("Deleting student", ex); }
    }

    private void searchStudent() {
        String q = studentSearchField.getText().trim();
        if (q.isEmpty()) { loadStudents(); return; }
        studentTableModel.setRowCount(0);
        String sql = """
                SELECT * FROM students
                WHERE first_name LIKE ? OR last_name LIKE ? OR student_roll = ?
                """;
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + q + "%");
            ps.setString(2, "%" + q + "%");
            ps.setString(3, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    studentTableModel.addRow(new Object[]{
                            rs.getInt("student_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("student_roll")
                    });
                }
            }
        } catch (SQLException ex) { showError("Searching", ex); }
    }

    // --------------- Teacher Logic ---------------
    private void loadTeachers() {
        teacherTableModel.setRowCount(0);
        String sql = "SELECT * FROM teachers ORDER BY teacher_id";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                teacherTableModel.addRow(new Object[]{
                        rs.getInt("teacher_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email")
                });
            }
        } catch (SQLException ex) {
            showError("Loading teachers", ex);
        }
    }

    private void addTeacher() {
        String f = teacherFirstNameField.getText().trim();
        String l = teacherLastNameField.getText().trim();
        String e = teacherEmailField.getText().trim();
        if (f.isEmpty() || l.isEmpty() || e.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required."); return;
        }
        String sql = "INSERT INTO teachers(first_name,last_name,email) VALUES(?,?,?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, f); ps.setString(2, l); ps.setString(3, e);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Teacher added.");
            teacherFirstNameField.setText(""); teacherLastNameField.setText(""); teacherEmailField.setText("");
            loadTeachers();
        } catch (SQLException ex) { showError("Adding teacher", ex); }
    }

    private void updateTeacher() {
        int row = teacherTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a teacher."); return; }
        int id = (int) teacherTableModel.getValueAt(row, 0);
        String f = teacherFirstNameField.getText().trim();
        String l = teacherLastNameField.getText().trim();
        String e = teacherEmailField.getText().trim();
        if (f.isEmpty() || l.isEmpty() || e.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required."); return;
        }
        String sql = "UPDATE teachers SET first_name=?, last_name=?, email=? WHERE teacher_id=?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, f); ps.setString(2, l); ps.setString(3, e); ps.setInt(4, id);
            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Updated.");
                loadTeachers();
            }
        } catch (SQLException ex) { showError("Updating teacher", ex); }
    }

    private void deleteTeacher() {
        int row = teacherTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a teacher."); return; }
        int id = (int) teacherTableModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete this teacher?", "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM teachers WHERE teacher_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Deleted.");
            loadTeachers();
        } catch (SQLException ex) { showError("Deleting teacher", ex); }
    }

    // --------------- Subject Logic ---------------
    private void loadSubjects() {
        subjectTableModel.setRowCount(0);
        // Clear combo boxes to avoid duplicates
        subjectComboBox.removeAllItems();
        reportSubjectComboBox.removeAllItems();

        String sql = "SELECT * FROM subjects ORDER BY subject_id";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String subjectName = rs.getString("subject_name");
                subjectTableModel.addRow(new Object[]{
                        rs.getInt("subject_id"),
                        subjectName
                });
                subjectComboBox.addItem(subjectName);
                reportSubjectComboBox.addItem(subjectName);
            }
        } catch (SQLException ex) {
            showError("Loading subjects", ex);
        }
    }

    private void addSubject() {
        String name = subjectNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Subject name is required."); return;
        }
        String sql = "INSERT INTO subjects(subject_name) VALUES(?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Subject added.");
            subjectNameField.setText("");
            loadSubjects();
        } catch (SQLException ex) { showError("Adding subject", ex); }
    }

    private void deleteSubject() {
        int row = subjectTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a subject."); return; }
        int id = (int) subjectTableModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete this subject?", "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM subjects WHERE subject_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Deleted.");
            loadSubjects();
        } catch (SQLException ex) { showError("Deleting subject", ex); }
    }

    // --------------- User Logic ---------------
    private void loadUsers() {
        userTableModel.setRowCount(0);
        String sql = "SELECT * FROM users ORDER BY user_id";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                userTableModel.addRow(new Object[]{
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role")
                });
            }
        } catch (SQLException ex) {
            showError("Loading users", ex);
        }
    }

    private void createUser() {
        String username = newUsernameField.getText().trim();
        String password = new String(newPasswordField.getPassword());
        String role = (String) userRoleComboBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password are required."); return;
        }

        String sql = "INSERT INTO users(username, password, role) VALUES(?, ?, ?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password); // In a real application, you should hash passwords
            ps.setString(3, role);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "User created.");
            newUsernameField.setText("");
            newPasswordField.setText("");
            loadUsers();
        } catch (SQLException ex) { showError("Creating user", ex); }
    }

    private void deleteUser() {
        int row = userTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a user."); return; }
        int id = (int) userTableModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete this user?", "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE user_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Deleted.");
            loadUsers();
        } catch (SQLException ex) { showError("Deleting user", ex); }
    }

    // --------------- Attendance ---------------
    private void populateAttendanceTable() {
        DefaultTableModel m = new DefaultTableModel(new String[]{"Student ID","First Name","Last Name","Roll No.","Status"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return ("Teacher".equals(currentRole) || "Admin".equals(currentRole)) && c == 4;
            }
        };
        attendanceTable.setModel(m);

        if ("Teacher".equals(currentRole) || "Admin".equals(currentRole)) {
            JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Present","Absent"});
            attendanceTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(statusCombo));
        }

        String sql = "SELECT student_id, first_name, last_name, student_roll FROM students ORDER BY student_id";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                m.addRow(new Object[]{
                        rs.getInt("student_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("student_roll"),
                        "Present"
                });
            }
        } catch (SQLException ex) { showError("Loading students for attendance", ex); }
    }

    private void markAttendance() {
        if (!("Teacher".equals(currentRole) || "Admin".equals(currentRole))) {
            JOptionPane.showMessageDialog(this, "You don't have permission to mark attendance.");
            return;
        }

        String subjectName = (String) subjectComboBox.getSelectedItem();
        Date d = (Date) dateSpinner.getValue();
        if (subjectName == null) { JOptionPane.showMessageDialog(this, "Add subjects first."); return; }

        String findSub = "SELECT subject_id FROM subjects WHERE subject_name=?";
        String findSession = "SELECT session_id FROM sessions WHERE session_date=? AND subject_id=?";
        String insertSession = "INSERT INTO sessions(session_date, subject_id) VALUES(?,?)";
        String upsertAttendance = """
                INSERT INTO attendance(student_id, session_id, status)
                VALUES(?,?,?)
                ON DUPLICATE KEY UPDATE status = VALUES(status)
                """;

        Connection c = null;
        try {
            c = DatabaseManager.getConnection();
            c.setAutoCommit(false);

            int subjectId;
            try (PreparedStatement ps = c.prepareStatement(findSub)) {
                ps.setString(1, subjectName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Subject not found.");
                    subjectId = rs.getInt(1);
                }
            }

            int sessionId;
            java.sql.Date sqlDate = new java.sql.Date(d.getTime());
            try (PreparedStatement ps = c.prepareStatement(findSession)) {
                ps.setDate(1, sqlDate); ps.setInt(2, subjectId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        sessionId = rs.getInt(1);
                    } else {
                        try (PreparedStatement ins = c.prepareStatement(insertSession, Statement.RETURN_GENERATED_KEYS)) {
                            ins.setDate(1, sqlDate); ins.setInt(2, subjectId);
                            ins.executeUpdate();
                            try (ResultSet g = ins.getGeneratedKeys()) {
                                g.next(); sessionId = g.getInt(1);
                            }
                        }
                    }
                }
            }

            try (PreparedStatement ps = c.prepareStatement(upsertAttendance)) {
                DefaultTableModel m = (DefaultTableModel) attendanceTable.getModel();
                for (int i = 0; i < m.getRowCount(); i++) {
                    int studentId = (int) m.getValueAt(i, 0);
                    String status = (String) m.getValueAt(i, 4);
                    ps.setInt(1, studentId);
                    ps.setInt(2, sessionId);
                    ps.setString(3, status);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            c.commit();
            JOptionPane.showMessageDialog(this, "Attendance saved for " + new SimpleDateFormat("yyyy-MM-dd").format(d) + " (" + subjectName + ")");
        } catch (SQLException ex) {
            try { if (c != null) c.rollback(); } catch (SQLException e) { e.printStackTrace(); }
            showError("Marking attendance", ex);
        } finally {
            try { if (c != null) c.setAutoCommit(true); c.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // --------------- Reporting ---------------
    private void generateReport() {
        String roll = reportStudentRollField.getText().trim();
        String subject = (String) reportSubjectComboBox.getSelectedItem();
        Date from = (Date) reportFromDate.getValue();
        Date to   = (Date) reportToDate.getValue();

        if ("Student".equals(currentRole)) {
            try (Connection c = DatabaseManager.getConnection();
                 PreparedStatement ps = c.prepareStatement("SELECT student_roll FROM students WHERE user_id = ?")) {
                ps.setInt(1, currentUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && !rs.getString("student_roll").equals(roll)) {
                        JOptionPane.showMessageDialog(this, "You can only view your own attendance.");
                        return;
                    }
                }
            } catch (SQLException ex) { showError("Checking student permission", ex); return; }
        }

        if (roll.isEmpty() || subject == null) {
            JOptionPane.showMessageDialog(this, "Enter roll and select subject."); return;
        }

        String sql = """
                SELECT s.first_name, s.last_name,
                       SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END) AS present_count,
                       COUNT(*) AS total_classes
                FROM attendance a
                JOIN students s  ON s.student_id = a.student_id
                JOIN sessions se ON se.session_id = a.session_id
                JOIN subjects sb ON sb.subject_id = se.subject_id
                WHERE s.student_roll = ?
                  AND sb.subject_name = ?
                  AND se.session_date BETWEEN ? AND ?
                GROUP BY s.student_id
                """;

        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, roll);
            ps.setString(2, subject);
            ps.setDate(3, new java.sql.Date(from.getTime()));
            ps.setDate(4, new java.sql.Date(to.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                reportArea.setText("");
                if (rs.next()) {
                    int present = rs.getInt("present_count");
                    int total   = rs.getInt("total_classes");
                    double pct = total > 0 ? (present * 100.0 / total) : 0.0;
                    String name = rs.getString("first_name") + " " + rs.getString("last_name");

                    reportArea.append("Attendance Report\n");
                    reportArea.append("-----------------\n");
                    reportArea.append("Student: " + name + " (Roll: " + roll + ")\n");
                    reportArea.append("Subject: " + subject + "\n");
                    reportArea.append("Period : " + new SimpleDateFormat("yyyy-MM-dd").format(from)
                            + " to " + new SimpleDateFormat("yyyy-MM-dd").format(to) + "\n\n");
                    reportArea.append("Total Classes : " + total + "\n");
                    reportArea.append("Present       : " + present + "\n");
                    reportArea.append(String.format("Attendance %% : %.2f%%\n", pct));
                } else {
                    reportArea.setText("No classes found for the selected filters.");
                }
            }
        } catch (SQLException ex) { showError("Generating report", ex); }
    }

    private void showError(String where, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error " + where + ":\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}



