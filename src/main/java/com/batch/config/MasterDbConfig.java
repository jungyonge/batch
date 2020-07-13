package com.batch.config;

import com.batch.annotation.MasterDb;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
@MapperScan(
        basePackages = DatabaseConfig.BASE_PACKAGE,
        annotationClass = MasterDb.class,
        sqlSessionFactoryRef = "masterSqlSessionFactory"
)
public class MasterDbConfig extends DatabaseConfig {

    @Primary
    @Bean
    @ConfigurationProperties(prefix = "master.datasource")
    public DataSourceProperties masterDataSourceProp() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "master.datasource.tomcat")
    public DataSource masterDataSource() {
        return masterDataSourceProp().initializeDataSourceBuilder().build();
    }

    @Bean
    public SqlSessionFactory masterSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        configureSqlSessionFactory(sqlSessionFactoryBean, masterDataSource());
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public SqlSessionTemplate masterSqlSessionTemplate() throws Exception {
        SqlSessionTemplate sqlSessionTemplate = new SqlSessionTemplate(masterSqlSessionFactory(), ExecutorType.SIMPLE);
        return sqlSessionTemplate;
    }
}
