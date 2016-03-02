/*******************************************************************************************
 * (C) COPYRIGHT IBM Corporation 2009, 2010
 * All Rights Reserved. 
 *
 * Licensed Materials-Property of IBM																				
 ******************************************************************************************/
package com.ibm.gps.franceSickness.util;
public class StringUtil {

    public static final String CHARSET_UTF_8 = "UTF-8";

    public static boolean isEmpty(String text) {
        return !isNotEmpty(text);
    }

    public static boolean isNotEmpty(String text) {
        return text != null && text.trim().length() != 0;
    }

}
