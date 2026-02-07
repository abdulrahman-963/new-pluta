package com.pluta.camera.configs;

import feign.Logger;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            log.error("Feign client error - Method: {}, Status: {}, Reason: {}",
                    methodKey, response.status(), response.reason());
            return new ErrorDecoder.Default().decode(methodKey, response);
        };
    }
}