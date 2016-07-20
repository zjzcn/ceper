package com.github.zjzcn.ceper.rule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.zjzcn.ceper.utils.BaseBean;

public class Rule extends BaseBean {

	private static final long serialVersionUID = 1L;
	
	private String statementId;
	/*
	 * processor: esper/outlier
	 */
	private String processorType; 
	
	private String statement;
	
	private Map<String, Object> attachments = new HashMap<String, Object>();
 
	private Set<Defination> definations = new HashSet<Defination>();
	
	public String getStatementId() {
		return statementId;
	}

	public void setStatementId(String statementId) {
		this.statementId = statementId;
	}

	public String getProcessorType() {
		return processorType;
	}

	public void setProcessorType(String processorType) {
		this.processorType = processorType;
	}

	public String getStatement() {
		return statement;
	}

	public void setStatement(String statement) {
		this.statement = statement;
	}

	public Map<String, Object> getAttachments() {
		return attachments;
	}

	public void setAttachments(Map<String, Object> attachments) {
		this.attachments = attachments;
	}

	public Set<Defination> getDefinations() {
		return definations;
	}

	public void setDefinations(Set<Defination> definations) {
		this.definations = definations;
	}

	public void addDefinations(Defination defination) {
		if(definations == null) {
			definations = new HashSet<>();
		}
		definations.add(defination);
	}

	
	@Override
	public int hashCode() {
		return statementId.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		Rule other = (Rule)obj;
		return statementId.equals(other.statementId);
	}
}
