import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.Queue;
import javax.swing.*;

public class GanttChart {
    public void ganttChart(JPanel panel, Controller controller) {
        Queue<Controller.GanttChartEntry> ganttChartQueue = controller.getGanttChartData();
        panel.removeAll(); // Clear previous Gantt chart
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0,3)); // Increase horizontal gap to 20 pixels

        int currentTime = 0; // Track the current time for calculating end time
        int totalEntries = ganttChartQueue.size(); // Get total number of entries
        int entryIndex = 0; // Initialize entry index

        while (!ganttChartQueue.isEmpty()) {
            Controller.GanttChartEntry entry = ganttChartQueue.poll();
            String processName = entry.processID.equals("IDLE") ? "Idle" : entry.processID;

            // Create a bar for the process
            JPanel barPanel = new JPanel();
            barPanel.setLayout(new BorderLayout());
            barPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            barPanel.setBackground(Color.WHITE);
    
            // Add process name label inside the bar
            JLabel processLabel = new JLabel(processName, SwingConstants.CENTER);
            processLabel.setFont(new Font("Arial", Font.BOLD, 12));
            barPanel.add(processLabel, BorderLayout.CENTER);
    
            // Calculate the width based on the process name length
            Font font = processLabel.getFont();
            FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
            int textWidth = (int) font.getStringBounds(processName, frc).getWidth();
            int padding = 20; // Add some padding to avoid cutting off text
            int barWidth = Math.max(textWidth + padding, entry.duration * 40); // Use the larger of the two values
    
            // Set the preferred size of the bar panel
            barPanel.setPreferredSize(new Dimension(barWidth, 50));
    
            // Create a container for the bar and time label
            JPanel containerPanel = new JPanel();
            containerPanel.setLayout(new BorderLayout());
    
            // Add the bar to the container
            containerPanel.add(barPanel, BorderLayout.CENTER);

            // Add padding below the bar
            JPanel paddingPanel = new JPanel();
            paddingPanel.setPreferredSize(new Dimension(entry.duration * 40, 30)); // Increase padding height to 30 pixels
            paddingPanel.setLayout(new BorderLayout()); // Use BorderLayout to center the time label
            containerPanel.add(paddingPanel, BorderLayout.SOUTH);

            // Add start time label at the bottom of the padding
            JLabel timeLabel = new JLabel(String.valueOf(entry.startTime), SwingConstants.CENTER);
            timeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            paddingPanel.add(timeLabel, BorderLayout.WEST); // Place time label at the bottom of the padding
            
            // Update current time for the end time calculation
            currentTime += entry.duration; // Increment current time by the duration of the current entry

            // Check if this is the last entry
            if (entryIndex == totalEntries - 1) {
                // Add end time label at the bottom of the padding
                JLabel endTimeLabel = new JLabel(String.valueOf(currentTime), SwingConstants.CENTER);
                endTimeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                paddingPanel.add(endTimeLabel, BorderLayout.EAST); // Place end time label at the bottom of the padding
            }

            // Increment entry index
            entryIndex++;

            // Add the container to the Gantt chart panel
            panel.add(containerPanel);

        }

        panel.revalidate(); // Refresh the panel
        panel.repaint(); // Repaint the panel
    }
}