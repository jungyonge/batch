package com.batch.config;

import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@MapperScan(basePackages = "com.batch.mapper", annotationClass = org.apache.ibatis.annotations.Mapper.class)
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String encryptedUrl;

    @Value("${spring.datasource.username}")
    private String encryptedUsername;

    @Value("${spring.datasource.password}")
    private String encryptedPassword;

    @Bean("DatasourceCustom")
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(JasyptConfig.decrypt(encryptedUrl));
        dataSource.setUsername(JasyptConfig.decrypt(encryptedUsername));
        dataSource.setPassword(JasyptConfig.decrypt(encryptedPassword));
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        // 다른 DataSource 설정들...
        return dataSource;
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource,
        ApplicationContext applicationContext) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
             sqlSessionFactoryBean.setDataSource(dataSource);
             sqlSessionFactoryBean.setConfigLocation(
                 applicationContext.getResource("classpath:mybatis-config.xml")); // MyBatis 설정 파일 위치 설정
             sqlSessionFactoryBean.setMapperLocations(
                 applicationContext.getResources("classpath:mapper/*.xml")); // 매퍼 XML 파일 위치 설정
             return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory)
        throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
