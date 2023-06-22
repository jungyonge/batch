package com.batch.config;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
@MapperScan(
    basePackages = "com.batch.mapper",
    value = {"com.batch.mapper"})
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String encryptedUrl;

    @Value("${spring.datasource.username}")
    private String encryptedUsername;

    @Value("${spring.datasource.password}")
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

   	@Bean
   	public SqlSessionFactory sqlSessionFactory(DataSource dataSource, ApplicationContext applicationContext) throws Exception {
   		SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
   		sqlSessionFactoryBean.setDataSource(dataSource);
   		return sqlSessionFactoryBean.getObject();
   	}

   	@Bean
   	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) throws Exception {
   		return new SqlSessionTemplate(sqlSessionFactory);
   	}


}
