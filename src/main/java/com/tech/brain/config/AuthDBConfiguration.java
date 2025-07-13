package com.tech.brain.config;

import com.tech.brain.utils.AuthConstant;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Properties;

@Slf4j
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.tech.brain.repository", transactionManagerRef = "platformTransactionManager")
public class AuthDBConfiguration {
    private final AuthDBConfig dbConfig;

    public AuthDBConfiguration(AuthDBConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    @Bean
    JpaVendorAdapter jpaVendorAdapter() {
        log.info("AuthDBConfiguration ===> jpaVendorAdapter");
        return new HibernateJpaVendorAdapter();
    }

    @Bean
    PlatformTransactionManager platformTransactionManager(EntityManagerFactory emf) {
        log.info("AuthDBConfiguration ===> platformTransactionManager");
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(emf);
        return txManager;
    }

    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        log.info("AuthDBConfiguration ===> LocalContainerEntityManagerFactoryBean");
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dbConfig.dataSource());
        factoryBean.setPackagesToScan("com.tech.brain.entity");
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter());
        factoryBean.setJpaProperties(jpaProperties());
        return factoryBean;
    }

    private Properties jpaProperties() {
        log.info("AuthDBConfiguration ===> jpaProperties");
        Properties properties = new Properties();
        properties.put(AuthConstant.DIALECT_KEY, AuthConstant.DIALECT_VALUE);
        properties.put("hibernate.dialect", "com.tech.brain.config.DialectConfig");
//        properties.put("spring.jpa.database-platform", "org.hibernate.dialect.MySQL8Dialect");
        properties.put(AuthConstant.SHOW_SQL_KEY, AuthConstant.SHOW_SQL_VALUE);
        properties.put(AuthConstant.FORMAT_SQL_KEY, AuthConstant.FORMAT_SQL_VALUE);
        properties.put("spring.jpa.hibernate.ddl-auto", "update");
        return properties;
    }
}
