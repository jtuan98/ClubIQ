package com.avatar.dto.serializer;

import java.lang.reflect.Type;

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
			result.add("type", new JsonPrimitive(amenity.getAmenityType()));
			result.add("name", new JsonPrimitive(amenity.getName()));
			return result;
		}
		return null;
	}

}
