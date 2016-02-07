package com.avatar.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ObjectUtil {
	public static Map<String, Object> ConvertObjectToMap(final Object obj)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		final Method[] methods = obj.getClass().getMethods();

		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		for (final Method m : methods) {
			if (m.getName().startsWith("get")
					&& !m.getName().startsWith("getClass")) {
				final Object value = m.invoke(obj);
				final String key = m.getName().substring(3,4).toLowerCase() + m.getName().substring(4) ;
				map.put(key, value);
			}
		}
		return map;
	}
}
