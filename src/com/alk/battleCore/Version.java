package com.alk.battleCore;

import org.apache.commons.lang.StringUtils;

public class Version implements Comparable<Object> {
	String[] parts;
	public Version(String strv){
		parts = strv.split("\\.");
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof Version)
			return compareToVersion((Version)o);
		return compareToVersion(new Version(o.toString()));
	}
	
	public String getVersion(){
		return this.toString();
	}
	
	public String toString(){
		return StringUtils.join(parts, '.');
	}
	
	private int compareToVersion(Version v) {
		int max = Math.max(parts.length, v.parts.length);
		String p1[],p2[];
		if (max > parts.length){
			p1 = new String[max];
			System.arraycopy(parts, 0, p1, 0, parts.length);
			for (int i=parts.length;i<max;i++){
				p1[i] = "0";}
		} else {
			p1 = parts;
		}
		if (max > v.parts.length){
			p2 = new String[max];
			System.arraycopy(v.parts, 0, p2, 0, v.parts.length);
			for (int i=v.parts.length;i<max;i++){
				p2[i] = "0";}
		} else {
			p2 = v.parts;
		}
		for (int i=0;i<max;i++){
			int c = p1[i].compareTo(p2[i]);
			if (c == 0) continue;
			return c;
		}
		return 0;
	}
}
