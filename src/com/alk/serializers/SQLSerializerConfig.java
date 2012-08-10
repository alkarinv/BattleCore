package com.alk.serializers;

import org.bukkit.configuration.ConfigurationSection;

import com.alk.util.Log;

public class SQLSerializerConfig {

	public static void configureSQL(SQLSerializer sql, ConfigurationSection cs) {
		try{
			if (cs.contains("db")) sql.setDB(cs.getString("db"));
			sql.setURL(cs.getString("url"));
			sql.setPort(cs.getString("port"));
			sql.setUsername(cs.getString("username"));
			sql.setPassword(cs.getString("password"));
			sql.init();
		} catch (Exception e){
			Log.err("Error configuring sql");
		}
	}

}
