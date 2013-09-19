package mc.alk.v1r7.core;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class MCListener extends MCPlugin implements Listener{

	@Override
	public void onEnable() {
		super.onEnable();
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
	}
}
