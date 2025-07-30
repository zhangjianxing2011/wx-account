//package com.something.utils;
//
//import org.springframework.stereotype.Component;
//
//import java.util.concurrent.*;
//
//@Component
//public class ProcessingUtil {
//    private final ConcurrentMap<String, FutureTask<ProcessingResult>> processingTasks = new ConcurrentHashMap<>();
//
//    private final ConcurrentMap<String, CacheEntry<ProcessingResult>> resultCache = new ConcurrentHashMap<>();
//
////    private final ExecutorService executor = Executors.newCachedThreadPool();
//
//    private static final long CACHE_EXPIRATION = 5 * 60 * 1000;
//
//    public ProcessingResult handleRequest(String requestId) throws Exception {
//        // 1. 检查缓存中是否存在结果
//        ProcessingResult cached = getCachedResult(requestId);
//        if (cached != null) {
//            return cached;
//        }
//
//        // 2. 创建或获取处理任务
//        FutureTask<ProcessingResult> futureTask = new FutureTask<>(() -> {
//            try {
//                // 调用第三方接口
//                ProcessingResult result = callThirdPartyService(requestId);
//                // 缓存结果
//                cacheResult(requestId, result);
//                return result;
//            } catch (Exception e) {
//                // 记录错误结果
//                ProcessingResult errorResult = new ProcessingResult(false, "调用失败: " + e.getMessage());
//                cacheResult(requestId, errorResult);
//                throw e;
//            }
//        });
//
//        FutureTask<ProcessingResult> existingTask = processingTasks.putIfAbsent(requestId, futureTask);
//        if (existingTask == null) {
//            // 当前线程首次处理该请求
//            executor.submit(futureTask);
//        } else {
//            futureTask = existingTask;
//        }
//
//        // 3. 等待异步任务完成
//        try {
//            return futureTask.get(4800, TimeUnit.MILLISECONDS); // 超时控制
//        } catch (TimeoutException e) {
//            // 超时处理：返回 null 或触发微信重试机制
//            return null;
//        }
//    }
//
//    // 调用第三方接口（模拟）
//    private ProcessingResult callThirdPartyService(String requestId) throws Exception {
//        // 模拟耗时操作（如网络请求）
//        Thread.sleep(3000);
//        // 假设成功返回结果
//        return new ProcessingResult(true, "处理成功：" + requestId);
//    }
//
//    // 获取缓存结果
//    private ProcessingResult getCachedResult(String key) {
//        CacheEntry<ProcessingResult> entry = resultCache.get(key);
//        if (entry != null && !entry.isExpired()) {
//            return entry.value;
//        } else {
//            resultCache.remove(key);
//            return null;
//        }
//    }
//
//    // 缓存处理结果
//    private void cacheResult(String key, ProcessingResult value) {
//        resultCache.put(key, new CacheEntry<>(value, CACHE_EXPIRATION));
//    }
//
//    // 定时清理过期缓存（可选）
//    public void startCacheCleaner() {
//        ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();
//        cleaner.scheduleAtFixedRate(() -> {
//            long now = System.currentTimeMillis();
//            resultCache.forEach((key, entry) -> {
//                if (entry.isExpired()) {
//                    resultCache.remove(key);
//                }
//            });
//        }, 1, 1, TimeUnit.MINUTES);
//    }
//
//    // 停止线程池（应用关闭时调用）
//    public void shutdown() {
//        executor.shutdownNow();
//    }
//
//    // 缓存条目类
//    private static class CacheEntry<T> {
//        final T value;
//        final long expireTime;
//
//        CacheEntry(T value, long ttlMillis) {
//            this.value = value;
//            this.expireTime = System.currentTimeMillis() + ttlMillis;
//        }
//
//        boolean isExpired() {
//            return System.currentTimeMillis() > expireTime;
//        }
//    }
//
//    // 处理结果类
//    public static class ProcessingResult {
//        private final boolean success;
//        private final String message;
//
//        public ProcessingResult(boolean success, String message) {
//            this.success = success;
//            this.message = message;
//        }
//
//        public boolean isSuccess() {
//            return success;
//        }
//
//        public String getMessage() {
//            return message;
//        }
//    }
//}
