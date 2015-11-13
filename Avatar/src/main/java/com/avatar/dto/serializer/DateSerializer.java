package com.avatar.dto.serializer;

import java.lang.reflect.Type;
import java.util.Date;

import org.joda.time.format.DateTimeFormatter;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DateSerializer implements JsonSerializer<Date>{

	protected DateTimeFormatter dtf;

	public DateSerializer(final DateTimeFormatter dtf) {
		this.dtf =dtf;
	}

	@Override
	public JsonElement serialize(final Date date, final Type type,
			final JsonSerializationContext context) {
		JsonElement retVal = null;
		if (date != null) {
			retVal = new JsonPrimitive(dtf.print(date.getTime()));
		}
		return retVal;
	}

}
