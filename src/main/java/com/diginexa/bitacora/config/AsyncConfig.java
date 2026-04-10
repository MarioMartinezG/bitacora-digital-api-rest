package com.diginexa.bitacora.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración para procesamiento asíncrono.
 * Usado por los listeners de notificaciones para no bloquear el hilo principal.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "notificacionExecutor")
    public Executor notificacionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("notificacion-");
        executor.initialize();
        return executor;
    }
}
