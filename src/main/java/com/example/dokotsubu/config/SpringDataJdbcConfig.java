package com.example.dokotsubu.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;

@Configuration
public class SpringDataJdbcConfig {

    @Bean
    static BeanPostProcessor relationalMappingContextCustomizer() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof RelationalMappingContext mappingContext) {
                    mappingContext.setForceQuote(false);
                }
                return bean;
            }
        };
    }
}
