package com.avatar.mvc.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

public class RenderingImageView implements View {
	private static final String HEIGHT = "HEIGHT";
	private static final String WIDTH = "WIDTH";
	private static final String DEF_CONTENT_TYPE = "image/jpeg";
	public static final String DATA = "data";
	private static final String CALLBACK = "callback";

	@Override
	public String getContentType() {
		return DEF_CONTENT_TYPE;
	}

	private String getFormat(final InputStream input) throws IOException {
		final ImageInputStream stream = ImageIO.createImageInputStream(input);

		final Iterator<ImageReader> iter = ImageIO.getImageReaders(stream);
		if (!iter.hasNext()) {
			return null;
		}
		final ImageReader reader = iter.next();
		final ImageReadParam param = reader.getDefaultReadParam();
		reader.setInput(stream, true, true);

		try {
			reader.read(0, param);
			return reader.getFormatName();
		} finally {
			reader.dispose();
			stream.close();
		}
	}

	private void init() {
	}

	public void register(final Class clazz, final Object serializer) {

	}

	@Override
	public void render(final Map<String, ?> model, final HttpServletRequest req,
			final HttpServletResponse resp) throws Exception {
		init();
		final Object data = model.get(DATA);
		if (data != null && data instanceof byte[]) {
			resp.setContentType(DEF_CONTENT_TYPE);
			resp.getOutputStream().write((byte[])data);
		}
	}
}