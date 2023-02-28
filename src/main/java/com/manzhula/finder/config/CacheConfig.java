package com.manzhula.finder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Configuration
public class CacheConfig {
    @Bean
    @Scope("singleton")
    public Map<String, UserCache> userCacheMap() {
        return new HashMap<>();
    }

    @Bean
    @Scope("singleton")
    public Map<String, UserCache> userFilterCacheMap() {
        return new HashMap<>();
    }

    @Bean
    @Scope("singleton")
    public Map<String, UserCache> userOnFilterCacheMap() {
        return new HashMap<>();
    }
}
