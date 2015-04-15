package com.avatar.dto.serializer;

import java.lang.reflect.Type;
import java.util.Date;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.avatar.dto.WsResponse;
import com.avatar.dto.account.AccountDto;
import com.avatar.dto.account.EmployeeAccountDto;
import com.avatar.dto.enums.ResponseStatus;
import com.avatar.util.GSonBuilderUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class AccountDtoCheckInDateSerializer implements JsonSerializer<Pair<AccountDto, Date>> {
	public static void main(final String[] args) {
		final GSonBuilderUtil builder = new GSonBuilderUtil();
		builder.register(WsResponse.class, new WsResponseSerializer());
		builder.register(ImmutablePair.class, new PairSerializer());
		builder.register(ImmutablePair.class, new AccountDtoCheckInDateSerializer());
		final Gson gson = builder.getGson();
		final AccountDto account = new EmployeeAccountDto();
		account.setAddress("address");
		account.setDeviceId("deviceId");
		account.setEmail("whatever@yahoo.com");
		account.setName("test");
		final Pair<AccountDto, Date> kv = new ImmutablePair<AccountDto, Date>(account, new Date());
		final WsResponse<Pair<AccountDto, Date>> data = new WsResponse<Pair<AccountDto, Date>>(
				ResponseStatus.success, "", kv, "test");
		System.out.println(gson.toJson(data));
	}

	protected final DateTimeFormatter yyyyMMddHHmmssDtf = DateTimeFormat.forPattern("yyyyMMdd HH:mm:ss");

	@Override
	public JsonElement serialize(final Pair<AccountDto, Date> keyValue,
			final Type type, final JsonSerializationContext context) {
		if (keyValue != null) {
			final JsonElement element = context.serialize(keyValue.getKey());
			final JsonObject result = element.getAsJsonObject();
			result.add("checkInDateTime", new JsonPrimitive(yyyyMMddHHmmssDtf.print(keyValue.getRight().getTime())));
			return result;
		}
		return null;
	}

}
