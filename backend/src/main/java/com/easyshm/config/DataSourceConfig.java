package com.easyshm.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceAutoConfigure;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@EnableAutoConfiguration(exclude = DruidDataSourceAutoConfigure.class)
public class DataSourceConfig {

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource.druid.mysql")
    public DataSource dataSource() {
        return new DruidDataSource();
    }

    @Bean("tdengineDataSource")
    @ConfigurationProperties("spring.datasource.druid.tdengine")
    public DataSource tdengineDataSource() {
        return new DruidDataSource();
    }

    @Bean("tdengineJdbcTemplate")
    public JdbcTemplate tdengineJdbcTemplate(@Qualifier("tdengineDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }
}
