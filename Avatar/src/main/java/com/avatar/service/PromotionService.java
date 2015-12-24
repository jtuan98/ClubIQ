package com.avatar.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.avatar.business.PromotionBusiness;
import com.avatar.dao.AccountDao;
import com.avatar.dao.BeaconDao;
import com.avatar.dao.ClubDao;
import com.avatar.dao.PromotionDao;
import com.avatar.dto.club.ClubDto;
import com.avatar.dto.club.SubAmenityDto;
import com.avatar.dto.promotion.Promotion;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.PermissionDeniedException;

@Service
public class PromotionService extends BaseService implements PromotionBusiness {
	@Resource(name = "promotionDaoJdbc")
	private PromotionDao promotionDao;

	@Resource(name = "clubDaoJdbc")
	private ClubDao clubDao;

	@Resource(name = "accountDaoJdbc")
	private AccountDao accountDao;

	@Resource(name = "beaconDaoJdbc")
	private BeaconDao beaconDao;

	@Override
	public void cleanupPromoBeaconInfo(final String mobileNumber,
			final Date fromDate, final Date toDate) throws NotFoundException {
		final Integer userIdPk = accountDao.getUserIdPkByUserId(mobileNumber);

		promotionDao.delete(userIdPk, fromDate, toDate);
		beaconDao.deleteBeaconInfoByUserId(userIdPk, fromDate, toDate);
	}

	@Override
	public void delete(final Integer promoIdPk) throws NotFoundException,
	PermissionDeniedException {
		promotionDao.delete(promoIdPk);
	}

	@Override
	public Promotion getPromotion(final Integer promoIdPk)
			throws NotFoundException {
		final Promotion promotion = promotionDao.getPromotion(promoIdPk);
		final ClubDto club = clubDao.get(promotion.getClub().getId(), false);
		final SubAmenityDto subAmenity = clubDao.getSubAmenity(promotion.getSubAmenity()
				.getId());
		promotion.setClub(club);
		promotion.setSubAmenity(subAmenity);
		return promotion;
	}

	@Override
	public List<Promotion> getPromotions(final String beaconId)
			throws NotFoundException {
		final Integer beaconIdPk = beaconDao.getBeaconIdPk(beaconId);
		final Integer clubIdPk = beaconDao.getClubIdPkByBeaconIdPk(beaconIdPk);
		final Integer subAmenityIdPk = beaconDao.getSubAmenityIdPk(beaconIdPk);

		final List<Promotion> promotions = promotionDao.getValidPromotions(
				clubIdPk, subAmenityIdPk);
		if (CollectionUtils.isNotEmpty(promotions)) {
			final ClubDto club = clubDao.get(clubIdPk, false);
			final SubAmenityDto subAmenity = clubDao.getSubAmenity(subAmenityIdPk);
			for (final Promotion promotion : promotions) {
				promotion.setClub(club);
				promotion.setSubAmenity(subAmenity);
			}
		}
		return promotions;
	}

	@Override
	public List<Promotion> getPromotions(final String clubId,
			final String subAmenityId) throws NotFoundException {
		final Integer clubIdPk = clubDao.getClubIdPk(clubId);
		final Integer subAmenityIdPk = clubDao.getClubSubAmenityIdPk(clubIdPk, subAmenityId);
		final List<Promotion> promotions = promotionDao.getAllPromotions(
				clubIdPk, subAmenityIdPk);
		if (CollectionUtils.isNotEmpty(promotions)) {
			final ClubDto club = clubDao.get(clubIdPk, true);
			final SubAmenityDto subAmenity = clubDao.getSubAmenity(subAmenityIdPk);
			for (final Promotion promotion : promotions) {
				promotion.setClub(club);
				promotion.setSubAmenity(subAmenity);
			}
		}
		return promotions;
	}

	@Override
	public void newPromotion(final Promotion promotion)
			throws NotFoundException {
		populatePkForClubsAndSubAmenity(promotion);
		promotionDao.newPromotion(promotion);
	}

	private void populatePkForClubsAndSubAmenity(final Promotion promotion)
			throws NotFoundException {
		Assert.notNull(promotion);
		Assert.notNull(promotion.getClub());
		Assert.hasText(promotion.getClub().getClubId());
		Assert.notNull(promotion.getSubAmenity());
		Assert.hasText(promotion.getSubAmenity().getSubAmenityId());
		final Integer clubIdPk = clubDao.getClubIdPk(promotion.getClub()
				.getClubId());
		final Integer subAmenityIdPk = clubDao.getClubSubAmenityIdPk(clubIdPk, promotion
				.getSubAmenity().getSubAmenityId());
		promotion.getClub().setId(clubIdPk);
		promotion.getSubAmenity().setId(subAmenityIdPk);
	}

	@Override
	public void recordPromotionRead(final Integer promotionIdPk,
			final String userId, final boolean promoRead)
					throws NotFoundException {
		final Integer userIdPk = accountDao.getUserIdPkByUserId(userId);
		promotionDao.recordPromotionRead(promotionIdPk, userIdPk, promoRead);
	}

	@Override
	public void update(final Promotion promotion) throws NotFoundException {
		promotionDao.update(promotion);
	}
}
