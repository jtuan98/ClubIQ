package com.avatar.dto.serializer;

import java.lang.reflect.Type;

import org.apache.commons.lang3.StringUtils;

import com.avatar.dto.account.AccountDto;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class AccountDtoMinimalSerializer implements JsonSerializer<AccountDto> {

	@Override
	public JsonElement serialize(final AccountDto account, final Type type,
			final JsonSerializationContext jsonContext) {
		if (account != null) {
			final JsonObject result = new JsonObject();
			result.add("userId", new JsonPrimitive(account.getUserId()));
			if (account.getLastCheckInDate() != null) {
				result.add("lastCheckInDate", jsonContext.serialize(account.getLastCheckInDate()));
			}
			if (StringUtils.isNotEmpty(account.getEmail())) {
				result.add("email", new JsonPrimitive(account.getEmail()));
			}
			if (StringUtils.isNotEmpty(account.getMobileNumber())) {
				result.add("mobileNumber", new JsonPrimitive(account.getMobileNumber()));
			}
			if (account.getPicture() != null && StringUtils.isNotEmpty(account.getPicture().getPictureAsBase64String())) {
				result.add("picture", new JsonPrimitive(account.getPicture().getPictureAsBase64String()));
			}
			if (StringUtils.isNotEmpty(account.getName())) {
				result.add("realname", new JsonPrimitive(account.getName()));
			}
			if (account.getHomeClub() != null && StringUtils.isNotEmpty(account.getHomeClub().getClubId())) {
				result.add("homeClubId", new JsonPrimitive(account.getHomeClub().getClubId()));
			}
			return result;
		}
		return null;
	}

}
