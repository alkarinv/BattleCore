package mc.alk.v1r7.util;

import java.text.SimpleDateFormat;

public class TimeUtil {
	static final String version = "1.1";

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
		boolean has = false;
		StringBuilder sb = new StringBuilder();
		if (d > 0) {
			has=true;
			sb.append("&6"+ d +"&e "+dayOrDays(d));
		}
		if (h > 0) {
			has=true;
			sb.append("&6"+ h +"&e "+hourOrHours(h));
		}
		if (m > 0) {
			has=true;
			sb.append("&6"+ m +"&e "+minOrMins(m));
		}
		if (s > 0) {
			has=true;
			sb.append("&6"+ s +"&e "+secOrSecs(s));
		}
		if (!has){
			return "&60";
		}
		return sb.toString();
	}

	public static String convertToString(long t){
		t = t / 1000;
		return convertSecondsToString(t);
	}

	public static String dayOrDays(long t){
		return t > 1 || t == 0? "days" : "day";
	}

	public static String hourOrHours(long t){
		return t > 1 || t ==0 ? "hours" : "hour";
	}

	public static String minOrMins(long t){
		return t > 1 || t == 0? "minutes" : "minute";
	}
	public static String secOrSecs(long t){
		return t > 1 || t == 0? "sec" : "secs";
	}

	public static String convertLongToDate(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd hh:mm:ss");
		return sdf.format(time);
	}

	public static String PorP(int size) {
		return size == 1 ? "person" : "people";
	}

}
