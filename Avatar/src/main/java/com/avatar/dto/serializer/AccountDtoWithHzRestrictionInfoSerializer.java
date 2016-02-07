package com.avatar.dto.serializer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Map;

import com.avatar.dto.account.MemberAccountDto;
import com.avatar.util.ObjectUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class AccountDtoWithHzRestrictionInfoSerializer implements JsonSerializer<MemberAccountDto>  {

	@Override
	public JsonElement serialize(final MemberAccountDto account, final Type type,
			final JsonSerializationContext jsonContext) {
		if (account != null) {
			final JsonObject result=new JsonObject();
			try {
				for (  final Map.Entry<String,Object> entry : ObjectUtil.ConvertObjectToMap(account).entrySet()) {
					result.add(entry.getKey(),jsonContext.serialize(entry.getValue()));
				}
				if(account.getHomeClub() != null) {
					result.add("HZRESTRICTION", jsonContext.serialize(account.getHomeClub().getHzRestriction()));
					result.add("X_coord", jsonContext.serialize(account.getHomeClub().getXcoord()));
					result.add("Y_coord", jsonContext.serialize(account.getHomeClub().getYcoord()));
				}
				return result;
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
			}
		}
		return null;
	}

}
