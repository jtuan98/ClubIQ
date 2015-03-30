package com.avatar.mvc.view;

import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.View;

import com.avatar.util.GSonBuilderUtil;
import com.google.gson.Gson;

public class JsonView implements View {
	private static final String DEF_JSON_CONTENT_TYPE = "application/json; charset=UTF-8";
	private static final String DEF_JS_CONTENT_TYPE = "text/javascript; charset=UTF-8";
	public static final String DATA = "data";
	private static final String CALLBACK = "callback";

	private final GSonBuilderUtil builder = new GSonBuilderUtil();
	private Gson gson = null;

	@Override
	public String getContentType() {
		return DEF_JSON_CONTENT_TYPE;
	}

	private void init() {
		if (gson == null) {
			gson = builder.getGson();
		}
	}

	public void register(final Class clazz, final Object serializer) {
		builder.register(clazz, serializer);
	}

	@Override
	public void render(final Map<String, ?> model, final HttpServletRequest req,
			final HttpServletResponse resp) throws Exception {
		init();
		final Object data = model.get(DATA);
		final String cb = req.getParameter(CALLBACK);
		if (StringUtils.isNotEmpty(cb)) {
			resp.setContentType(DEF_JS_CONTENT_TYPE);
		} else {
			resp.setContentType(DEF_JSON_CONTENT_TYPE);
		}

		final PrintWriter out = resp.getWriter();
		if (StringUtils.isNotEmpty(cb)) {
			out.write(StringEscapeUtils.escapeHtml4(cb) + "(");
		}
		if (data != null) {
			final String json = gson.toJson(data);
			out.write(json);
			System.out.println(json);
		}
		if (StringUtils.isNotEmpty(cb)) {
			out.write(");");
		}
	}
}