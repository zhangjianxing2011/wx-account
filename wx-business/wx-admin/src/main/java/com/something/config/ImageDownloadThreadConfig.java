package com.something.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 可以直接在配置类中配置线程池，也可以在方法中配置线程池
 *
 */
@Configuration
public class ImageDownloadThreadConfig implements SchedulingConfigurer {

    @Bean("scheduledThreadPoolExecutor")
    public Executor scheduledThreadPoolExecutor() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("admin-Scheduling-");
        scheduler.setThreadPriority(3);
        scheduler.setPoolSize(6);//((ScheduledThreadPoolExecutor)this.scheduledExecutor).setCorePoolSize(poolSize);
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.setScheduler(scheduledThreadPoolExecutor());
    }

    /**
     *
     * @return core-> queue -> max -> new thread
     */
    @Bean(name = "imageDownloadThreadPoolExecutor")
    public ThreadPoolTaskExecutor imageDownLoadThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setCorePoolSize(12);
        executor.setQueueCapacity(200000);//LinkedBlockingQueue，blockQueue you know
        executor.setKeepAliveSeconds(300);
        executor.setThreadNamePrefix("img-Down-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());//这边可以用DiscardPolicy策略
        executor.initialize();
        return executor;
    }



    @Bean(name = "chatThreadPool")
    public ThreadPoolTaskExecutor webSocketThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(2000);
        executor.setKeepAliveSeconds(60);
        executor.setDaemon(true);
        executor.setThreadNamePrefix("chat-thread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

}
