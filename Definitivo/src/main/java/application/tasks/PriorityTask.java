package application.tasks;

public class PriorityTask implements Runnable, Comparable<PriorityTask> {
    private final int priority;
    private final Runnable task;

    public PriorityTask(int priority, Runnable task) {
        this.priority = priority;
        this.task = task;
    }

    @Override
    public void run() {
        task.run();
    }

    @Override
    public int compareTo(PriorityTask other) {
        return Integer.compare(other.priority, this.priority);
    }
}
