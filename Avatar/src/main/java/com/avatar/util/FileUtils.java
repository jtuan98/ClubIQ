package com.avatar.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {
	/**
	   Read an input stream, and return it as a byte array.
	   Sometimes the source of bytes is an input stream instead of a file.
	   This implementation closes aInput after it's read.
	 * @throws IOException
	 */
	public static byte[] readAndClose(final InputStream aInput) throws IOException{
		ByteArrayOutputStream result = null;
		if (aInput != null) {
			//carries the data from input to output :
			final byte[] bucket = new byte[32*1024];
			try {
				//Use buffering? No. Buffering avoids costly access to disk or network;
				//buffering to an in-memory stream makes no sense.
				result = new ByteArrayOutputStream(bucket.length);
				int bytesRead = 0;
				while(bytesRead != -1){
					//aInput.read() returns -1, 0, or more :
					bytesRead = aInput.read(bucket);
					if(bytesRead > 0){
						result.write(bucket, 0, bytesRead);
					}
				}
			}
			finally {
				aInput.close();
				//result.close(); this is a no-operation for ByteArrayOutputStream
			}
		}
		return result != null? result.toByteArray(): null;
	}

}
