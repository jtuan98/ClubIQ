package com.avatar.dto.serializer;

import java.lang.reflect.Type;

import org.apache.commons.lang3.StringUtils;

import com.avatar.dto.club.ClubDto;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ClubListingSerializer implements JsonSerializer<ClubDto> {

	@Override
	public JsonElement serialize(final ClubDto club, final Type type,
			final JsonSerializationContext jsonContext) {
		if (club != null) {
			final JsonObject result = new JsonObject();
			result.add("clubId", new JsonPrimitive(club.getClubId()));
			result.add("clubName", new JsonPrimitive(club.getClubName()));
			if (StringUtils.isNotEmpty(club.getStateLongName())) {
				result.add("state", new JsonPrimitive(club.getStateLongName()));
			} else if (StringUtils.isNotEmpty(club.getState())) {
				result.add("state", new JsonPrimitive(club.getState()));
			}
			if (StringUtils.isNotEmpty(club.getXcoord())) {
				result.add("xcoord", new JsonPrimitive(club.getXcoord()));
			}
			if (StringUtils.isNotEmpty(club.getYcoord())) {
				result.add("ycoord", new JsonPrimitive(club.getYcoord()));
			}
			return result;
		}
		return null;
	}

}
