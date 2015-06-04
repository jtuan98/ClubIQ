package com.avatar.dto;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class ImagePicBuilder implements DtoBuilder<ImagePic> {
	private final ImagePic image = mock(ImagePic.class);

	@Override
	public ImagePic getBuiltInstance() {
		return image;
	}

	public ImagePicBuilder withId(final Integer id) {
		given(image.getId()).willReturn(id);
		return this;
	}

	public ImagePicBuilder withImageHash(final String imageHash) {
		given(image.getImageHash()).willReturn(imageHash);
		return this;
	}

	public ImagePicBuilder withPicture(final byte[] picture) {
		given(image.getPicture()).willReturn(picture);
		return this;
	}

	public ImagePicBuilder withUserId(final Integer userId) {
		given(image.getUserId()).willReturn(userId);
		return this;
	}

}
