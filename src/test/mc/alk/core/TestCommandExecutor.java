package test.mc.alk.core;

import junit.framework.TestCase;
import mc.alk.executors.CustomCommandExecutor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class TestCommandExecutor extends TestCase {
	public static class TestExecutor extends CustomCommandExecutor
	{
		@MCCommand(cmds={"test"})
		public void testCmd(CommandSender sender){
			sender.sendMessage("test");
		}

		@MCCommand()
		public void testNoCmd(CommandSender sender){
			sender.sendMessage("done");
		}

		@MCCommand()
		public void testNoCmd2(CommandSender sender, Integer ndays){
			sender.sendMessage("done2");
		}

		@MCCommand()
		public void testNoCmd3(CommandSender sender, Integer ndays, String name){
			sender.sendMessage("done3");
		}
	}

	public void testCommand(){
		TestExecutor te = new TestExecutor();
		TestCommandSender sender = new TestCommandSender();
		Command command = new TestCommand("shop");
		String label = "shop";
		String[] args= new String[]{};

		args= new String[]{label};
		te.onCommand(sender, command, label, args);
		assertEquals("done",sender.getLastMessage());

		args= new String[]{label, "1"};
		te.onCommand(sender, command, label, args);
		assertEquals("done2",sender.getLastMessage());

		args= new String[]{label, "1", "player2"};
		te.onCommand(sender, command, label, args);
		assertEquals("done3",sender.getLastMessage());
	}


}
