package com.zcw.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * edit spring servlet config
 * <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
 * <init-param>
 * <param-name>contextInitializerClasses</param-name>
 * <param-value>com.timevale.billingsystem.config.PropertySourceInitializer</param-value>
 * </init-param>
 * </servlet-class>
 * <p>
 * edit junit test config
 *
 * @ContextConfiguration(initializers = PropertySourceInitializer.class)
 * Created by zhangchengwei on 27/10/2017.
 */
public class PropertySourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final Logger log = LoggerFactory
            .getLogger(PropertySourceInitializer.class);

    private static PropertySource<?> remotePropertySources = null;
    private static ConfigurableEnvironment env = null;
    private static final String DEFAULT_CONFIG_FILE = "config-server.properties";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        env = applicationContext.getEnvironment();
        initPropertyFromConfigServer();
        env.getPropertySources().addLast(remotePropertySources);
    }

    private static void initPropertyFromConfigServer() {
        Resource resource = new ClassPathResource(DEFAULT_CONFIG_FILE);
        ResourcePropertySource resourcePropertySource = null;
        try {
            resourcePropertySource = new ResourcePropertySource(resource.getDescription(), resource);
            env.getPropertySources().addLast(resourcePropertySource);
        } catch (IOException e) {
            log.error("Get configServer config ERROR!", e);
        }
        String url = resourcePropertySource.getProperty("spring.cloud.config.server.uri").toString();
        String userName = resourcePropertySource.getProperty("spring.security.user.name").toString();
        String password = resourcePropertySource.getProperty("spring.security.user.password").toString();
        ConfigClientProperties configClientProperties = new ConfigClientProperties(env);
        configClientProperties.setUri(url);
        configClientProperties.setUsername(userName);
        configClientProperties.setPassword(password);

        ConfigServicePropertySourceLocator configServicePropertySourceLocator = new ConfigServicePropertySourceLocator(configClientProperties);
        remotePropertySources = configServicePropertySourceLocator.locate(env);
    }


    public static Map<String, Object> getSourceFromConfigServer(String suffixStr) {
        Collection<PropertySource<?>> propertySources = ((CompositePropertySource) remotePropertySources).getPropertySources();
        PropertySource<?> result = null;
        for (PropertySource<?> propertySource : propertySources) {
            if (propertySource.getName().endsWith(suffixStr)) {
                result = propertySource;
                break;
            }
        }
        Map<String, Object> source = ((MapPropertySource) result).getSource();
        return source;
    }
}
