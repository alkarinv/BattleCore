package com.alk.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

public class Log {

	private static final String COLOR_MC_CHAR = Character.toString((char) 167);
	private static final Logger log = Bukkit.getLogger();

	public static void info(String msg){
		if (log != null)
			log.info(colorChat(msg));
		else 
			System.out.println(colorChat(msg));
	}
	public static void config(String msg){
		if (log != null && log.isLoggable(Level.CONFIG))
			log.config(colorChat(msg));
		else 
			info(msg);
	}
	public static void warn(String msg){
		if (log != null)
			log.warning(colorChat(msg));
		else 
			System.err.println(colorChat(msg));
	}
	public static void err(String msg){
		if (log != null)
			log.severe(colorChat(msg));
		else 
			System.err.println(colorChat(msg));
	}
	
    public static String colorChat(String msg) {
        return msg.replaceAll("&", COLOR_MC_CHAR);
    }
	public static void debug(String string) {
		System.out.println(string);
	}

}
