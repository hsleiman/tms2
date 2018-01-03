/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.objectbrains.sti.config;

import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NearCacheConfig;
import com.objectbrains.hcms.hazelcast.HazelcastService.MapKey;
import java.util.List;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author connorpetty
 */
@Configuration
public class Configs implements BeanFactoryAware {

    public static final MapKey<String, String> USER_TOKEN_KEY_MAP = new MapKey<>("USER_TOKEN_KEY_MAP");
    public static final MapKey<String, List<Integer>> USER_PERMISSION_KEY_MAP = new MapKey<>("USER_PERMISSION_KEY_MAP");
    public static final MapKey<String, String> TOKEN_USER_KEY_MAP = new MapKey<>("TOKEN_USER_KEY_MAP");

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Bean
    public MapConfig userTokenKeyMapConfig() {
        MapConfig config = new MapConfig(USER_TOKEN_KEY_MAP.getName());
        config.setMaxIdleSeconds(60 * 30);
        config.setAsyncBackupCount(1);
        config.setNearCacheConfig(getNearCacheConfigLFU(USER_TOKEN_KEY_MAP.getName()));
        return config;
    }

    @Bean
    public MapConfig tokenUserKeyMapConfig() {
        MapConfig config = new MapConfig(TOKEN_USER_KEY_MAP.getName());
        config.setMaxIdleSeconds(60 * 30);
        config.setAsyncBackupCount(1);
        config.setNearCacheConfig(getNearCacheConfigLFU(TOKEN_USER_KEY_MAP.getName()));
        return config;
    }

    @Bean
    public MapConfig userPermissionKeyMapConfig() {
        MapConfig config = new MapConfig(USER_PERMISSION_KEY_MAP.getName());
        config.setMaxIdleSeconds(60 * 30);
        config.setAsyncBackupCount(1);
        config.setNearCacheConfig(getNearCacheConfigLFU(USER_PERMISSION_KEY_MAP.getName()));
        return config;
    }

    private NearCacheConfig getNearCacheConfigLFU(String name) {
        NearCacheConfig nearCacheConfig = new NearCacheConfig(name + "NearCache");
        nearCacheConfig.setCacheLocalEntries(true);
        nearCacheConfig.setMaxIdleSeconds(300);
        nearCacheConfig.setMaxSize(300);
        nearCacheConfig.setEvictionPolicy("LFU");
        nearCacheConfig.setInvalidateOnChange(true);
        return nearCacheConfig;
    }
}
