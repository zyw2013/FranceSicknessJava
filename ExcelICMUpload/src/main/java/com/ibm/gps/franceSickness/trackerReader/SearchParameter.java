package com.ibm.gps.franceSickness.trackerReader;

import java.io.Serializable;


/**
 * The bean class for search parameters.
 * <p>
 * relationOperator are valid relationship oeratiors in SQL, such as >, <, >=,
 * <=, !=, =, like
 * 
 * 
 */
public class SearchParameter implements Serializable {
	public static String TYPE_STRING = "String";

	public static String TYPE_INT = "Int";

	public static String TYPE_FLOAT = "Float";

	public static String TYPE_BOOLEAN = "Boolean";

	public static String TYPE_PARTICIPANT = "Participant";

	public static String TYPE_TIME = "Time";

	public static String OPERATOR_LIKE = "like";

	/**
	 * 
	 */
	private static final long serialVersionUID = -285872732389865797L;

	/**
	 *@preserve
	 */
	private String paramName;

	/**
	 *@preserve
	 */
	private String paramType;

	/**
	 *@preserve
	 */
	private String paramValue;

	/**
	 *@preserve
	 */
	private String relationOperator;

	public SearchParameter() {

	}

	public SearchParameter(String paramName, String paramType,
			String paramValue, String relationOperator) {
		this.paramName = paramName;
		this.paramType = paramType;
		this.paramValue = paramValue;
		this.relationOperator = relationOperator;
	}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

	public String getRelationOperator() {
		return relationOperator;
	}

	public void setRelationOperator(String relationOperator) {
		this.relationOperator = relationOperator;
	}

	public String getParamType() {
		return paramType;
	}

	public void setParamType(String paramType) {
		this.paramType = paramType;
	}
}
