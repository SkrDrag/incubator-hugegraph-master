package org.apache.hugegraph.api.traversers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomTaskManager {

    private ExecutorService executor;

    public CustomTaskManager(int poolSize) {
        // 创建一个固定大小的线程池
        this.executor = Executors.newFixedThreadPool(poolSize);
    }

    // 提交任务
    public void submitTask(CustomTask task) {
        executor.submit(task);
    }

    // 关闭线程池
    public void shutdown() {
        this.executor.shutdown();
    }

    // 任务类
    public static class CustomTask implements Runnable {
        private String taskName;

        public CustomTask(String taskName) {
            this.taskName = taskName;
        }

        @Override
        public void run() {
            // 执行任务的逻辑
            System.out.println("Task " + taskName + " is executing.");
        }
    }
}

