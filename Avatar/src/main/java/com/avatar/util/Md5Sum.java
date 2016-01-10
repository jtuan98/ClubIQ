package com.avatar.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class Md5Sum {

	public static String hashString(final byte[] data) {
		return DigestUtils.md5Hex(data);
	}

	public static String hashStringBase64Data(final String dataBase64) {
		final byte[] data = Base64.decodeBase64(dataBase64) ;
		return DigestUtils.md5Hex(data);
	}


}
