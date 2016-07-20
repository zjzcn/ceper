package com.github.zjzcn.ceper.rule;

import java.util.HashMap;
import java.util.Map;

import com.github.zjzcn.ceper.utils.BaseBean;

public class Defination extends BaseBean {

	private static final long serialVersionUID = 1L;

	private String dataType;
	
	private Map<String, Object> fieldMap = new HashMap<String, Object>();

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	
	public Map<String, Object> getFieldMap() {
		return fieldMap;
	}

	public void setFieldMap(Map<String, Object> fieldMap) {
		this.fieldMap = fieldMap;
	}

	@Override
	public int hashCode() {
		return dataType.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		Defination other = (Defination)obj;
		return dataType.equals(other.dataType);
	}
}
