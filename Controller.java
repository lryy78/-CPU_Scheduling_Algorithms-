import java.util.*;

public class Controller {
    private final List<Process> processes;
    private final int quantumTime;
    private int totalTurnaroundTime = 0;
    private int totalWaitingTime = 0;
    private double averageTurnaroundTime = 0.0;
    private double averageWaitingTime = 0.0;
    private final Queue<GanttChartEntry> ganttChartQueue = new LinkedList<>();

    public Controller(List<Process> processes, int quantumTime) {
        this.processes = processes;
        this.quantumTime = quantumTime;
    }

    public void executeRoundRobin() {
        int currentTime = 0;
        processes.sort(Comparator.comparingInt(Process::getArrivalTime));
        Queue<Process> readyQueue = new LinkedList<>();
        Set<String> inQueue = new HashSet<>();
    
        while (!allProcessesFinished()) {
            // Add processes to the ready queue that have arrived by the current time
            for (Process process : processes) {
                if (process.getArrivalTime() <= currentTime && !process.isFinished() && !inQueue.contains(process.getProcessID())) {
                    readyQueue.add(process);
                    inQueue.add(process.getProcessID());
                }
            }
    
            if (!readyQueue.isEmpty()) {
                Process process = readyQueue.poll();
                int timeSlice = Math.min(quantumTime, process.getRemainingBurstTime());
                ganttChartQueue.add(new GanttChartEntry(String.valueOf(process.getProcessID()), currentTime, timeSlice));
    
                currentTime += timeSlice;
                process.reduceRemainingBurstTime(timeSlice);
    
                // Re-add processes that arrived during the current time slice
                for (Process p : processes) {
                    if (p.getArrivalTime() <= currentTime && !p.isFinished() && !inQueue.contains(p.getProcessID())) {
                        readyQueue.add(p);
                        inQueue.add(p.getProcessID());
                    }
                }
    
                if (process.getRemainingBurstTime() == 0) {
                    process.setCompletionTime(currentTime);
                    process.markAsFinished();
                    //System.out.println(process.getProcessID() + " = " + process.getCompletionTime());
                } else {
                    readyQueue.add(process);
                }
            } else {
                // Handle idle time
                final int finalCurrentTime = currentTime;
                int nextArrivalTime = processes.stream()
                        .filter(p -> !p.isFinished() && p.getArrivalTime() > finalCurrentTime)
                        .mapToInt(Process::getArrivalTime)
                        .min()
                        .orElse(Integer.MAX_VALUE);
    
                if (nextArrivalTime != Integer.MAX_VALUE) {
                    int idleTime = nextArrivalTime - currentTime;
                    ganttChartQueue.add(new GanttChartEntry("IDLE", currentTime, idleTime));
                    currentTime = nextArrivalTime;
                }
            }
        }
    
        calculateMetrics();
    }

    public void executeNonPreemptivePriority() {
        int currentTime = 0;
        processes.sort(Comparator.comparingInt(Process::getArrivalTime));

        while (!allProcessesFinished()) {
            Process nextProcess = null;
            for (Process process : processes) {
                if (process.getArrivalTime() <= currentTime && !process.isFinished()) {
                    if (nextProcess == null || process.getPriority() < nextProcess.getPriority()) {
                        nextProcess = process;
                    }
                }
            }

            if (nextProcess != null) {
                int timeSlice = nextProcess.getRemainingBurstTime();
                ganttChartQueue.add(new GanttChartEntry(String.valueOf(nextProcess.getProcessID()), currentTime, timeSlice));

                currentTime += timeSlice;
                nextProcess.setCompletionTime(currentTime);
                nextProcess.markAsFinished();
            } else {
                ganttChartQueue.add(new GanttChartEntry("IDLE", currentTime, 1));
                currentTime++;
            }
        }

        calculateMetrics();
    }

    public void executeNonPreemptiveSJF() {
        int currentTime = 0;
        processes.sort(Comparator.comparingInt(Process::getArrivalTime));

        while (!allProcessesFinished()) {
            Process shortestJob = null;
            for (Process process : processes) {
                if (process.getArrivalTime() <= currentTime && !process.isFinished()) {
                    if (shortestJob == null || process.getInitialBurstTime() < shortestJob.getInitialBurstTime()) {
                        shortestJob = process;
                    }
                }
            }

            if (shortestJob != null) {
                int timeSlice = shortestJob.getInitialBurstTime();
                ganttChartQueue.add(new GanttChartEntry(String.valueOf(shortestJob.getProcessID()), currentTime, timeSlice));

                currentTime += timeSlice;
                shortestJob.setCompletionTime(currentTime);
                shortestJob.markAsFinished();
            } else {
                ganttChartQueue.add(new GanttChartEntry("IDLE", currentTime, 1));
                currentTime++;
            }
        }

        calculateMetrics();
    }

    public void executePreemptivePriority() {
        int currentTime = 0;
        GanttChartEntry currentEntry = null;
    
        while (!allProcessesFinished()) {
            Process highestPriorityProcess = null;
    
            // Find the highest priority process that is ready to execute
            for (Process process : processes) {
                if (process.getArrivalTime() <= currentTime && !process.isFinished()) {
                    if (highestPriorityProcess == null || 
                        process.getPriority() < highestPriorityProcess.getPriority() ||
                        (process.getPriority() == highestPriorityProcess.getPriority() && process.getArrivalTime() < highestPriorityProcess.getArrivalTime())) {
                        highestPriorityProcess = process;
                    }
                }
            }
    
            if (highestPriorityProcess != null) {
                // If a new process is selected or there's no current entry, handle the Gantt chart entry
                if (currentEntry == null || !currentEntry.processID.equals(String.valueOf(highestPriorityProcess.getProcessID()))) {
                    if (currentEntry != null) {
                        ganttChartQueue.add(currentEntry);
                    }
                    currentEntry = new GanttChartEntry(String.valueOf(highestPriorityProcess.getProcessID()), currentTime, 0);
                }
    
                currentEntry.duration++;
                highestPriorityProcess.reduceRemainingBurstTime(1);
                currentTime++;
    
                if (highestPriorityProcess.getRemainingBurstTime() == 0) {
                    highestPriorityProcess.setCompletionTime(currentTime);
                    highestPriorityProcess.markAsFinished();
                    ganttChartQueue.add(currentEntry);
                    currentEntry = null; // Reset current entry since the process is finished
                }
            } else {
                // Handle idle time
                if (currentEntry == null || !currentEntry.processID.equals("IDLE")) {
                    if (currentEntry != null) {
                        ganttChartQueue.add(currentEntry);
                    }
                    currentEntry = new GanttChartEntry("IDLE", currentTime, 0);
                }
    
                currentEntry.duration++;
                currentTime++;
            }
        }
    
        // Add the last Gantt chart entry if it exists
        if (currentEntry != null) {
            ganttChartQueue.add(currentEntry);
        }
    
        calculateMetrics();
    }
    

    public void executePreemptiveSJF() {
        int currentTime = 0;
        GanttChartEntry currentEntry = null;
    
        while (!allProcessesFinished()) {
            Process shortestJob = null;
            for (Process process : processes) {
                if (process.getArrivalTime() <= currentTime && !process.isFinished()) {
                    if (shortestJob == null || 
                        process.getRemainingBurstTime() < shortestJob.getRemainingBurstTime() ||
                        (process.getRemainingBurstTime() == shortestJob.getRemainingBurstTime() && 
                         process.getArrivalTime() < shortestJob.getArrivalTime())) { // FCFS tie-breaker
                        shortestJob = process;
                    }
                }
            }
    
            if (shortestJob != null) {
                if (currentEntry == null || !currentEntry.processID.equals(String.valueOf(shortestJob.getProcessID()))) {
                    // If a new process starts or there's no current entry, create a new Gantt chart entry.
                    if (currentEntry != null) {
                        ganttChartQueue.add(currentEntry);
                    }
                    currentEntry = new GanttChartEntry(String.valueOf(shortestJob.getProcessID()), currentTime, 0);
                }
    
                currentEntry.duration++;
                shortestJob.reduceRemainingBurstTime(1);
                currentTime++;
    
                if (shortestJob.getRemainingBurstTime() == 0) {
                    shortestJob.setCompletionTime(currentTime);
                    shortestJob.markAsFinished();
                }
            } else {
                // Handle idle time
                if (currentEntry == null || !currentEntry.processID.equals("IDLE")) {
                    if (currentEntry != null) {
                        ganttChartQueue.add(currentEntry);
                    }
                    currentEntry = new GanttChartEntry("IDLE", currentTime, 0);
                }
    
                currentEntry.duration++;
                currentTime++;
            }
        }
    
        // Add the last entry if it exists
        if (currentEntry != null) {
            ganttChartQueue.add(currentEntry);
        }
    
        calculateMetrics();
    }
    

    private boolean allProcessesFinished() {
        for (Process process : processes) {
            if (!process.isFinished()) {
                return false;
            }
        }
        return true;
    }

    private void calculateMetrics() {
        for (Process process : processes) {
            process.calculateTurnaroundTime();
            process.calculateWaitingTime();
            totalTurnaroundTime += process.getTurnaroundTime();
            totalWaitingTime += process.getWaitingTime();
        }

        averageTurnaroundTime = (double) totalTurnaroundTime / processes.size();
        averageWaitingTime = (double) totalWaitingTime / processes.size();
    }

    public Queue<GanttChartEntry> getGanttChartData() {
        return ganttChartQueue;
    }

    public int getTotalTurnaroundTime() {
        return totalTurnaroundTime;
    }

    public double getAverageTurnaroundTime() {
        return averageTurnaroundTime;
    }

    public int getTotalWaitingTime() {
        return totalWaitingTime;
    }

    public double getAverageWaitingTime() {
        return averageWaitingTime;
    }

    public static class GanttChartEntry {
        public String processID;
        public int startTime;
        public int duration;

        public GanttChartEntry(String processID, int startTime, int duration) {
            this.processID = processID;
            this.startTime = startTime;
            this.duration = duration;
        }
    }
}