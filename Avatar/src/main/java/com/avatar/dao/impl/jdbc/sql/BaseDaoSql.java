package com.avatar.dao.impl.jdbc.sql;

public class BaseDaoSql {
	public static final String NOW = "SELECT NOW()";
	public static final String GET_SESSION_TIMEZONE ="SELECT @@session.time_zone";
	public static final String INS_IMAGES = "INSERT INTO IMAGES (ID, "
			+ "IMAGE_ID, IMAGE_BINARY, CREATE_DATE) VALUES (?, ?, ?, NOW())";
	public static final String UPD_IMAGES = "UPDATE IMAGES set "
			+ "IMAGE_ID=?, IMAGE_BINARY=?, CREATE_DATE=NOW() WHERE ID=?";
	public static final String SEL_IMAGE_BY_ID = "SELECT * FROM IMAGES where ID = ? ";
}
