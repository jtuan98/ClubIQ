package com.avatar.scheduler.task;

import java.util.logging.Logger;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.avatar.dao.TokenCacheDao;

@Component("cleanUpExpiredTokenTask")
public class CleanUpExpiredTokenTask {
	private final Logger logger = Logger.getLogger(CleanUpExpiredTokenTask.class.getName());

	@Resource(name="tokenCacheDaoJdbc")
	private TokenCacheDao tokenCacheDaoJdbc;

	public void cleanup() {
		final int rowsDeleted = 0;//tokenCacheDaoJdbc.cleanupExpiredTokens();
		logger.info("Cleaning up expired tokens " + rowsDeleted);
	}
}
