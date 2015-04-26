package com.avatar.dto.serializer;

import java.lang.reflect.Type;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.avatar.dto.promotion.Promotion;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class PromotionSerializer implements JsonSerializer<Promotion> {
	protected final DateTimeFormatter yyyyMMddHHmmssDtf = DateTimeFormat
			.forPattern("yyyyMMdd HH:mm:ss");

	@Override
	public JsonElement serialize(final Promotion promotion, final Type type,
			final JsonSerializationContext context) {
		if (promotion != null) {
			final JsonObject result = new JsonObject();
			result.add("promotionId", new JsonPrimitive(promotion.getId()));
			result.add("clubId", new JsonPrimitive(promotion.getClub()
					.getClubId()));
			result.add("amenityId", new JsonPrimitive(promotion.getAmenity()
					.getAmenityId()));
			result.add("promotionTitle",
					new JsonPrimitive(promotion.getTitle()));
			result.add("promotionDetails",
					new JsonPrimitive(promotion.getDescription()));
			if (promotion.getEffectiveDate() != null) {
				result.add(
						"effectiveDate",
						new JsonPrimitive(yyyyMMddHHmmssDtf.print(promotion
								.getEffectiveDate().getTime())));
			}
			if (promotion.getEndingDate() != null) {
				result.add(
						"endingDate",
						new JsonPrimitive(yyyyMMddHHmmssDtf.print(promotion
								.getEndingDate().getTime())));
			}
			return result;
		}
		return null;
	}

}
