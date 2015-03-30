package com.avatar.dao.impl.jdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.lob.LobHandler;

import com.avatar.dto.ImagePic;

public class ImageMapper implements RowMapper<ImagePic> {
	private final LobHandler lobHandler;

	public ImageMapper(final LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}

	@Override
	public ImagePic mapRow(final ResultSet rs, final int rowNo) throws SQLException {
		final ImagePic retVal = new ImagePic();
		retVal.setId(rs.getInt("ID"));
		retVal.setImageHash(rs.getString("IMAGE_ID"));
		final byte[] binary = lobHandler.getBlobAsBytes(rs, "IMAGE_BINARY");
		retVal.setPicture(binary);
		return retVal;
	}

}
