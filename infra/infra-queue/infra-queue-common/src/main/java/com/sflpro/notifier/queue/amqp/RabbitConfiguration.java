package com.sflpro.notifier.queue.amqp;

import com.sflpro.notifier.queue.QueueConfigurationDefaults;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;

/**
 * Company: SFL LLC
 * Created on 2019, June 3
 *
 * @author Yervand Aghababyan
 */
@Configuration
@ConditionalOnProperty(name = "notifier.queue.engine", havingValue = "rabbit")
@EnableRabbit()
@Import({RabbitAutoConfiguration.class, QueueConfigurationDefaults.class})
public class RabbitConfiguration {

    @Value("${amqp.executor.coreSize}")
    private int executorServiceCoreSize;

    @Value("${amqp.executor.maxSize}")
    private int executorServiceMaxSize;

    @Value("${notifier.queue.topic}")
    private String topic;

    @Bean
    public Queue notificationQueue() {
        final Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-max-priority", 2);
        return new Queue(topic, true, false, false, arguments);
    }

    @Bean
    public ThreadPoolTaskExecutor amqpTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

        taskExecutor.setThreadGroupName("amqp-executors");
        taskExecutor.setThreadNamePrefix("amqp-executor-");
        taskExecutor.setCorePoolSize(executorServiceCoreSize);
        taskExecutor.setMaxPoolSize(executorServiceMaxSize);

        return taskExecutor;
    }
}
