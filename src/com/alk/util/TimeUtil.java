package com.alk.util;

import java.text.SimpleDateFormat;

public class TimeUtil {
	static final String version = "TimeUtil 1.0";
	
	public static String convertToString(int minutes, int seconds){
		return convertSecondsToString(minutes*60+seconds);
	}
	public static String convertSecondsToString(long t){
	    long s = t % 60;
	    t /= 60;
	    long m = t %60;
	    t /=60;
	    long h = t % 24;
	    t /=24;
	    long d = t;
	    
	    if (d < 1 && m < 1 && h < 1){
	    	return s +" seconds";
	    }
	    if (d < 1 && h < 1 && s == 0){
	    	return m + " " + minOrMins(m);
	    }
	    if (d < 1){
	    	return "&6" + h +"&e "+hourOrHours(h)+" &6" + m+ "&e "+minOrMins(m)+" and &6" + s +"&e sec";	
	    } else {
	    	return "&6" + d + "&e "+dayOrDays(d)+" &6"+h +"&e "+hourOrHours(h)+" &6" + m+ "&e "+minOrMins(m);
	    }
	   }
	
	public static String convertToString(long t){
	    t = t / 1000;  
	    return convertSecondsToString(t);
	}
	
	private static String dayOrDays(long t){
		return t > 1 || t == 0? "days" : "day";
	}

	private static String hourOrHours(long t){
		return t > 1 || t ==0 ? "hours" : "hour";
	}

	private static String minOrMins(long t){
		return t > 1 || t == 0? "minutes" : "minute";
	}

	public static String convertLongToDate(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd hh:mm:ss");
		return sdf.format(time);
	}

	public static String PorP(int size) {
		return size == 1 ? "person" : "people";
	}
	
}
