package com.avatar.dao.impl.jdbc;

import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.avatar.dao.PromotionDao;
import com.avatar.dao.impl.jdbc.mapper.PromotionMapper;
import com.avatar.dto.promotion.Promotion;
import com.avatar.exception.NotFoundException;

@Repository
public class PromotionDaoJdbc extends BaseJdbcDao implements PromotionDao {

	private static String GET_PROMOTIONS_BY_CLUBID_AMENITYID = "SELECT * FROM PROMOTIONS WHERE CLUB_ID = ? AND CLUB_AMENITY_ID = ? AND CURDATE() > EFFECTIVE_DATE AND CURDATE() < ENDING_DATE ";

	private final PromotionMapper promotionMapper = new PromotionMapper();

	private static String INS_PROMO_HISTORY = "INSERT INTO PROMOTION_HISTORY ("
			+ "      ID,PROMOTION_ID,CLUB_ID,CLUB_AMENITY_ID,USER_ID,PROMO_READ,CREATE_DATE) "
			+ "SELECT ?,ID,CLUB_ID,CLUB_AMENITY_ID,       ?,        ?, NOW() "
			+ "FROM PROMOTIONS WHERE ID = ? ";

	private static String INS_NEW_PROMOTION = "INSERT INTO PROMOTIONS ("
			+ "ID, CLUB_ID, CLUB_AMENITY_ID,	TITLE, DETAILS, EFFECTIVE_DATE, ENDING_DATE, CREATE_DATE) "
			+ "VALUES(?,?,?,?,?,?,?,NOW())";

	@Override
	public List<Promotion> getPromotions(final Integer clubIdPk,
			final Integer amenityIdPk) {
		final List<Promotion> retVal = getJdbcTemplate().query(
				GET_PROMOTIONS_BY_CLUBID_AMENITYID, promotionMapper, clubIdPk,
				amenityIdPk);
		return retVal;
	}

	@Override
	public void newPromotion(final Promotion promotion) {
		final int idPk = sequencer.nextVal("ID_SEQ");
		getJdbcTemplate().update(INS_NEW_PROMOTION, idPk,
				promotion.getClub().getId(), promotion.getAmenity().getId(),
				promotion.getTitle(), promotion.getDescription(),
				promotion.getEffectiveDate(), promotion.getEndingDate());
	}

	@Override
	public void recordPromotionRead(final Integer promotionIdPk,
			final Integer userIdPk, final boolean promoRead) throws NotFoundException {
		final int idPk = sequencer.nextVal("ID_SEQ");
		final int recInserted = getJdbcTemplate().update(INS_PROMO_HISTORY, idPk,
				userIdPk, promoRead ? "Y" : "N", promotionIdPk);
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

}
