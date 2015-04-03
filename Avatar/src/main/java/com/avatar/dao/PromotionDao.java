package com.avatar.dao;

import java.util.List;

import com.avatar.dto.promotion.Promotion;
import com.avatar.exception.NotFoundException;

public interface PromotionDao {

	List<Promotion> getPromotions(Integer clubIdPk, Integer amenityIdPk);

	void newPromotion(Promotion promotion);

	void recordPromotionRead(Integer promotionIdPk, Integer userIdPk,
			boolean promoRead) throws NotFoundException;

	void update(Promotion promotion) throws NotFoundException;

}
