package com.something.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class RetryWithSingleThirdPartyCall {
    ExecutorService executor = Executors.newFixedThreadPool(2);

    // 模拟第三方服务
    static class ThirdPartyService {
        private final AtomicInteger callCount = new AtomicInteger(0);
        private final long processingTimeMillis;
        private final boolean shouldSucceed;

        public ThirdPartyService(long processingTimeMillis, boolean shouldSucceed) {
            this.processingTimeMillis = processingTimeMillis;
            this.shouldSucceed = shouldSucceed;
        }

        public String fetchData(String input) throws Exception {
            int count = callCount.incrementAndGet();
            System.out.println("ThirdPartyService: fetchData called. Attempt: " + count + ". Input: " + input);
            System.out.println("ThirdPartyService: Simulating processing for " + processingTimeMillis + "ms...");

            Thread.sleep(processingTimeMillis); // 模拟耗时操作

            if (!shouldSucceed) {
                System.out.println("ThirdPartyService: Simulating an error during fetch.");
                throw new RuntimeException("Third-party service failed processing input: " + input);
            }

            String result = "Data for " + input + " (from third-party)";
            System.out.println("ThirdPartyService: Successfully fetched data.");
            return result;
        }

        public int getCallCount() {
            return callCount.get();
        }
    }

    private final ThirdPartyService thirdPartyService;
    private final ExecutorService executorService; // 用于异步执行第三方调用
    private CompletableFuture<String> thirdPartyCallFuture = null; // 关键：用于确保第三方只调用一次

    public RetryWithSingleThirdPartyCall(ThirdPartyService thirdPartyService) {
        this.thirdPartyService = thirdPartyService;
        // 使用守护线程，以便JVM可以正常退出
        this.executorService = Executors.newCachedThreadPool(r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        });
    }

    public String processRequestWithRetries(String requestData) throws Exception {
        int maxRetries = 3;
        long timeoutSeconds = 5;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            System.out.println("\nMainLogic: Attempt " + attempt + " for request: " + requestData);

            // 只有在 thirdPartyCallFuture 为 null (即第一次尝试，或者之前的 Future 被重置) 时才真正发起请求
            // 在这个特定场景下，我们希望 Future 在整个重试周期内是同一个，所以只在第一次初始化
            if (thirdPartyCallFuture == null) {
                System.out.println("MainLogic: Initiating third-party call for the first time.");
                // 使用 final 变量以在 lambda 中使用
                final String finalRequestData = requestData;
                thirdPartyCallFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        // 这是实际的第三方调用点
                        return thirdPartyService.fetchData(finalRequestData);
                    } catch (Exception e) {
                        // CompletableFuture 需要显式处理异常并完成 exceptionally
                        throw new RuntimeException(e); // 或者更具体的自定义异常
                    }
                }, executorService);
            } else {
                System.out.println("MainLogic: Reusing existing third-party call future.");
            }

            try {
                // 尝试在超时时间内获取结果
                System.out.println("MainLogic: Attempting to get result with timeout: " + timeoutSeconds + "s");
                String result = thirdPartyCallFuture.get(timeoutSeconds, TimeUnit.SECONDS);
                System.out.println("MainLogic: Successfully got result in attempt " + attempt);
                return result; // 成功获取，直接返回
            } catch (TimeoutException e) {
                System.err.println("MainLogic: Attempt " + attempt + " timed out waiting for third-party response.");
                lastException = e;
                if (attempt == maxRetries) {
                    System.err.println("MainLogic: Max retries reached after timeout. Failing.");
                    throw new Exception("Request failed after " + maxRetries + " attempts due to timeout.", e);
                }
                // 超时，继续下一次重试
            } catch (ExecutionException e) {
                // 第三方调用本身抛出了异常
                System.err.println("MainLogic: Third-party call failed with an exception: " + e.getCause().getMessage());
                lastException = (Exception) e.getCause();
                // 根据需求，如果第三方调用明确失败，可能不应该再重试。
                // 题目描述为“如果在第二次重试且未超时的时候则返回请求第三方的结果”，这意味着如果第三方调用已完成（即使是失败），
                // 我们应该使用那个“结果”（即异常）。
                throw lastException;
            } catch (InterruptedException e) {
                System.err.println("MainLogic: Request interrupted.");
                Thread.currentThread().interrupt(); // 恢复中断状态
                throw new Exception("Request processing was interrupted.", e);
            }
            System.out.println("MainLogic: Retrying...");
        }

        // 如果循环结束仍未成功 (理论上应该在循环内通过抛出异常结束)
        throw new Exception("Request failed after " + maxRetries + " attempts. Last known error: " +
                (lastException != null ? lastException.getMessage() : "Unknown error"), lastException);
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        // 场景1: 第三方调用很快成功
        System.out.println("--- SCENARIO 1: Third-party quick success ---");
        ThirdPartyService quickService = new ThirdPartyService(1000, true); // 1秒完成
        RetryWithSingleThirdPartyCall handler1 = new RetryWithSingleThirdPartyCall(quickService);
        try {
            String result = handler1.processRequestWithRetries("data1");
            System.out.println("Final Result (Scenario 1): " + result);
            System.out.println("Third-party call count (Scenario 1): " + quickService.getCallCount());
        } catch (Exception e) {
            System.err.println("Error (Scenario 1): " + e.getMessage());
            if (e.getCause() != null) System.err.println("  Cause: " + e.getCause().getMessage());
        } finally {
            handler1.shutdown();
        }
        System.out.println("--------------------------------------------\n");


        // 场景2: 第三方调用第一次超时，第二次重试时第三方已完成 (在5秒内)
        System.out.println("--- SCENARIO 2: Third-party slow, success on second attempt's wait ---");
        ThirdPartyService mediumService = new ThirdPartyService(7000, true); // 7秒完成
        RetryWithSingleThirdPartyCall handler2 = new RetryWithSingleThirdPartyCall(mediumService);
        try {
            String result = handler2.processRequestWithRetries("data2");
            System.out.println("Final Result (Scenario 2): " + result);
            System.out.println("Third-party call count (Scenario 2): " + mediumService.getCallCount());
        } catch (Exception e) {
            System.err.println("Error (Scenario 2): " + e.getMessage());
            if (e.getCause() != null) System.err.println("  Cause: " + e.getCause().getMessage());
        } finally {
            handler2.shutdown();
        }
        System.out.println("--------------------------------------------\n");


        // 场景3: 第三方调用非常慢，所有重试都超时
        System.out.println("--- SCENARIO 3: Third-party very slow, all retries timeout ---");
        ThirdPartyService verySlowService = new ThirdPartyService(20000, true); // 20秒完成
        RetryWithSingleThirdPartyCall handler3 = new RetryWithSingleThirdPartyCall(verySlowService);
        try {
            String result = handler3.processRequestWithRetries("data3");
            System.out.println("Final Result (Scenario 3): " + result);
        } catch (Exception e) {
            System.err.println("Error (Scenario 3): " + e.getMessage());
            // if (e.getCause() != null) System.err.println("  Cause: " + e.getCause().getMessage());
        } finally {
            System.out.println("Third-party call count (Scenario 3): " + verySlowService.getCallCount());
            handler3.shutdown();
        }
        System.out.println("--------------------------------------------\n");

        // 场景4: 第三方调用直接失败
        System.out.println("--- SCENARIO 4: Third-party call fails directly ---");
        ThirdPartyService failingService = new ThirdPartyService(1000, false); // 1秒后失败
        RetryWithSingleThirdPartyCall handler4 = new RetryWithSingleThirdPartyCall(failingService);
        try {
            String result = handler4.processRequestWithRetries("data4");
            System.out.println("Final Result (Scenario 4): " + result);
        } catch (Exception e) {
            System.err.println("Error (Scenario 4): " + e.getMessage());
            // if (e.getCause() != null) System.err.println("  Cause: " + e.getCause().getMessage());
        } finally {
            System.out.println("Third-party call count (Scenario 4): " + failingService.getCallCount());
            handler4.shutdown();
        }
        System.out.println("--------------------------------------------\n");
    }
}
