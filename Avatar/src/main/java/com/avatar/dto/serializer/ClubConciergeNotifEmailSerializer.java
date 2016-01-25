package com.avatar.dto.serializer;

import java.lang.reflect.Type;

import org.apache.commons.lang3.StringUtils;

import com.avatar.dto.club.ClubDto;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ClubConciergeNotifEmailSerializer implements JsonSerializer<ClubDto> {

	@Override
	public JsonElement serialize(final ClubDto club, final Type type,
			final JsonSerializationContext jsonContext) {
		if (club != null) {
			final JsonObject result = new JsonObject();
			result.add("clubId", new JsonPrimitive(club.getClubId()));
			if (club.getConcierge() != null && StringUtils.isNotEmpty(club.getConcierge().getNotifEmail())) {
				result.add("notifEmail", new JsonPrimitive(club.getConcierge().getNotifEmail()));
			} else {
				result.add("notifEmail", new JsonPrimitive(""));
			}
			return result;
		}
		return null;
	}

}
