package com.avatar.dao.impl.jdbc;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;

import com.avatar.dao.Sequencer;
import com.avatar.dao.impl.jdbc.mapper.ImageMapper;
import com.avatar.dto.ImagePic;

public abstract class BaseJdbcDao {

	protected LobHandler lobHandler = new DefaultLobHandler();

	private NamedParameterJdbcTemplate namedParam;
	private JdbcTemplate jdbcTemplate;

	@Resource(name = "avatarSequencer")
	protected Sequencer sequencer;

	private static String INS_IMAGES = "INSERT INTO IMAGES (ID, "
			+ "IMAGE_ID, IMAGE_BINARY, " + "CREATE_DATE) VALUES (?, ?, ?, ?)";

	private static String UPD_IMAGES = "UPDATE IMAGES set "
			+ "IMAGE_ID=?, IMAGE_BINARY=?, CREATE_DATE=? WHERE ID=?";

	private static final String SEL_IMAGE_BY_ID = "SELECT * FROM IMAGES where ID = ? ";

	private final ImageMapper imageMapper = new ImageMapper(lobHandler);

	protected ImagePic getImage(final Integer imageIdPk) {
		ImagePic image = null;
		if (imageIdPk != null) {
			try {
				image = getJdbcTemplate().queryForObject(SEL_IMAGE_BY_ID,
						imageMapper, imageIdPk);
			} catch (final EmptyResultDataAccessException e1) {
			}
		}
		return image;
	}

	protected JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	protected LobHandler getLobHandler() {
		return lobHandler;
	}

	protected NamedParameterJdbcTemplate getNamedParam() {
		return namedParam;
	}

	protected void initTemplate(final DataSource ds) {
		try {
			ds.getConnection().setAutoCommit(false);
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		namedParam = new NamedParameterJdbcTemplate(ds);
		jdbcTemplate = new JdbcTemplate(ds);
	}

	protected Integer persistImage(final ImagePic picture) {
		final String imageHash = picture != null ? picture.getImageHash()
				: null;
		final boolean hasImage = StringUtils.isNotEmpty(imageHash);
		final Integer idImage = hasImage ? sequencer.nextVal("ID_SEQ") : null;
		if (hasImage) {
			getJdbcTemplate().update(
					INS_IMAGES,
					new Object[] {
							// ID
							idImage,
							// IMAGE_ID,
							imageHash,
							// IMAGE_BINARY,
							new SqlLobValue(picture.getPicture()),
							// CREATE_DATE
							new Date() },
					new int[] { Types.INTEGER, Types.VARCHAR, Types.BLOB,
							Types.DATE });
		}

		return idImage;
	}

	public abstract void setDataSource(DataSource ds);

	public void setJdbcTemplate(final JdbcTemplate template) {
		jdbcTemplate = jdbcTemplate;
	}

	protected void setLobHandler(final LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}

	protected Integer updateImage(final Integer imageIdPk, final byte[] picture) {
		Integer retVal = imageIdPk;
		final String imageHash = DigestUtils.md5Hex(picture);
		if (imageIdPk != null) {
			// Update
			getJdbcTemplate().update(
					UPD_IMAGES,
					new Object[] {
							// IMAGE_ID,
							imageHash,
							// IMAGE_BINARY,
							new SqlLobValue(picture),
							// CREATE_DATE
							new Date(),
							// ID
							imageIdPk },
					new int[] { Types.VARCHAR, Types.BLOB, Types.DATE,
							Types.INTEGER });
		} else {
			// insert
			final int idImage = sequencer.nextVal("ID_SEQ");
			retVal = idImage;
			getJdbcTemplate().update(
					INS_IMAGES,
					new Object[] {
							// ID
							idImage,
							// IMAGE_ID,
							imageHash,
							// IMAGE_BINARY,
							new SqlLobValue(picture),
							// CREATE_DATE
							new Date() },
					new int[] { Types.INTEGER, Types.VARCHAR, Types.BLOB,
							Types.DATE });
		}
		return retVal;
	}

}
