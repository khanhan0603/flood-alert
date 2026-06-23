package com.example.flood_alert.configuration;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableCaching
public class RedisConfig {
        @Bean
        public RedisCacheManager cacheManager(
                        RedisConnectionFactory factory) {

                RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofSeconds(30))
                                .serializeValuesWith(
                                                RedisSerializationContext.SerializationPair
                                                                .fromSerializer(
                                                                                new GenericJackson2JsonRedisSerializer()));

                return RedisCacheManager.builder(factory)
                                .cacheDefaults(config)
                                .build();
        }

        // Test redis
        // @Bean
        // CommandLineRunner cacheTest(
        //                 CacheManager cacheManager) {

        //         return args -> {
        //                 Cache cache = cacheManager.getCache(
        //                                 "team-dashboard");

        //                 cache.put("test", "hello");

        //                 System.out.println(
        //                                 cache.get("test").get());
        //         };
        // }
}
