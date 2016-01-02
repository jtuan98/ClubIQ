package com.avatar.dto.serializer;

import java.lang.reflect.Type;

import org.apache.commons.lang3.StringUtils;

import com.avatar.dto.ImagePic;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ImagePicSerializer implements JsonSerializer<ImagePic> {

	@Override
	public JsonElement serialize(final ImagePic image, final Type type,
			final JsonSerializationContext context) {
		final JsonObject result = new JsonObject();
		if (image != null && StringUtils.isNotEmpty(image.getPictureAsBase64String())) {
			result.add("picture", new JsonPrimitive(image.getPictureAsBase64String()));
		}
		return result;
	}

}
