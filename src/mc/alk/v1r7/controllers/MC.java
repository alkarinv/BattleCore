package mc.alk.v1r7.controllers;

import java.io.File;
import java.util.Formatter;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 * MessageController
 * @author alkarin
 *
 */
public class MC {

	private YamlConfiguration mc = new YamlConfiguration();
	File f;

	public String getMessage(String prefix,String node, Object... varArgs) {
		try{
			ConfigurationSection n = mc.getConfigurationSection(prefix);

			StringBuilder buf = new StringBuilder(n.getString("prefix", "[Arena]"));
			String msg = n.getString(node, "No translation for " + node);
			Formatter form = new Formatter(buf);

			form.format(msg, varArgs);
			form.close();
			return colorChat(buf.toString());
		} catch(Exception e){
			System.err.println("Error getting message " + prefix + "." + node);
			for (Object o: varArgs){ System.err.println("argument=" + o);}
			e.printStackTrace();
			return "Error getting message " + prefix + "." + node;
		}
	}
	public String getMessageNP(String prefix,String node, Object... varArgs) {
		ConfigurationSection n = mc.getConfigurationSection(prefix);
		StringBuilder buf = new StringBuilder();
		String msg = n.getString(node, "No translation for " + node);
		Formatter form = new Formatter(buf);
		try{
			form.format(msg, varArgs);
			form.close();
		} catch(Exception e){
			System.err.println("Error getting message " + prefix + "." + node);
			for (Object o: varArgs){ System.err.println("argument=" + o);}
			e.printStackTrace();
		}
		return colorChat(buf.toString());
	}

	public String getMessageAddPrefix(String pprefix, String prefix,String node, Object... varArgs) {
		ConfigurationSection n = mc.getConfigurationSection(prefix);
		StringBuilder buf = new StringBuilder(pprefix);
		String msg = n.getString(node, "No translation for " + node);
		Formatter form = new Formatter(buf);
		try{
			form.format(msg, varArgs);
			form.close();
		} catch(Exception e){
			System.err.println("Error getting message " + prefix + "." + node);
			for (Object o: varArgs){ System.err.println("argument=" + o);}
			e.printStackTrace();
		}
		return colorChat(buf.toString());
	}

	public static String colorChat(String msg) {
		return msg.replaceAll("&", Character.toString((char) 167));
	}

	public boolean setConfig(File f){
		this.f = f;
		return load();
	}

	public static boolean sendMessage(Player p, String message){
		if (message ==null) return true;
		String[] msgs = message.split("\n");
		for (String msg: msgs){
			if (p == null){
				System.out.println(MC.colorChat(msg));
			} else {
				p.getPlayer().sendMessage(MC.colorChat(msg));			
			}			
		}
		return true;
	}

	public static boolean sendMessage(OfflinePlayer p, String message){
		if (message ==null) return true;
		String[] msgs = message.split("\n");
		for (String msg: msgs){
			if (p == null){
				System.out.println(MC.colorChat(msg));
			} else {
				p.getPlayer().sendMessage(MC.colorChat(msg));			
			}			
		}
		return true;
	}
	public static boolean sendMessage(CommandSender p, String message){
		if (message ==null) return true;
		if (p instanceof Player){
			if (((Player) p).isOnline())
				p.sendMessage(MC.colorChat(message));			
		} else {
			p.sendMessage(MC.colorChat(message));
		}
		return true;
	}

	public boolean load() {
		try {
			mc.load(f);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public static String minuteOrMinutes(int minutes) {
		return minutes == 1 ? "minute" : "minutes";
	}
	public static String getTeamsOrPlayers(int teamSize) {
		return teamSize==1 ? "players" : "teams";
	}
	public boolean sendMsg(CommandSender sender, String message) {
		return MC.sendMessage(sender, message);
	}
}
