package com.datasophon.api.configuration;

import com.datasophon.common.utils.FileUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.CollectionUtils;

@Slf4j
public class ConfigPropertiesExtend implements EnvironmentPostProcessor {
    
    private static final String CONFIG_HOME = "conf/datasophon.conf";
    
    private static final String DEFAULT_APPLICATION_CONFIG = "conf/profiles/application-config.yml";
    
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MutablePropertySources propertySources = environment.getPropertySources();
        
        // load the datasophon configuration (config/profiles/application-config.yml)
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        if (!activeProfiles.isEmpty() && !Collections.singletonList("config").containsAll(activeProfiles)) {
            // running other profiles
            return;
        }
        try {
            List<PropertySource<?>> configPropertySources = new YamlPropertySourceLoader().load(DEFAULT_APPLICATION_CONFIG, new FileSystemResource(DEFAULT_APPLICATION_CONFIG));
            if (!CollectionUtils.isEmpty(configPropertySources)) {
                for (PropertySource<?> propertySource : configPropertySources) {
                    propertySources.addFirst(propertySource);
                }
            }
        } catch (Exception e) {
            System.err.println("Default config application-config not found ");
            log.error("Default config application-config not found", e);
        }
        
        // load the datasophon configuration (config/datasophon.conf)
        Properties properties = loadCustomProperties();
        propertySources.addFirst(new PropertiesPropertySource("datasophonConfig", properties));
    }
    
    private Properties loadCustomProperties() {
        Properties properties = new Properties();
        File file = new File(FileUtils.concatPath(System.getProperty("user.dir"), CONFIG_HOME));
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            properties.load(inputStream);
        } catch (Exception e) {
            System.err.println("Failed to load the datasophon configuration (config/datasophon.conf), use application-config.yml");
            return new Properties();
        }
        List<Object> removeKeys = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String val = String.valueOf(entry.getValue()).trim();
            if (StringUtils.isBlank(val)) {
                removeKeys.add(entry.getKey());
            }
            entry.setValue(val);
        }
        for (Object key : removeKeys) {
            properties.remove(key);
        }
        return properties;
    }
}
