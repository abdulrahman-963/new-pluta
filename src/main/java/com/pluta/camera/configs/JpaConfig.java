package com.pluta.camera.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;


@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
//@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
public class JpaConfig {
}