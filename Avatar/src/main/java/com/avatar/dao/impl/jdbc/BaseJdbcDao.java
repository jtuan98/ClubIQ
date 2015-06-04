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
import org.springframework.util.Assert;

import com.avatar.dao.DbDateDao;
import com.avatar.dao.Sequencer;
import com.avatar.dao.impl.jdbc.mapper.ImageMapper;
import com.avatar.dao.impl.jdbc.sql.BaseDaoSql;
import com.avatar.dto.ImagePic;
import com.avatar.dto.enums.DbTimeZone;

public abstract class BaseJdbcDao implements DbDateDao {

	protected LobHandler lobHandler = new DefaultLobHandler();

	private NamedParameterJdbcTemplate namedParam;

	private JdbcTemplate jdbcTemplate;

	@Resource(name = "avatarSequencer")
	protected Sequencer sequencer;

	private final ImageMapper imageMapper = new ImageMapper(lobHandler);

	@Resource(name = "timezone")
	private String timezone;

	protected ImagePic getImage(final Integer imageIdPk) {
		ImagePic image = null;
		if (imageIdPk != null) {
			try {
				image = getJdbcTemplate().queryForObject(
						BaseDaoSql.SEL_IMAGE_BY_ID, imageMapper, imageIdPk);
			} catch (final EmptyResultDataAccessException e1) {
			}
		}
		return image;
	}

	protected JdbcTemplate getJdbcTemplate() {
		final String timeZoneJdbc = jdbcTemplate.queryForObject(
				BaseDaoSql.GET_SESSION_TIMEZONE, String.class);
		if (!timezone.equalsIgnoreCase(timeZoneJdbc)) {
			System.out.println("WARNING: wrong timezone: " + timeZoneJdbc);
			jdbcTemplate.execute("SET time_zone = '" + timezone + "'");
		}
		return jdbcTemplate;
	}

	protected JdbcTemplate getJdbcTemplate(final DbTimeZone tz) {
		Assert.notNull(tz);
		final String timeZoneJdbc = jdbcTemplate.queryForObject(
				BaseDaoSql.GET_SESSION_TIMEZONE, String.class);
		if (!timezone.equalsIgnoreCase(tz.getDbSetting())) {
			System.out.println("WARNING: wrong timezone: " + timeZoneJdbc);
			jdbcTemplate.execute("SET time_zone = '" + tz.name() + "'");
		}
		return jdbcTemplate;
	}

	protected LobHandler getLobHandler() {
		return lobHandler;
	}

	protected NamedParameterJdbcTemplate getNamedParam() {
		return namedParam;
	}

	@Override
	public Date getNow() {
		return getJdbcTemplate().queryForObject(BaseDaoSql.NOW, Date.class);
	}

	@Override
	public Date getNow(final DbTimeZone tz) {
		return getJdbcTemplate(tz).queryForObject(BaseDaoSql.NOW, Date.class);
	}

	protected void initTemplate(final DataSource ds) {
		try {
			ds.getConnection().setAutoCommit(false);
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		namedParam = new NamedParameterJdbcTemplate(ds);
		jdbcTemplate = new JdbcTemplate(ds);
		System.out.println("Setting time zone to " + timezone);
		try {
			jdbcTemplate.execute("SET time_zone = '" + timezone + "'");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	protected Integer persistImage(final ImagePic picture) {
		final String imageHash = picture != null ? picture.getImageHash()
				: null;
		final boolean hasImage = StringUtils.isNotEmpty(imageHash);
		final Integer idImage = hasImage ? sequencer.nextVal("ID_SEQ") : null;
		System.out.println("persistImage:  hasImage =>" + hasImage);
		if (hasImage) {
			getJdbcTemplate().update(BaseDaoSql.INS_IMAGES, new Object[] {
					// ID
					idImage,
					// IMAGE_ID,
					imageHash,
					// IMAGE_BINARY,
					new SqlLobValue(picture.getPicture()) },
					new int[] { Types.INTEGER, Types.VARCHAR, Types.BLOB });
		}

		return idImage;
	}

	public abstract void setDataSource(DataSource ds);

	public void setJdbcTemplate(final JdbcTemplate template) {
		jdbcTemplate = template;
		jdbcTemplate.execute("SET time_zone = '" + timezone + "'");
	}

	protected void setLobHandler(final LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}

	protected Integer updateImage(final Integer imageIdPk, final byte[] picture) {
		Integer retVal = imageIdPk;
		final String imageHash = DigestUtils.md5Hex(picture);
		if (imageIdPk != null) {
			// Update
			getJdbcTemplate().update(BaseDaoSql.
					UPD_IMAGES,
					new Object[] {
					// IMAGE_ID,
					imageHash,
					// IMAGE_BINARY,
					new SqlLobValue(picture),
					// ID
					imageIdPk },
					new int[] { Types.VARCHAR, Types.BLOB, Types.DATE,
					Types.INTEGER });
		} else {
			// insert
			final int idImage = sequencer.nextVal("ID_SEQ");
			retVal = idImage;
			getJdbcTemplate().update(BaseDaoSql.INS_IMAGES, new Object[] {
					// ID
					idImage,
					// IMAGE_ID,
					imageHash,
					// IMAGE_BINARY,
					new SqlLobValue(picture) },
					new int[] { Types.INTEGER, Types.VARCHAR, Types.BLOB });
		}
		return retVal;
	}

}
