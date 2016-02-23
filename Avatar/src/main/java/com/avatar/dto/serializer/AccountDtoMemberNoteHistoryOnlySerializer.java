package com.avatar.dto.serializer;

import java.lang.reflect.Type;

import org.apache.commons.collections4.CollectionUtils;

import com.avatar.dto.account.AccountDto;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class AccountDtoMemberNoteHistoryOnlySerializer implements JsonSerializer<AccountDto> {

	@Override
	public JsonElement serialize(final AccountDto account, final Type type,
			final JsonSerializationContext jsonContext) {
		if (account != null) {
			final JsonObject result = new JsonObject();
			if(CollectionUtils.isNotEmpty(account.getNoteHistory())) {
				result.add("noteHistory", jsonContext.serialize(account.getNoteHistory()));
			}
			return result;
		}
		return null;
	}

}
