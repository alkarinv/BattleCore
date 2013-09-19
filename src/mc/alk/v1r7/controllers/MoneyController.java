package mc.alk.v1r7.controllers;

import mc.alk.v1r7.util.Log;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;


public class MoneyController implements Listener{
	static boolean initialized = false;
	static boolean hasVault = false;
	static boolean useBank = false;

	public static Economy economy = null;
	public static boolean hasEconomy(){
		return initialized;
	}
	public static boolean hasAccount(String name) {
		if (!initialized) return true;
		return economy.hasAccount(name);
	}
	public static boolean hasEnough(String name, double fee) {
		if (!initialized) return true;
		return hasEnough(name, (float) fee);
	}
	public static boolean hasEnough(String name, float amount) {
		if (!initialized) return true;
		return useBank ? economy.bankBalance(name).balance >= amount : economy.getBalance(name) >= amount;
	}

	public static boolean hasEnough(String name, float amount, String world) {
		return hasEnough(name,amount);
	}

	public static void subtract(String name, float amount, String world) {
		subtract(name,amount);
	}

	public static void subtract(String name, double amount) {
		subtract(name,(float) amount);
	}

	public static void subtract(String name, float amount) {
		if (!initialized) return;
		if (useBank)
			economy.bankWithdraw(name, amount);
		else
			economy.withdrawPlayer(name, amount);
	}


	public static void add(String name, float amount, String world) {
		add(name,amount);
	}

	public static void add(String name, double amount) {
		if (!initialized) return;
		add(name,(float)amount);
	}

	public static void add(String name, float amount) {
		if (!initialized) return;
		if (useBank)
			economy.bankDeposit(name, amount);
		else
			economy.depositPlayer(name, amount) ;
	}

	public static Double balance(String name, String world) {
		return balance(name);
	}

	public static Double balance(String name) {
		if (!initialized) return 0.0;
		if (useBank)
			return economy.bankBalance(name).balance;
		else
			return economy.getBalance(name);
	}

	public static void setup(Plugin plugin) {
		Bukkit.getPluginManager().registerEvents(new MoneyController(), plugin);
		checkRegisteredPlugins();
	}

	@EventHandler
    public void setup(PluginEnableEvent event) {
		MoneyController.checkRegisteredPlugins();
	}
	private static void checkRegisteredPlugins(){
		if (initialized) /// We are good to go already
			return;
		Plugin controller;
    	if (MoneyController.economy == null){ /// We want to use vault if we can
    		controller = Bukkit.getServer().getPluginManager().getPlugin("Vault");
    		if (controller != null) {
    			RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().
    					getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
    			if (economyProvider==null || economyProvider.getProvider() == null){
    				MoneyController.economy = null;
    				Log.warn("[] found no economy plugin. Attempts to use money in arenas might result in errors.");
    				return;
    			}
    			MoneyController.economy = economyProvider.getProvider();
    			initialized = true;
    			Log.info("[] found economy plugin Vault. [Default]");
    		}
    	}
	}
	public static void setUseBank(boolean useBank){
		if (!initialized || !economy.hasBankSupport())
			return;
		MoneyController.useBank = useBank;
	}
}
