package com.avatar.business;

import java.util.List;

import com.avatar.dto.promotion.Promotion;
import com.avatar.exception.NotFoundException;

public interface PromotionBusiness {

	List<Promotion> getPromotions(String beaconId) throws NotFoundException;

	void newPromotion(Promotion promotion) throws NotFoundException;

	void recordPromotionRead(Integer promotionIdPk, String userId,
			boolean promoRead) throws NotFoundException;
}
