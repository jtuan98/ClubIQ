package com.avatar.dao.impl.jdbc;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

//@Configuration
//@EnableAspectJAutoProxy(proxyTargetClass = true)
public class MySqlLocalJdbcContextConfiguration {

	@Bean(name="datasource")
	public DataSource datasource() {
		final DriverManagerDataSource retVal = new DriverManagerDataSource();
		retVal.setDriverClassName("com.mysql.jdbc.Driver");
		retVal.setUrl("jdbc:mysql://localhost:3306/junitavatardb");
		retVal.setUsername("avatar");
		retVal.setPassword("pa22w0rd");
		return retVal;
	}
}