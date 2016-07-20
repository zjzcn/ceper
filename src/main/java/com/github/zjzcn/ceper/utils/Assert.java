package com.github.zjzcn.ceper.utils;

public final class Assert {

	public static boolean isTrue(boolean expression, String message) {
		if (!expression) {
			throw new IllegalArgumentException(message);
		}
		return expression;
	}

	public static boolean isTrue(boolean expression) {
		return isTrue(expression, "[Assertion failed] - this expression must be true");
	}

	public static <T> T notNull(T object, String message) {
		if (object == null) {
			throw new IllegalArgumentException(message);
		}
		return object;
	}

	public static <T> T notNull(T object) {
		return notNull(object, "[Assertion failed] - this argument is required; it must not be null");
	}

	public static String notEmpty(String text, String message) {
		if (text==null || text.isEmpty()) {
			throw new IllegalArgumentException(message);
		}
		return text;
	}

	public static String notEmpty(String text) {
		return notEmpty(text, "[Assertion failed] - this String argument must have length; it must not be null or empty");
	}

	public static String notBlank(String text, String message) {
		if (text==null || text.isEmpty())  {
			throw new IllegalArgumentException(message);
		}
		return text;
	}

	public static String notBlank(String text) {
		return notBlank(text, "[Assertion failed] - this String argument must have text; it must not be null, empty, or blank");
	}

}

