package com.avatar.dto.serializer;

import java.lang.reflect.Type;

import org.apache.commons.lang3.StringUtils;

import com.avatar.dto.club.AmenityDto;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class AmenityListingSerializer implements JsonSerializer<AmenityDto> {

	@Override
	public JsonElement serialize(final AmenityDto amenity, final Type type,
			final JsonSerializationContext jsonContext) {
		if (amenity != null) {
			final JsonObject result = new JsonObject();
			result.add("amenityId", new JsonPrimitive(amenity.getAmenityId()));
			result.add("amenityName", new JsonPrimitive(amenity.getDescription()));
			if (StringUtils.isNotEmpty(amenity.getHeader())) {
				result.add("header", new JsonPrimitive(amenity.getHeader()));
			}
			return result;
		}
		return null;
	}

}
