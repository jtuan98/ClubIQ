package com.avatar.service;

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
import com.avatar.dto.club.AmenityDto;
import com.avatar.dto.club.ClubDto;
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
	public void delete(final Integer promoIdPk) throws NotFoundException,
			PermissionDeniedException {
		promotionDao.delete(promoIdPk);
	}

	@Override
	public Promotion getPromotion(final Integer promoIdPk) throws NotFoundException {
		final Promotion promotion = promotionDao.getPromotion(promoIdPk);
		final ClubDto club = clubDao.get(promotion.getClub().getId(), false);
		final AmenityDto amenity = clubDao.getAmenity(promotion.getAmenity().getId());
		promotion.setClub(club);
		promotion.setAmenity(amenity);
		return promotion;
	}

	@Override
	public List<Promotion> getPromotions(final String beaconId)
			throws NotFoundException {
		final Integer beaconIdPk = beaconDao.getBeaconIdPk(beaconId);
		final Integer clubIdPk = beaconDao.getClubIdPkByBeaconIdPk(beaconIdPk);
		final Integer amenityIdPk = beaconDao.getAmenityIdPk(beaconIdPk);

		final List<Promotion> promotions = promotionDao.getValidPromotions(clubIdPk,
				amenityIdPk);
		if (CollectionUtils.isNotEmpty(promotions)) {
			final ClubDto club = clubDao.get(clubIdPk, false);
			final AmenityDto amenity = clubDao.getAmenity(amenityIdPk);
			for (final Promotion promotion : promotions) {
				promotion.setClub(club);
				promotion.setAmenity(amenity);
			}
		}
		return promotions;
	}

	@Override
	public List<Promotion> getPromotions(final String clubId, final String amenityId)
			throws NotFoundException {
		final Integer clubIdPk = clubDao.getClubIdPk(clubId);
		final Integer amenityIdPk = clubDao.getClubAmenityIdPk(amenityId);
		final List<Promotion> promotions = promotionDao.getAllPromotions(clubIdPk,
				amenityIdPk);
		if (CollectionUtils.isNotEmpty(promotions)) {
			final ClubDto club = clubDao.get(clubIdPk, true);
			final AmenityDto amenity = clubDao.getAmenity(amenityIdPk);
			for (final Promotion promotion : promotions) {
				promotion.setClub(club);
				promotion.setAmenity(amenity);
			}
		}
		return promotions;
	}

	@Override
	public void newPromotion(final Promotion promotion)
			throws NotFoundException {
		populatePkForClubsAndAmenity(promotion);
		promotionDao.newPromotion(promotion);
	}

	private void populatePkForClubsAndAmenity(final Promotion promotion) throws NotFoundException {
		Assert.notNull(promotion);
		Assert.notNull(promotion.getClub());
		Assert.hasText(promotion.getClub().getClubId());
		Assert.notNull(promotion.getAmenity());
		Assert.hasText(promotion.getAmenity().getAmenityId());
		final Integer clubIdPk = clubDao.getClubIdPk(promotion.getClub()
				.getClubId());
		final Integer amenityIdPk = clubDao.getClubAmenityIdPk(promotion
				.getAmenity().getAmenityId());
		promotion.getClub().setId(clubIdPk);
		promotion.getAmenity().setId(amenityIdPk);
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
