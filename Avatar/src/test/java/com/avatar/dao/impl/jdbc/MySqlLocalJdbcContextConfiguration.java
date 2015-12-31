package com.avatar.dao.impl.jdbc;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.avatar.dao.AccountDao;
import com.avatar.dao.ClubDao;
import com.avatar.dao.Sequencer;

//@Configuration
//@EnableAspectJAutoProxy(proxyTargetClass = true)
public class MySqlLocalJdbcContextConfiguration {

	@Bean(name = "accountDaoJdbc")
	public AccountDao accountDaoJdbc() {
		return new AccountDaoJdbc();
	}

	@Bean(name = "avatarSequencer")
	public Sequencer avatarSequencer() {
		return new AvatarSequencer();
	}

	@Bean(name = "clubDaoJdbc")
	public ClubDao clubDaoJdbc() {
		return new ClubDaoJdbc();
	}

	@Bean(name = "avatarDataSource")
	public DataSource datasource() {
		final DriverManagerDataSource retVal = new DriverManagerDataSource();
		retVal.setDriverClassName("com.mysql.jdbc.Driver");
		retVal.setUrl("jdbc:mysql://localhost:3306/junitavatardb");
		retVal.setUsername("avatar");
		retVal.setPassword("pa22w0rd");
		return retVal;
	}

	@Bean(name = "timezone")
	public String timezone() {
		return "US/Pacific";
	}

}