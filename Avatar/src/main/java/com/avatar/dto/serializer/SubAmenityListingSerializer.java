package com.avatar.dto.serializer;

import java.lang.reflect.Type;

import com.avatar.dto.club.SubAmenityDto;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class SubAmenityListingSerializer implements JsonSerializer<SubAmenityDto> {

	@Override
	public JsonElement serialize(final SubAmenityDto subamenity, final Type type,
			final JsonSerializationContext jsonContext) {
		if (subamenity != null) {
			final JsonObject result = new JsonObject();
			result.add("subamenityId", new JsonPrimitive(subamenity.getSubAmenityId()));
			result.add("amenityId", new JsonPrimitive(subamenity.getAmenityId()));
			return result;
		}
		return null;
	}

}