package com.github.zjzcn.ceper.utils;

/**
 * 编码解码工具类
 * 
 * @author zhangjz
 * @version [1.0.0 2013-05-07]
 */
public class HexUtils {
	private static final char[] DIGITS_UPPER = "0123456789ABCDEF".toCharArray();

	public static byte[] hex2byte(String hexString) {
		Assert.notNull(hexString);

		int len = hexString.length();

		byte[] out = new byte[len >> 1];

		// two characters form the hex value.
		for (int i = 0, j = 0; j < len; i++) {
			int f = Character.digit(hexString.charAt(j), 16) << 4;
			j++;
			f = f | Character.digit(hexString.charAt(j), 16);
			j++;
			out[i] = (byte) (f & 0xFF);
		}

		return out;
	}

	public static String byte2hex(byte[] data) {
		int l = data.length;
		char[] out = new char[l << 1];
		// two characters form the hex value.
		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = DIGITS_UPPER[(0xF0 & data[i]) >>> 4];
			out[j++] = DIGITS_UPPER[0x0F & data[i]];
		}
		return new String(out);
	}

	public static void main(String[] args) {
		String hString = "46333430303035567141674E46714D44";
		System.out.println(new String(hex2byte(hString)));
	}
}
