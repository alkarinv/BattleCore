package test.mc.alk.core;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class TestCommand extends Command{

	public TestCommand(String name) {
		super(name);
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		System.out.println("executing command  sender="  + sender +"   lbl=" + commandLabel +"   args=" + args);
		return false;
	}

}
