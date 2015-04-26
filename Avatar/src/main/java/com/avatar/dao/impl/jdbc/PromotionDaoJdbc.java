package com.avatar.dao.impl.jdbc;

import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.avatar.dao.PromotionDao;
import com.avatar.dao.impl.jdbc.mapper.PromotionMapper;
import com.avatar.dto.promotion.Promotion;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.PermissionDeniedException;

@Repository
public class PromotionDaoJdbc extends BaseJdbcDao implements PromotionDao {

	private static String GET_PROMOTIONS_BY_CLUBID_AMENITYID = "SELECT * FROM PROMOTIONS WHERE CLUB_ID = ? AND CLUB_AMENITY_ID = ? ";
	private static String GET_PROMOTIONS_BY_CLUBID_AMENITYID_VALID = GET_PROMOTIONS_BY_CLUBID_AMENITYID + " AND CURDATE() >= EFFECTIVE_DATE AND CURDATE() <= ENDING_DATE ";

	private final PromotionMapper promotionMapper = new PromotionMapper();

	private static String INS_PROMO_HISTORY = "INSERT INTO PROMOTION_HISTORY ("
			+ "      ID,PROMOTION_ID,CLUB_ID,CLUB_AMENITY_ID,USER_ID,PROMO_READ,CREATE_DATE) "
			+ "SELECT ?,ID,CLUB_ID,CLUB_AMENITY_ID,       ?,        ?, NOW() "
			+ "FROM PROMOTIONS WHERE ID = ? ";

	private static String INS_NEW_PROMOTION = "INSERT INTO PROMOTIONS ("
			+ "ID, CLUB_ID, CLUB_AMENITY_ID,	TITLE, DETAILS, EFFECTIVE_DATE, ENDING_DATE, CREATE_DATE) "
			+ "VALUES(?,?,?,?,?,?,?,NOW())";

	private static String GET_PROMO_ID_PK = "select ID from PROMOTIONS where ID=?";

	private static String UPD_PROMOTION_BY_PK = "update PROMOTIONS set "
			+ "TITLE=?, DETAILS=? WHERE ID=?";
	private static String UPD_PROMOTION_EFFECTIVE_DATE_BY_PK = "update PROMOTIONS set "
			+ "EFFECTIVE_DATE=? WHERE ID=?";
	private static String UPD_PROMOTION_ENDINGDATE_BY_PK = "update PROMOTIONS set "
			+ "ENDING_DATE=? WHERE ID=?";

	private static String GET_PROMOTION_BY_ID = "SELECT * FROM PROMOTIONS WHERE ID = ?";

	private static String DEL_PROMO_ID = "DELETE from PROMOTIONS where ID=?";
	private static String COUNT_PROMO_ID_HISTORY = "SELECT COUNT(*) from PROMOTION_HISTORY where PROMOTION_ID=?";

	@Override
	public void delete(final Integer promoIdPk) throws NotFoundException,
			PermissionDeniedException {
		// Check if id found
		getPromoIdPk(promoIdPk);
		final Integer count = getJdbcTemplate().queryForObject(
				COUNT_PROMO_ID_HISTORY, Integer.class, promoIdPk);
		if (count > 0) {
			throw new PermissionDeniedException("Promotion ID " + promoIdPk + " has references");
		}
		getJdbcTemplate().update(DEL_PROMO_ID, promoIdPk);
	}

	@Override
	public List<Promotion> getAllPromotions(final Integer clubIdPk,
			final Integer amenityIdPk) {
		final List<Promotion> retVal = getJdbcTemplate().query(
				GET_PROMOTIONS_BY_CLUBID_AMENITYID, promotionMapper, clubIdPk,
				amenityIdPk);
		return retVal;
	}

	private void getPromoIdPk(final Integer id) throws NotFoundException {
		// Check if id found
		try {
			final Integer promoIdPk = getJdbcTemplate().queryForObject(
					GET_PROMO_ID_PK, Integer.class, id);
		} catch (final EmptyResultDataAccessException e) {
			throw new NotFoundException("Promotion ID " + id + " not found");
		}

	}

	@Override
	public Promotion getPromotion(final Integer promoIdPk)
			throws NotFoundException {
		try {
			final Promotion retVal = getJdbcTemplate().queryForObject(
					GET_PROMOTION_BY_ID, promotionMapper, promoIdPk);
			return retVal;
		} catch (final EmptyResultDataAccessException e) {
			throw new NotFoundException("Promotion ID " + promoIdPk
					+ " not found");
		}
	}

	@Override
	public List<Promotion> getValidPromotions(final Integer clubIdPk,
			final Integer amenityIdPk) {
		final List<Promotion> retVal = getJdbcTemplate().query(
				GET_PROMOTIONS_BY_CLUBID_AMENITYID_VALID, promotionMapper, clubIdPk,
				amenityIdPk);
		return retVal;
	}

	@Override
	public void newPromotion(final Promotion promotion) {
		Assert.notNull(promotion);
		Assert.notNull(promotion.getClub());
		Assert.notNull(promotion.getClub().getId());
		Assert.notNull(promotion.getAmenity());
		Assert.notNull(promotion.getAmenity().getId());

		final int idPk = sequencer.nextVal("ID_SEQ");
		getJdbcTemplate().update(INS_NEW_PROMOTION, idPk,
				promotion.getClub().getId(), promotion.getAmenity().getId(),
				promotion.getTitle(), promotion.getDescription(),
				promotion.getEffectiveDate(), promotion.getEndingDate());
	}

	@Override
	public void recordPromotionRead(final Integer promotionIdPk,
			final Integer userIdPk, final boolean promoRead)
			throws NotFoundException {
		final int idPk = sequencer.nextVal("ID_SEQ");
		final int recInserted = getJdbcTemplate().update(INS_PROMO_HISTORY,
				idPk, userIdPk, promoRead ? "Y" : "N", promotionIdPk);
		if (recInserted == 0) {
			throw new NotFoundException("promotionId " + promotionIdPk
					+ " not found!");
		}
	}

	@Override
	@Resource(name = "avatarDataSource")
	public void setDataSource(final DataSource ds) {
		initTemplate(ds);
	}

	@Override
	public void update(final Promotion promotion) throws NotFoundException {
		Assert.notNull(promotion);
		Assert.notNull(promotion.getId());

		// Check if id found
		getPromoIdPk(promotion.getId());

		getJdbcTemplate().update(UPD_PROMOTION_BY_PK, promotion.getTitle(),
				promotion.getDescription(), promotion.getId());
		if (promotion.getEffectiveDate() != null) {
			getJdbcTemplate().update(UPD_PROMOTION_EFFECTIVE_DATE_BY_PK,
					promotion.getEffectiveDate(), promotion.getId());
		}
		if (promotion.getEndingDate() != null) {
			getJdbcTemplate().update(UPD_PROMOTION_ENDINGDATE_BY_PK,
					promotion.getEndingDate(), promotion.getId());
		}
	}
}
