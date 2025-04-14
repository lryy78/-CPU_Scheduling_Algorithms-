import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;


public class MainWindow extends JFrame {
    private JTable processTable;
    private JButton addButton, deleteButton, resetButton, calculateButton, clearAllButton;
    private JTextField timeQuantumInput;
    private JComboBox<String> algorithmComboBox;
    private JTextField totalTurnaroundTimeOutput, averageTurnaroundTimeOutput, totalWaitingTimeOutput, averageWaitingTimeOutput;
    private final JPanel ganttChartPanel;

    public MainWindow() {
        setTitle("CPU Scheduling");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(20, 20));

        // Initialize components
        initializeInputFields();
        initializeTable();
        initializeButtons();
        initializeOutputFields();

        // Input panel
        JPanel inputPanel = createPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        inputPanel.add(new JLabel("Enter Time Quantum: "));
        inputPanel.add(timeQuantumInput);
        inputPanel.add(new JLabel("Select Algorithm: "));
        inputPanel.add(algorithmComboBox);
        inputPanel.add(calculateButton);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buttonPanel.add(addButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(deleteButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(resetButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(clearAllButton);

        // Table scroll pane
        JScrollPane tableScrollPane = new JScrollPane(processTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        tableScrollPane.setPreferredSize(new Dimension(800, 220));

        // Table and button panel
        JPanel tableAndButtonPanel = new JPanel(new BorderLayout(10, 10));
        tableAndButtonPanel.add(tableScrollPane, BorderLayout.CENTER);
        tableAndButtonPanel.add(buttonPanel, BorderLayout.EAST);

        // Output panel
        JPanel outputPanel = createPanel(new GridLayout(2, 4, 10, 10));
        outputPanel.add(new JLabel("Total Turnaround Time: ", SwingConstants.RIGHT));
        outputPanel.add(totalTurnaroundTimeOutput);
        outputPanel.add(new JLabel("Average Turnaround Time: ", SwingConstants.RIGHT));
        outputPanel.add(averageTurnaroundTimeOutput);
        outputPanel.add(new JLabel("Total Waiting Time: ", SwingConstants.RIGHT));
        outputPanel.add(totalWaitingTimeOutput);
        outputPanel.add(new JLabel("Average Waiting Time: ", SwingConstants.RIGHT));
        outputPanel.add(averageWaitingTimeOutput);

        // Gantt Chart Panel
        ganttChartPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
        ganttChartPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JLabel ganttChartLabel = new JLabel("Gantt Chart", SwingConstants.LEFT);
        ganttChartLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        ganttChartLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Gantt Chart Scroll Pane with the label at the top
        JScrollPane ganttChartScrollPane = new JScrollPane(ganttChartPanel);
        ganttChartScrollPane.setPreferredSize(new Dimension(1150, 170));
        ganttChartScrollPane.setColumnHeaderView(ganttChartLabel);

        // Center panel (table, Gantt chart, and output)
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(tableAndButtonPanel, BorderLayout.NORTH);
        centerPanel.add(ganttChartScrollPane, BorderLayout.CENTER);
        centerPanel.add(outputPanel, BorderLayout.SOUTH);

        // Add components to the frame
        add(inputPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        // Finalize window
        pack();
        setSize(850, 610);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    // Helper method to create a panel with padding
    private JPanel createPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return panel;
    }

    private void initializeTable() {
        String[] columnNames = {
            "Process ID", "Arrival Time", "Burst Time", 
            "Priority", "Completion Time", "Turnaround Time", "Waiting Time"
        };
        Object[][] data = new Object[3][7]; // Default to 3 rows
        // Initialize the table with empty process IDs
        for (Object[] data1 : data) {
            data1[0] = ""; // Empty Process ID
            data1[1] = ""; // Empty Arrival Time
            data1[2] = ""; // Empty Burst Time
            data1[3] = ""; // Empty Priority
            data1[4] = ""; // Empty Completion Time
            data1[5] = ""; // Empty Turnaround Time
            data1[6] = ""; // Empty Waiting Time
        }
    
        // Create a custom table model
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Allow editing for Process ID, Arrival Time, Burst Time, and Priority (if applicable)
                String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
                if (column == 0 || column == 1 || column == 2) { // Process ID, Arrival Time, Burst Time
                    return true;
                }
                if (column == 3) { // Priority column
                    return selectedAlgorithm.contains("Priority"); // Enable only for Priority-based algorithms
                }
                return false; // Other columns are non-editable
            }
        };
    
        processTable = new JTable(model);
        processTable.setRowHeight(30); // Smaller row height
        processTable.setFillsViewportHeight(true);
    
        JTableHeader header = processTable.getTableHeader();
        header.setFont(header.getFont().deriveFont(Font.BOLD));
    
        // Add an event listener to the algorithmComboBox to update the table model
        algorithmComboBox.addActionListener(e -> {
            String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
            if (selectedAlgorithm.contains("Priority")) {
                // Enable the Priority column for Priority-based algorithms
                model.fireTableStructureChanged(); // Refresh the table
            } else {
                // Disable and clear the Priority column for other algorithms
                for (int i = 0; i < model.getRowCount(); i++) {
                    model.setValueAt("", i, 3); // Clear the Priority column
                }
                model.fireTableStructureChanged(); // Refresh the table
            }
        });
    }

    private void initializeButtons() {
        addButton = new JButton("+");
        deleteButton = new JButton("-");
        resetButton = new JButton("🗑️");
        calculateButton = new JButton("Calculate");
    
        // New button for clearing all data
        clearAllButton = new JButton("⟳");

        Dimension buttonSize = new Dimension(50, 30);
        addButton.setPreferredSize(buttonSize);
        addButton.setMinimumSize(buttonSize);
        addButton.setMaximumSize(buttonSize);

        deleteButton.setPreferredSize(buttonSize);
        deleteButton.setMinimumSize(buttonSize);
        deleteButton.setMaximumSize(buttonSize);

        resetButton.setPreferredSize(buttonSize);
        resetButton.setMinimumSize(buttonSize);
        resetButton.setMaximumSize(buttonSize);

        clearAllButton.setPreferredSize(buttonSize);
        clearAllButton.setMinimumSize(buttonSize);
        clearAllButton.setMaximumSize(buttonSize);
    
        // Add action listeners
        addButton.addActionListener(e -> onAddButtonClicked());
        deleteButton.addActionListener(e -> onDeleteButtonClicked());
        resetButton.addActionListener(e -> onResetButtonClicked());
        calculateButton.addActionListener(e -> onCalculateButtonClicked());
        clearAllButton.addActionListener(e -> onClearAllButtonClicked()); 
    }

    private void onClearAllButtonClicked() {
        DefaultTableModel model = (DefaultTableModel) processTable.getModel();
    
        // Clear all editable fields in the table
        for (int i = 0; i < model.getRowCount(); i++) {
            for (int j = 0; j < model.getColumnCount(); j++) { // Start from column 1 (skip Process ID)
                model.setValueAt("", i, j);
            }
        }
    
        // Clear the Gantt chart panel
        clearGanttChartPanel();
    
        // Clear the output fields
        clearOutputFields();
    
        // Clear the time quantum input field if it's enabled
        if (timeQuantumInput.isEditable()) {
            timeQuantumInput.setText("");
        }
    }

    private void initializeInputFields() {
        timeQuantumInput = new JTextField(10);
        algorithmComboBox = new JComboBox<>(new String[] {
            "Round Robin", "Preemptive SJF", "Non Preemptive SJF", "Preemptive Priority", "Non Preemptive Priority"
        });
    
        // Add an event listener to the algorithmComboBox
        algorithmComboBox.addActionListener(e -> {
            String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
            DefaultTableModel model = (DefaultTableModel) processTable.getModel();
        
            // Clear table columns (Completion Time, Turnaround Time, Waiting Time)
            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 4; j <= 6; j++) { // Columns 4, 5, 6
                    model.setValueAt("", i, j);
                }
            }
        
            // Clear output fields
            clearOutputFields();
        
            // Clear the Gantt chart panel
            clearGanttChartPanel();
        
            // Enable/disable timeQuantumInput based on the selected algorithm
            boolean isRoundRobin = selectedAlgorithm.equals("Round Robin");
            timeQuantumInput.setEditable(isRoundRobin);
            timeQuantumInput.setFocusable(isRoundRobin);
            timeQuantumInput.setBackground(isRoundRobin ? Color.WHITE : Color.LIGHT_GRAY);
            if (!isRoundRobin) timeQuantumInput.setText("");
        
            // Do not clear Priority column values; simply refresh the table
            model.fireTableDataChanged();
        });        
    
        // Initialize timeQuantumInput based on the default selected algorithm
        String defaultAlgorithm = (String) algorithmComboBox.getSelectedItem();
        boolean isRoundRobin = defaultAlgorithm.equals("Round Robin");
        timeQuantumInput.setEditable(isRoundRobin);
        timeQuantumInput.setFocusable(isRoundRobin);
        timeQuantumInput.setBackground(isRoundRobin ? Color.WHITE : Color.LIGHT_GRAY);
        if (!isRoundRobin) timeQuantumInput.setText("");
    }
    
    // Helper method to clear output fields
    private void clearOutputFields() {
        totalTurnaroundTimeOutput.setText("0");
        averageTurnaroundTimeOutput.setText("0.0");
        totalWaitingTimeOutput.setText("0");
        averageWaitingTimeOutput.setText("0.0");
    }
    
    // Helper method to clear the Gantt chart panel
    private void clearGanttChartPanel() {
        ganttChartPanel.removeAll();
        ganttChartPanel.revalidate();
        ganttChartPanel.repaint();
    }

    private void initializeOutputFields() {
        // Array of field variables and their initial values
        JTextField[] fields = {
            totalTurnaroundTimeOutput = new JTextField("0"),
            averageTurnaroundTimeOutput = new JTextField("0.0"),
            totalWaitingTimeOutput = new JTextField("0"),
            averageWaitingTimeOutput = new JTextField("0.0")
        };
    
        // Apply common properties to each field using a for loop
        for (JTextField field : fields) {
            field.setEditable(false); // Make non-editable
            field.setFocusable(false); // Make non-clickable
            field.setHorizontalAlignment(SwingConstants.CENTER); // Center align text
        }
    }

    private void onAddButtonClicked() {
        DefaultTableModel model = (DefaultTableModel) processTable.getModel();
        model.addRow(new Object[]{"", "", "", "", "", "", ""});
    }

    private void onDeleteButtonClicked() {
        DefaultTableModel model = (DefaultTableModel) processTable.getModel();
        if (model.getRowCount() > 3) {
            model.removeRow(model.getRowCount() - 1); // Delete the last row
        } else {
            JOptionPane.showMessageDialog(this, "Cannot delete rows. Minimum 3 rows required.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onResetButtonClicked() {
        DefaultTableModel model = (DefaultTableModel) processTable.getModel();
        while (model.getRowCount() > 3) {
            model.removeRow(model.getRowCount() - 1); // Delete rows until only 3 remain
        }
    }

    private void onCalculateButtonClicked() {
        DefaultTableModel model = (DefaultTableModel) processTable.getModel();
        String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
        int timeQuantum = 0;

        // Commit any pending cell edits
        if (processTable.isEditing()) {
            processTable.getCellEditor().stopCellEditing();
        }

        // Validate and collect data
        List<Process> processes = new ArrayList<>();
        Set<String> processIDs = new HashSet<>(); // To track duplicate process IDs

        try {
            // First, check for duplicate process IDs
            for (int i = 0; i < model.getRowCount(); i++) {
                Object processIDObj = model.getValueAt(i, 0); // Column 0: Process ID
                String processIDText = processIDObj != null ? processIDObj.toString().trim() : "";

                if (processIDText.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "Process ID for row " + (i + 1) + " cannot be empty.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check for duplicate process IDs
                if (processIDs.contains(processIDText)) {
                    JOptionPane.showMessageDialog(this,
                        "Duplicate Process ID found: " + processIDText + ". Process IDs must be unique.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                processIDs.add(processIDText); // Add the process ID to the set
            }

            // If no duplicates, proceed with the rest of the validation and calculation
            if (selectedAlgorithm.equals("Round Robin")) {
                String timeQuantumText = timeQuantumInput.getText().trim();
                if (timeQuantumText.contains(" ") || timeQuantumText.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "Time Quantum must be a single positive number without spaces.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                timeQuantum = Integer.parseInt(timeQuantumText);
                if (timeQuantum <= 0) {
                    JOptionPane.showMessageDialog(this,
                        "Time Quantum must be positive.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            for (int i = 0; i < model.getRowCount(); i++) {
                // Validate Process ID (already checked for duplicates above)
                Object processIDObj = model.getValueAt(i, 0); // Column 0: Process ID
                String processIDText = processIDObj != null ? processIDObj.toString().trim() : "";

                // Validate Arrival Time
                Object arrivalTimeObj = model.getValueAt(i, 1); // Column 1: Arrival Time
                String arrivalTimeText = arrivalTimeObj != null ? arrivalTimeObj.toString().trim() : "";

                if (arrivalTimeText.contains(" ") || arrivalTimeText.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "Arrival Time for Process ID " + processIDText + " must be a single positive number without spaces.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int arrivalTime = Integer.parseInt(arrivalTimeText);

                // Validate Burst Time
                Object burstTimeObj = model.getValueAt(i, 2); // Column 2: Burst Time
                String burstTimeText = burstTimeObj != null ? burstTimeObj.toString().trim() : "";

                if (burstTimeText.contains(" ") || burstTimeText.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "Burst Time for Process ID " + processIDText + " must be a single positive number without spaces.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int burstTime = Integer.parseInt(burstTimeText);

                // Validate Priority if applicable
                int priority = 0; // Default priority
                if (selectedAlgorithm.contains("Priority")) {
                    Object priorityObj = model.getValueAt(i, 3); // Column 3: Priority
                    String priorityText = priorityObj != null ? priorityObj.toString().trim() : "";

                    if (priorityText.contains(" ") || priorityText.isEmpty()) {
                        JOptionPane.showMessageDialog(this,
                            "Priority for Process ID " + processIDText + " must be a single number without spaces.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    priority = Integer.parseInt(priorityText);
                }

                processes.add(new Process(processIDText, arrivalTime, burstTime, priority));
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Invalid input values. Please check your entries.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Perform scheduling calculation
        Controller controller = new Controller(processes, timeQuantum);

        switch (selectedAlgorithm) {
            case "Round Robin" -> controller.executeRoundRobin();
            case "Preemptive SJF" -> controller.executePreemptiveSJF();
            case "Non Preemptive SJF" -> controller.executeNonPreemptiveSJF();
            case "Preemptive Priority" -> controller.executePreemptivePriority();
            case "Non Preemptive Priority" -> controller.executeNonPreemptivePriority();
        }

        // Update the table with results
        for (int i = 0; i < model.getRowCount(); i++) {
            // Get the Process ID from the table
            Object processIDObj = model.getValueAt(i, 0); // Column 0: Process ID
            String processIDText = processIDObj != null ? processIDObj.toString().trim() : "";
        
            // Find the corresponding process in the processes list
            for (Process process : processes) {
                if (process.getProcessID().equals(processIDText)) {
                    // Update the table row with the correct process data
                    model.setValueAt(process.getCompletionTime(), i, 4); // Column 4: Completion Time
                    model.setValueAt(process.getTurnaroundTime(), i, 5); // Column 5: Turnaround Time
                    model.setValueAt(process.getWaitingTime(), i, 6);    // Column 6: Waiting Time
                    break; // Exit the inner loop once the process is found
                }
            }
        }
        
        // Refresh the table model to reflect changes
        model.fireTableDataChanged();

        // Update output fields
        totalTurnaroundTimeOutput.setText(String.valueOf(controller.getTotalTurnaroundTime()));
        averageTurnaroundTimeOutput.setText(String.format("%.2f", controller.getAverageTurnaroundTime()));
        totalWaitingTimeOutput.setText(String.valueOf(controller.getTotalWaitingTime()));
        averageWaitingTimeOutput.setText(String.format("%.2f", controller.getAverageWaitingTime()));

        // Display Gantt Chart
        ganttChartPanel.removeAll(); // Clear previous Gantt chart
        GanttChart ganttChart = new GanttChart();
        ganttChart.ganttChart(ganttChartPanel, controller); // Draw the Gantt chart
        ganttChartPanel.revalidate(); // Refresh the panel
        ganttChartPanel.repaint(); // Repaint the panel
    }
}