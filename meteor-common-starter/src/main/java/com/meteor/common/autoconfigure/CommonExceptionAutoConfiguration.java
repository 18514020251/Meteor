package com.meteor.common.autoconfigure;

import com.meteor.common.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 *  通用异常处理自动配置类
 *
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
