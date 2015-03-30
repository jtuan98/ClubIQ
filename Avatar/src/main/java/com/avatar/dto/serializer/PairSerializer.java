package com.avatar.dto.serializer;

import java.lang.reflect.Type;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.avatar.dto.WsResponse;
import com.avatar.dto.enums.ResponseStatus;
import com.avatar.util.GSonBuilderUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class PairSerializer implements JsonSerializer<Pair<String, String>> {
	public static void main(final String[] args) {
		final GSonBuilderUtil builder = new GSonBuilderUtil();
		builder.register(WsResponse.class, new WsResponseSerializer());
		builder.register(ImmutablePair.class, new PairSerializer());
		final Gson gson = builder.getGson();
		final Pair<String, String> kv = new ImmutablePair<String, String>("a", "bb");
		final WsResponse<Pair<String, String>> data = new WsResponse<Pair<String, String>>(
				ResponseStatus.success, "", kv, "test");
		System.out.println(gson.toJson(data));
	}

	@Override
	public JsonElement serialize(final Pair<String, String> keyValue,
			final Type type, final JsonSerializationContext context) {
		if (keyValue != null) {
			final JsonObject result = new JsonObject();
			result.add(keyValue.getKey(),
					context.serialize(keyValue.getValue()));
			return result;
		}
		return null;
	}

}
