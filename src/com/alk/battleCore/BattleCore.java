package com.alk.battleCore;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.alk.controllers.MoneyController;

public class BattleCore extends JavaPlugin{
	static private String pluginname; 
	static private String version;
	static private BattleCore plugin;
	@Override
	public void onEnable() {
		plugin = this;
		PluginDescriptionFile pdfFile = this.getDescription();
		pluginname = pdfFile.getName();
		version = pdfFile.getVersion();
		MoneyController.setup();

		System.out.println(getVersion()  + " enabled!");		
	}


	@Override
	public void onDisable() {}

	public static String getVersion() {return "[" + pluginname + " v" + version +"]";}

	public static BattleCore getSelf() {return plugin;}
	

}
