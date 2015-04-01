package com.avatar.dto;

import java.io.Serializable;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class ImagePic implements Serializable {

	public static void main(final String[] args) {
		final ImagePic pic = new ImagePic();
		pic.setPicture("123".getBytes());
		System.out.println(pic.getPictureAsBase64String());
	}

	protected Integer id;

	protected Integer userId;

	protected String imageHash;

	protected byte[] picture;

	public ImagePic() {
	}

	public ImagePic(final Integer id) {
		this.id = id;
	}

	public ImagePic(final String pictureBase64Encoded) {
		final byte[] binary = Base64.decodeBase64(pictureBase64Encoded);
		setPicture(binary);
		setImageHash(DigestUtils.md5Hex(binary));
	}

	public Integer getId() {
		return id;
	}

	public String getImageHash() {
		return imageHash;
	}

	public byte[] getPicture() {
		return picture;
	}

	public String getPictureAsBase64String() {
		final String base64 = new String(Base64.encodeBase64URLSafe(picture));
		return base64;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public void setImageHash(final String imageHash) {
		this.imageHash = imageHash;
	}

	public void setPicture(final byte[] picture) {
		this.picture = picture;
	}

	public void setUserId(final Integer userId) {
		this.userId = userId;
	}
}
