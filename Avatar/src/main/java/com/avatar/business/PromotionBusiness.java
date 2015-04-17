package com.avatar.business;

import java.util.List;

import com.avatar.dto.promotion.Promotion;
import com.avatar.exception.NotFoundException;

public interface PromotionBusiness {
	Promotion getPromotion(Integer promoIdPk) throws NotFoundException;

	List<Promotion> getPromotions(String beaconId) throws NotFoundException;

	List<Promotion> getPromotions(String clubId, String amenityId)
			throws NotFoundException;

	void newPromotion(Promotion promotion) throws NotFoundException;

	void recordPromotionRead(Integer promotionIdPk, String userId,
			boolean promoRead) throws NotFoundException;

	void update(Promotion promotion) throws NotFoundException;
}
