public class Process {
    private String processID;
    private int arrivalTime;
    private int initialBurstTime;
    private int remainingBurstTime;
    private int completionTime;
    private int turnaroundTime;
    private int waitingTime;
    private boolean isFinished;
    private int priority;

    public Process(String processID, int arrivalTime, int initialBurstTime, int priority) {
        this.processID = processID;
        this.arrivalTime = arrivalTime;
        this.initialBurstTime = initialBurstTime;
        this.priority = priority;
        this.remainingBurstTime = initialBurstTime;
        this.completionTime = 0;
        this.turnaroundTime = 0;
        this.waitingTime = 0;
        this.isFinished = false;
    }

    public String getProcessID() {
        return processID;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getInitialBurstTime() {
        return initialBurstTime;
    }

    public int getRemainingBurstTime() {
        return remainingBurstTime;
    }

    public int getCompletionTime() {
        return completionTime;
    }

    public int getTurnaroundTime() {
        return turnaroundTime;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public int getPriority() {
        return priority;
    }

    public void reduceRemainingBurstTime(int quantum) {
        remainingBurstTime -= quantum;
        if (remainingBurstTime < 0) remainingBurstTime = 0;
    }

    public void setCompletionTime(int time) {
        completionTime = time;
    }

    public void calculateTurnaroundTime() {
        turnaroundTime = completionTime - arrivalTime;
    }

    public void calculateWaitingTime() {
        waitingTime = turnaroundTime - initialBurstTime;
    }

    public void markAsFinished() {
        isFinished = true;
    }
}