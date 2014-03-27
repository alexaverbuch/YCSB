package com.ldbc.driver.runtime.executor;

import com.ldbc.driver.OperationHandler;
import com.ldbc.driver.OperationResult;
import com.ldbc.driver.temporal.Duration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadPoolOperationHandlerExecutor implements OperationHandlerExecutor {
    private final ExecutorService threadPool;
    private final CompletionService<OperationResult> completionService;

    private final AtomicLong submittedHandlers = new AtomicLong(0);
    private boolean shutdown = false;

    public ThreadPoolOperationHandlerExecutor(int threadCount) {
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        this.threadPool = Executors.newFixedThreadPool(threadCount, threadFactory);
        this.completionService = new ExecutorCompletionService<OperationResult>(threadPool);
    }

    @Override
    synchronized public final Future<OperationResult> execute(OperationHandler<?> operationHandler) {
        Future<OperationResult> future = completionService.submit(operationHandler);
        submittedHandlers.incrementAndGet();
        return future;
    }

    @Override
    synchronized public final void shutdown(Duration wait) throws OperationHandlerExecutorException {
        if (true == shutdown)
            throw new OperationHandlerExecutorException("Executor has already been shutdown");
        try {
            threadPool.shutdown();
            boolean allHandlersCompleted = threadPool.awaitTermination(wait.asMilli(), TimeUnit.MILLISECONDS);
            if (false == allHandlersCompleted) {
                throw new OperationHandlerExecutorException("Executor shutdown before all handlers could complete execution");
            }
        } catch (Exception e) {
            throw new OperationHandlerExecutorException("Error encountered while trying to shutdown", e.getCause());
        }
        shutdown = true;
    }
}
