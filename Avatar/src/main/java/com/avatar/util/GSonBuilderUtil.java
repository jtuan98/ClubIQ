package com.avatar.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GSonBuilderUtil {
	private GsonBuilder builder = new GsonBuilder();
	
	public void register(Class clazz, Object serializer) {
		builder.registerTypeAdapter(clazz, serializer);
	}

	public Gson getGson() {
		return builder.create();
	}
}
