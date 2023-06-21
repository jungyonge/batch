package com.batch.config;
import java.io.IOException;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Value("${jdbc.url}")
    private String encryptedUrl;

    @Value("${jdbc.username}")
    private String encryptedUsername;

    @Value("${jdbc.password}")
    private String encryptedPassword;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(JasyptConfig.decrypt(encryptedUrl));
        dataSource.setUsername(JasyptConfig.decrypt(encryptedUsername));
        dataSource.setPassword(JasyptConfig.decrypt(encryptedPassword));
        // 다른 DataSource 설정들...
        return dataSource;
    }


}
