package test.mc.alk.core;

import junit.framework.TestCase;
import mc.alk.v1r7.executors.CustomCommandExecutor;

import org.bukkit.OfflinePlayer;
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
		public void testNoCmd2(CommandSender sender, String name){
			sender.sendMessage("testNoCmd2");
		}

		@MCCommand()
		public void testNoCmd2(CommandSender sender, Integer ndays){
			sender.sendMessage("done2");
		}

		@MCCommand()
		public void testNoCmd3(CommandSender sender, Integer ndays, String name){
			sender.sendMessage("done3");
		}
		@MCCommand()
		public void testNoCmd3(CommandSender sender, String name, Integer ndays){
			sender.sendMessage("testNoCmd3");
		}

		@MCCommand(cmds={"setRating"},op=true)
		public void setRating(CommandSender sender, String db, OfflinePlayer player, Integer rating){
			sender.sendMessage("setRating");
		}
	}

	public void testCommand(){
		TestExecutor te = new TestExecutor();
		TestCommandSender sender = new TestCommandSender();
		Command command = new TestCommand("shop");
		String label = "shop";
		String[] args= new String[]{};

		args= new String[]{};
		te.onCommand(sender, command, label, args);
		assertEquals("done",sender.getLastMessage());

		args= new String[]{"1"};
		te.onCommand(sender, command, label, args);
		assertEquals("done2",sender.getLastMessage());

		args= new String[]{"1", "player2"};
		te.onCommand(sender, command, label, args);
		assertEquals("done3",sender.getLastMessage());

		args= new String[]{"player2", "1"};
		te.onCommand(sender, command, label, args);
		assertEquals("testNoCmd3",sender.getLastMessage());

		args= new String[]{"player2", "1"};
//		command = new TestCommand("")
		te.onCommand(sender, command, label, args);
		assertEquals("testNoCmd3",sender.getLastMessage());

	}


}
