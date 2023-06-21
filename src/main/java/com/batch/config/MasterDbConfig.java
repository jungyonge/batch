package com.batch.config;

import com.batch.annotation.MasterDb;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
@MapperScan(
    basePackages = "com.batch.mapper",
    value = {"com.batch.mapper"},
    sqlSessionFactoryRef = "masterSqlSessionFactory"
)
public class MasterDbConfig  {

    //    @Primary
//    @Bean
//    @ConfigurationProperties(prefix = "spring.datasource")
//    public DataSourceProperties masterDataSourceProp() {
//        return new DataSourceProperties();
//    }
    public static final String BASE_PACKAGE = "com.batch.mapper";
    @Value("${mybatis.config-location}")
    private String COMFIG_LOCATION_PATH;
    @Value("${mybatis.mapper-locations}")
    public String MAPPER_LOCATION_PATH;

    protected void configureSqlSessionFactory(SqlSessionFactoryBean sessionFactoryBean, DataSource dataSource) throws
        IOException {
        sessionFactoryBean.setDataSource(dataSource);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sessionFactoryBean.setConfigLocation(resolver.getResource(COMFIG_LOCATION_PATH));
        sessionFactoryBean.setMapperLocations(resolver.getResources(MAPPER_LOCATION_PATH));
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.tomcat")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    public SqlSessionFactory masterSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        configureSqlSessionFactory(sqlSessionFactoryBean, masterDataSource());
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public SqlSessionTemplate masterSqlSessionTemplate() throws Exception {
        SqlSessionTemplate sqlSessionTemplate = new SqlSessionTemplate(masterSqlSessionFactory(),
            ExecutorType.SIMPLE);
        return sqlSessionTemplate;
    }
}
