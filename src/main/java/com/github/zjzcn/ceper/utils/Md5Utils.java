package com.github.zjzcn.ceper.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Md5Utils {
	private static Logger logger = LoggerFactory.getLogger(Md5Utils.class);

	private static final String MD_MD5 = "MD5";
	private static final String MD_SHA = "SHA";
	private static final String MD_SHA_256 = "SHA-256";

	private static Charset charset = Charset.forName("UTF-8");

	private static MessageDigest getDigest(String algorithm) {
		try {
			return MessageDigest.getInstance(algorithm);
		} catch (Exception e) {
			logger.error("digest error, algorithm[{}]", algorithm);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Calculates the MD5 digest and returns the value as a 16 element
	 * <code>byte[]</code>.
	 * 
	 * @param data
	 *            Data to digest
	 * @return MD5 digest
	 */
	public static byte[] md5(byte[] data) {
		return getDigest(MD_MD5).digest(data);
	}

	/**
	 * MD5 16进制
	 * 
	 * @param data
	 * @return
	 */
	public static String md5Hex(String data) {
		return HexUtils.byte2hex(md5(data.getBytes(charset)));
	}

	/**
	 * SHA 消息摘要
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] sha(byte[] data) {
		return getDigest(MD_SHA).digest(data);
	}

	/**
	 * Calculates the SHA-1 digest and returns the value as a hex string.
	 * 
	 * @param data
	 *            Data to digest
	 * @return SHA-1 digest as a hex string
	 * @throws UnsupportedEncodingException
	 */
	public static String shaHex(String data) {
		return HexUtils.byte2hex(sha(data.getBytes(charset)));
	}

	/**
	 * SHA 消息摘要
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] sha256(byte[] data) {
		return getDigest(MD_SHA_256).digest(data);
	}

	/**
	 * Calculates the SHA-1 digest and returns the value as a hex string.
	 * 
	 * @param data
	 *            Data to digest
	 * @return SHA-1 digest as a hex string
	 * @throws UnsupportedEncodingException
	 */
	public static String sha256Hex(String data) {
		return HexUtils.byte2hex(sha256(data.getBytes(charset)));
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		String msg = "aachina";
		System.out.println(md5Hex(msg).toLowerCase());
		System.out.println(shaHex(msg));
		System.out.println(sha256Hex(msg));
		// byte[] c = Base64.decodeBase64(msg.getBytes());
		// byte[] key = md5("a1+b2=c3".getBytes());
		// byte[] b = decrypt3DES(key, c);
		// System.out.println(new String(b, "UTF-8"));
	}
}
