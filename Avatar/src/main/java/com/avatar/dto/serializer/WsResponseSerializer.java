package com.avatar.dto.serializer;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.avatar.dto.WsResponse;
import com.avatar.dto.enums.ResponseStatus;
import com.avatar.util.GSonBuilderUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class WsResponseSerializer implements JsonSerializer<WsResponse> {
	public static void main(final String[] args) {
		final GSonBuilderUtil builder = new GSonBuilderUtil();
		builder.register(WsResponse.class, new WsResponseSerializer());
		final Gson gson = builder.getGson();
		WsResponse<String> data = new WsResponse<String>(
				ResponseStatus.success, "", "This is a test", "testName");
		System.out.println(gson.toJson(data));
		data = new WsResponse<String>(ResponseStatus.success, "", null, "testName");
		System.out.println(gson.toJson(data));
		final List<String> myList = new LinkedList<String>();
		myList.add("1");
		myList.add("2");
		myList.add("3");
		final WsResponse<List<String>> data2 = new WsResponse<List<String>>(
				ResponseStatus.success, "", myList, "testName");
		System.out.println(gson.toJson(data2));
	}

	@Override
	public JsonElement serialize(final WsResponse response, final Type type,
			final JsonSerializationContext jsonContext) {
		JsonObject result;
		JsonElement element = null;
		if (response.getData() != null) {
			System.out.println("response.getDataType()=>"
					+ response.getDataType());
			if (response.getDataType() == null) {
				element = jsonContext.serialize(response.getData());
			} else {
				element = jsonContext.serialize(response.getData(),
						response.getDataType());
			}
		}

		if (element == null) {
			result = new JsonObject();
		} else if (element instanceof JsonArray) {
			result = new JsonObject();
			result.add(response.getDataName(), element);
		} else {
			try {
				System.out.println("Element class is " + element.getClass());
				result = element.getAsJsonObject();
			} catch (final IllegalStateException e) {
				result = new JsonObject();
				result.add(response.getDataName(), element.getAsJsonPrimitive());
			}
		}
		result.add("statusCode", new JsonPrimitive(response.getStatus()));
		result.add(
				"statusMessage",
				new JsonPrimitive(StringUtils.isEmpty(response
						.getStatusMessage()) ? "" : response.getStatusMessage()));
		return result;
	}
}
