package application.tasks;

import java.util.concurrent.*;

public class TaskExecutor {

    private final ScheduledExecutorService executorService;

    public TaskExecutor() {
        executorService = Executors.newScheduledThreadPool(3);
    }

    public void submitTask(int priority, Runnable task) {
        executorService.scheduleAtFixedRate(
                new PriorityTask(priority, task), 0, 1, TimeUnit.MINUTES);
    }

    public void shutDown() {
        executorService.shutdown();
    }
}
