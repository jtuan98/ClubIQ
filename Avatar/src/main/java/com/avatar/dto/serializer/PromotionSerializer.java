package com.avatar.dto.serializer;

import java.lang.reflect.Type;

import com.avatar.dto.promotion.Promotion;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class PromotionSerializer implements JsonSerializer<Promotion>{

	@Override
	public JsonElement serialize(final Promotion promotion, final Type type,
			final JsonSerializationContext context) {
		if (promotion != null) {
			final JsonObject result = new JsonObject();
			result.add("promotionId", new JsonPrimitive(promotion.getId()));
			result.add("clubId", new JsonPrimitive(promotion.getClub().getClubId()));
			result.add("amenityId", new JsonPrimitive(promotion.getAmenity().getAmenityId()));
			result.add("promotionTitle", new JsonPrimitive(promotion.getTitle()));
			result.add("promotionDetails", new JsonPrimitive(promotion.getDescription()));
			return result;
		}
		return null;
	}

}
