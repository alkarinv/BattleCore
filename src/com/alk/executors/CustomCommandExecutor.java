package com.alk.executors;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.alk.controllers.MC;


public abstract class CustomCommandExecutor implements CommandExecutor{
	static final boolean DEBUG = false;
	private HashMap<String,TreeMap<Integer,MethodWrapper>> methods = new HashMap<String,TreeMap<Integer,MethodWrapper>>();
	public static final int SELF = -2; /// Which index defines the sender
	protected HashMap<MCCommand, String> usage = new HashMap<MCCommand, String>();

	protected static class MethodWrapper{
		public MethodWrapper(Object obj, Method method){this.obj = obj; this.method = method;}
		Object obj; /// Object the method belongs to
		Method method; /// Method
	}
	
	/**
	 * When no arguments are supplied, no method is found
	 * What to display when this happens
	 * @param sender
	 */
	protected abstract void showHelp(CommandSender sender, Command command, String label);

	protected boolean hasMethod(String method){
		return methods.containsKey(method);
	}
	protected CustomCommandExecutor(){
		addMethods(this, getClass().getMethods());
	}

	protected void addMethods(Object obj, Method[] methodArray){
		for (Method method : methodArray){
			MCCommand mc = method.getAnnotation(MCCommand.class);
			if (mc == null)
				continue;
//			System.out.println("adding method " + method);
			/// For each of the cmds, store them with the method
			for (String cmd : mc.cmds()){
				cmd = cmd.toLowerCase();
				TreeMap<Integer,MethodWrapper> mthds = methods.get(cmd);
				if (mthds == null){
					mthds = new TreeMap<Integer,MethodWrapper>();
				}
				int order = mc.order() != -1? mc.order() : Integer.MAX_VALUE-mthds.size();
				mthds.put(order, new MethodWrapper(obj,method));
				methods.put(cmd, mthds);
			}
			
			if (!mc.usage().isEmpty()){
				usage.put(mc, mc.usage());
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		/// No method to handle, show some help
		if (args.length == 0){
			showHelp(sender, command,label);
			return true;
		}
		/// Find our method, and verify all the annotations
		TreeMap<Integer,MethodWrapper> methodmap = methods.get(args[0].toLowerCase());
		if (methodmap == null || methodmap.isEmpty()){
			return MC.sendMessage(sender, "That command does not exist!");
		}

		MCCommand mccmd = null;
		List<InvalidArgumentException> errs =null;
		boolean success = false;
		for (MethodWrapper mwrapper : methodmap.values()){
			mccmd = mwrapper.method.getAnnotation(MCCommand.class);
			final boolean isOp = sender == null || sender.isOp() || sender instanceof ConsoleCommandSender;

			if (mccmd.op() && !isOp ) /// no op, no pass
				continue;
			try {
				Arguments newArgs= verifyArgs(mccmd,sender,command, label, args);
				/// Invoke our method
				mwrapper.method.invoke(mwrapper.obj,sender,command,label, newArgs.args);					
				success = true;
				break; /// success on one
			} catch (InvalidArgumentException e){
				if (errs == null)
					errs = new ArrayList<InvalidArgumentException>();
				errs.add(e);
			} catch (Exception e) { /// Just all around bad
				e.printStackTrace();
			}
		}
		/// and handle all errors
		if (!success && errs != null && !errs.isEmpty()){
			if (errs.size() == 1){
				MC.sendMessage(sender, errs.get(0).getMessage());
				MC.sendMessage(sender, getUsage(command, mccmd));
				return true;
			}
			HashSet<String> errstrings = new HashSet<String>();
			for (InvalidArgumentException e: errs){
				errstrings.add(e.getMessage());}
			for (String msg : errstrings){
				MC.sendMessage(sender, msg);}
			MC.sendMessage(sender, getUsage(command, mccmd));
		}
		return true;
	}


	private String getUsage(Command c, MCCommand cmd) {
		if (!cmd.usage().isEmpty())
			return "&6"+c.getName()+":&e" + cmd.usage();
		/// By Default try to return the message under this commands name in "usage.cmd"
		return "&6No options specified";
	}

	private Arguments verifyArgs(MCCommand cmd, CommandSender sender, Command command, String label, String[] args) 
			throws InvalidArgumentException{
		if (DEBUG)System.out.println("verifyArgs " + cmd +" sender=" +sender+", label=" + label+" args="+args);
		Arguments newArgs = new Arguments(); /// Our return value
		Object[] objs = new Object[args.length]; /// Our new array of castable arguments
		System.arraycopy( args, 0, objs, 0, args.length );
		newArgs.args = objs; /// Set our return object with the new castable arguments

		/// Verify min number of arguments
		if (args.length < cmd.min()){
			throw new InvalidArgumentException(ChatColor.RED+"You need at least "+cmd.min()+" arguments");
		}
		/// Verfiy max number of arguments
		if (args.length > cmd.max()){
			throw new InvalidArgumentException(ChatColor.RED+"You need less than "+cmd.max()+" arguments");
		}

		/// First convert our sender to either a GuildPlayer or an op (null)
		Player player = null;
		if (sender instanceof Player){
			player = (Player) sender;
		} else {
			player = null;
		}
		if (cmd.op() && (player != null || !sender.isOp()))
			throw new InvalidArgumentException(ChatColor.RED +"You need to be an Admin to use this command");

		newArgs.sender = player; /// Put in our player to our return arguments

		/// In game check
		if (cmd.inGame() && player == null){
			throw new InvalidArgumentException(ChatColor.RED + "You can only use this command in game");			
		}

		/// Check to see if the players are online
		if (cmd.online().length > 0){
			if (DEBUG)System.out.println("isPlayer " + cmd.online());

			for (int playerIndex : cmd.online()){
				if (playerIndex == SELF){
					if (player == null)
						throw new InvalidArgumentException(ChatColor.RED + "You can only use this command in game");			
				} else {
					if (playerIndex >= args.length)
						throw new InvalidArgumentException("PlayerIndex out of range. " +getUsage(cmd));
					Player p = Bukkit.getPlayer(args[playerIndex]);
					if (p == null)
						throw new InvalidArgumentException(args[playerIndex]+" must be online ");			
					/// Change over our string to a guild player
					objs[playerIndex] = p;
				}
			}
		}

		/// Verify ints
		if (cmd.ints().length > 0){
			for (int index: cmd.ints()){
				if (index >= args.length)
					throw new InvalidArgumentException("IntegerIndex out of range. " + getUsage(cmd));
				try {
					objs[index] = Integer.parseInt(args[index]);
				}catch (NumberFormatException e){
					throw new InvalidArgumentException(ChatColor.RED+(String)args[1]+" is not a valid integer.");
				}
			}
		}
		/// Verify alphanumeric
		if (cmd.alphanum().length > 0){
			for (int index: cmd.alphanum()){
				if (index >= args.length)
					throw new InvalidArgumentException("String Index out of range. " + getUsage(cmd));
				if (!args[index].matches("[a-zA-Z0-9_]*")) {
					throw new InvalidArgumentException("&earguments can be only alphanumeric with underscores");
				}
			}
		}

		if (!cmd.perm().isEmpty() && !sender.hasPermission(cmd.perm()))
			throw new InvalidArgumentException(ChatColor.RED + "You dont have permission for this command");

		return newArgs; /// Success
	}

	private String getUsage(MCCommand cmd) {
		if (!cmd.usage().isEmpty())
			return cmd.usage();
		/// By Default try to return the message under this commands name in "usage.cmd"
		return "";
	}
	private class Arguments{
		Player sender;
		Object[] args;
	}
	public class InvalidArgumentException extends Exception {
		private static final long serialVersionUID = 1L;

		public InvalidArgumentException(String string) {
			super(string);
		}
	}

	static final int LINES_PER_PAGE = 8;
	public void help(CommandSender sender, Command command, Object[] args){
		Integer page = 1;

		if (args != null && args.length > 1){
			try{
				page = Integer.valueOf((String) args[1]);
			} catch (Exception e){
				MC.sendMessage(sender, ChatColor.RED+" " + args[1] +" is not a number, showing help for page 1.");
			}
		}

		Set<String> available = new HashSet<String>();
		Set<String> unavailable = new HashSet<String>();
		Set<String> onlyop = new HashSet<String>();

		for (MCCommand cmd : usage.keySet()){
			final String use = "&6/" + command.getName() +" " + usage.get(cmd);
			if (cmd.op() && !sender.isOp())
				onlyop.add(use);
			else if (!cmd.perm().isEmpty() && !sender.hasPermission(cmd.perm()))
				unavailable.add(use);
			else 
				available.add(use);
		}
		int npages = available.size()+unavailable.size();
		if (sender.isOp())
			npages += onlyop.size();
		npages = (int) Math.ceil( (float)npages/LINES_PER_PAGE);
		if (page > npages || page <= 0){
			MC.sendMessage(sender, "&4That page doesnt exist, try 1-"+npages);
			return;
		}
		if (command != null)
			MC.sendMessage(sender, "&eShowing page &6"+page +"/"+npages +"&6 :[Usage] /"+command.getName()+" help <page number>");
		else 
			MC.sendMessage(sender, "&eShowing page &6"+page +"/"+npages +"&6 :[Usage] /cmd help <page number>");
		int i=0;
		for (String use : available){
			i++;
			if (i < (page-1) *LINES_PER_PAGE || i >= page*LINES_PER_PAGE)
				continue;
			MC.sendMessage(sender, use);
		}
		for (String use : unavailable){
			i++;
			if (i < (page-1) *LINES_PER_PAGE || i >= page *LINES_PER_PAGE)
				continue;
			MC.sendMessage(sender, ChatColor.RED+"[Insufficient Perms] " + use);
		}
		if (sender.isOp()){
			for (String use : onlyop){
				i++;
				if (i < (page-1) *LINES_PER_PAGE || i >= page *LINES_PER_PAGE)
					continue;
				MC.sendMessage(sender, ChatColor.AQUA+"[OP only] &6"+use);
			}			
		}
	}

}
