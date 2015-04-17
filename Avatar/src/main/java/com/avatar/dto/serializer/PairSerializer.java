package com.avatar.dto.serializer;

import java.lang.reflect.Type;
import java.util.Date;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.avatar.dto.WsResponse;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.enums.ResponseStatus;
import com.avatar.util.GSonBuilderUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class PairSerializer implements JsonSerializer<Pair<Object, Object>> {
	public static void main(final String[] args) {
		final GSonBuilderUtil builder = new GSonBuilderUtil();
		builder.register(WsResponse.class, new WsResponseSerializer());
		builder.register(ImmutablePair.class, new PairSerializer());
		final Gson gson = builder.getGson();
		final Pair<String, String> kv = new ImmutablePair<String, String>("a",
				"bb");
		final WsResponse<Pair<String, String>> data = new WsResponse<Pair<String, String>>(
				ResponseStatus.success, "", kv, "test");
		System.out.println(gson.toJson(data));
	}

	protected final DateTimeFormatter yyyyMMddHHmmssDtf = DateTimeFormat
			.forPattern("yyyyMMdd HH:mm:ss");

	@Override
	public JsonElement serialize(final Pair<Object, Object> keyValue,
			final Type type, final JsonSerializationContext context) {
		if (keyValue != null) {
			if (keyValue.getKey() instanceof String) {
				final JsonObject result = new JsonObject();
				result.add((String)keyValue.getKey(),
						context.serialize(keyValue.getValue()));
				return result;
			} else if ((keyValue.getKey() instanceof AccountDto) && (keyValue.getValue() instanceof Date)) {
				return serializeAccountDtoCheckInDate(keyValue, type, context);
			}
		}

		return null;
	}

	private JsonElement serializeAccountDtoCheckInDate(
			final Pair<Object, Object> keyValue, final Type type,
			final JsonSerializationContext context) {
		if (keyValue != null) {
			final JsonElement element = context.serialize(keyValue.getKey());
			final JsonObject result = element.getAsJsonObject();
			result.add(
					"checkInDateTime",
					new JsonPrimitive(yyyyMMddHHmmssDtf.print(((Date)keyValue
							.getRight()).getTime())));
			return result;
		}
		return null;
	}

}
