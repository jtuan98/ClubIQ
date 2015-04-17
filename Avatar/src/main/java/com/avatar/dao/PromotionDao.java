package com.avatar.dao;

import java.util.List;

import com.avatar.dto.promotion.Promotion;
import com.avatar.exception.NotFoundException;
import com.avatar.exception.PermissionDeniedException;

public interface PromotionDao {
	void delete(Integer promoIdPk) throws NotFoundException, PermissionDeniedException;

	Promotion getPromotion(Integer promoIdPk) throws NotFoundException;

	List<Promotion> getPromotions(Integer clubIdPk, Integer amenityIdPk);

	void newPromotion(Promotion promotion);

	void recordPromotionRead(Integer promotionIdPk, Integer userIdPk,
			boolean promoRead) throws NotFoundException;

	void update(Promotion promotion) throws NotFoundException;

}
