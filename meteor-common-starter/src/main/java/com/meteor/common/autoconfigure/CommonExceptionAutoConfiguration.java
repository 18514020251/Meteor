package com.meteor.common.autoconfigure;

import com.meteor.common.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @author Programmer
 * @date 2026-01-17 15:32
 */
@AutoConfiguration
public class CommonExceptionAutoConfiguration {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
