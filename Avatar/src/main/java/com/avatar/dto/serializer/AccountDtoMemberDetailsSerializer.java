package com.avatar.dto.serializer;

import java.lang.reflect.Type;

import org.apache.commons.collections4.CollectionUtils;

import com.avatar.dto.account.AccountDto;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

public class AccountDtoMemberDetailsSerializer extends AccountDtoMemberSummarySerializer {

	@Override
	public JsonElement serialize(final AccountDto account, final Type type,
			final JsonSerializationContext jsonContext) {
		if (account != null) {
			final JsonObject result = (JsonObject) super.serialize(account, type, jsonContext);
			String noteHistoryFlag = "N";
			if(CollectionUtils.isNotEmpty(account.getNoteHistory())) {
				result.add("noteHistory", jsonContext.serialize(account.getNoteHistory()));
				noteHistoryFlag = "Y";
			}
			result.add("noteHistoryFlag", new JsonPrimitive(noteHistoryFlag));
			return result;
		}
		return null;
	}

}
