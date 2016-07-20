package com.github.zjzcn.ceper.utils;

import java.io.Serializable;

public abstract class BaseBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		return JsonUtils.toJsonString(this);
	}
}
