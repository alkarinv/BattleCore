package com.alk.executors;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public abstract class CustomCommandExecutor implements CommandExecutor{
	static final String version = "1.1";
	static final boolean DEBUG = false;
	static final String DEFAULT_CMD = "_dcmd_";
	
	/// The map of our methods
	private HashMap<String,TreeMap<Integer,MethodWrapper>> methods = new HashMap<String,TreeMap<Integer,MethodWrapper>>();
	protected HashMap<MCCommand, String> usage = new HashMap<MCCommand, String>();

	protected FileConfiguration config =null;

	/**
	 * Custom arguments class so that we can return a modified arguments
	 */
	public static class Arguments{
		Object[] args;
	}

	protected static class MethodWrapper{
		public MethodWrapper(Object obj, Method method){
			this.obj = obj; this.method = method;
		}
		Object obj; /// Object the method belongs to
		Method method; /// Method
	}

	protected CustomCommandExecutor(){
		addMethods(this, getClass().getMethods());
	}

	protected CustomCommandExecutor(FileConfiguration fileConfiguration){
		addMethods(this, getClass().getMethods());
		this.config = fileConfiguration;
	}

	protected boolean hasMethod(String method){
		return methods.containsKey(method);
	}

	/**
	 * When no arguments are supplied, no method is found
	 * What to display when this happens
	 * @param sender
	 */
	protected void showHelp(CommandSender sender, Command command){
		help(sender,command,null);
	}

	protected void addMethods(Object obj, Method[] methodArray){
		for (Method method : methodArray){
			MCCommand mc = method.getAnnotation(MCCommand.class);
			if (mc == null)
				continue;

			if (mc.cmds().length == 0){ /// There is no subcommand. just the command itself with arguments
				addMethod(obj, method, mc, DEFAULT_CMD);				
			}
			/// For each of the cmds, store them with the method
			for (String cmd : mc.cmds()){
				cmd = cmd.toLowerCase();
				addMethod(obj, method, mc, cmd);
			}
			/// save the usages, for showing help messages
			if (!mc.usageNode().isEmpty()){
				usage.put(mc, config.getString(mc.usageNode()));
			} else if (!mc.usage().isEmpty()){
				usage.put(mc, mc.usage());
			} else { /// Generate a automatic usage string
				usage.put(mc, createUsage(method));
			}
		}
	}

	private void addMethod(Object obj, Method method, MCCommand mc, String cmd) {
		TreeMap<Integer,MethodWrapper> mthds = methods.get(cmd);
		if (mthds == null){
			mthds = new TreeMap<Integer,MethodWrapper>();
		}
		int order = mc.order() != -1? mc.order() : Integer.MAX_VALUE-mthds.size();
		mthds.put(order, new MethodWrapper(obj,method));
		methods.put(cmd, mthds);
	}

	private String createUsage(Method method) {
		MCCommand cmd = method.getAnnotation(MCCommand.class);
		
		StringBuilder sb = new StringBuilder();
		if (cmd.cmds().length > 0 )
			sb.append(cmd.cmds()[0] +" ");
		boolean firstPlayerSender = cmd.inGame();
		for (Class<?> theclass : method.getParameterTypes()){			
			if (Player.class ==theclass){
				if (firstPlayerSender)
					firstPlayerSender = false;
				else 
					sb.append("<player> ");
			} else if (OfflinePlayer.class ==theclass){
				sb.append("<player> ");
			} else if (String.class == theclass){
				sb.append("<string> ");
			} else if (Integer.class == theclass){
				sb.append("<int> ");
			} else if (Object[].class == theclass){
				sb.append("[string ... ]");
			} else if (Boolean.class == theclass){
				sb.append("<true|false> ");
			} else if (Object.class == theclass){
				sb.append("<string> ");
			}
		}

		return sb.toString();
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		/// No method to handle, show some help
		TreeMap<Integer,MethodWrapper> methodmap = methods.get(DEFAULT_CMD);
		System.out.println(command.getName() +"   " + label + "      cmdlabel = " + command.getLabel());
		if (args.length == 0 && (methodmap == null || methodmap.isEmpty())
				|| (args.length > 0 && args[0].equals("?"))){
			showHelp(sender, command);
			return true;
		}
		
		/// Find our method, and verify all the annotations
		if (args.length > 0){
			methodmap = methods.get(args[0].toLowerCase());
		}
		if (methodmap == null || methodmap.isEmpty()){
			/// Maybe its a default command
			methodmap = methods.get(DEFAULT_CMD);
			if (methodmap == null || methodmap.isEmpty()){ /// nope nothing
				return sendMessage(sender, "&cThat command does not exist!&6 /"+command.getLabel()+" &c for help");
			}
		}

		MCCommand mccmd = null;
		List<InvalidArgumentException> errs =null;
		boolean success = false;
		for (MethodWrapper mwrapper : methodmap.values()){
			mccmd = mwrapper.method.getAnnotation(MCCommand.class);
			final boolean isOp = sender == null || sender.isOp() || sender instanceof ConsoleCommandSender;

			// Check perms
			if ( (mccmd.op() && !isOp) || (!mccmd.perm().isEmpty() && !sender.hasPermission(mccmd.perm()) )) 
				continue;
			try {
				Arguments newArgs= verifyArgs(mwrapper,mccmd,sender,command, label, args);
				mwrapper.method.invoke(mwrapper.obj,newArgs.args);
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
				sendMessage(sender, errs.get(0).getMessage());
				sendMessage(sender, getUsage(command, mccmd));
				return true;
			}
			HashSet<String> errstrings = new HashSet<String>();
			for (InvalidArgumentException e: errs){
				errstrings.add(e.getMessage());}
			for (String msg : errstrings){
				sendMessage(sender, msg);}
			sendMessage(sender, getUsage(command, mccmd));
		}
		return true;
	}

	static final String ONLY_INGAME =ChatColor.RED+"You need to be in game to use this command";
	private Arguments verifyArgs(MethodWrapper mwrapper, MCCommand cmd, 
			CommandSender sender, Command command, String label, String[] args) throws InvalidArgumentException{
		if (DEBUG)System.out.println("verifyArgs " + cmd +" sender=" +sender+", label=" + label+" args="+args);
		int strIndex = cmd.cmds().length == 0 ? 0 : 1; /// Skip the label if we have a cmd 
		int objIndex = 0;

		/// Check our permissions
		if (!cmd.perm().isEmpty() && !sender.hasPermission(cmd.perm()))
			throw new InvalidArgumentException("&cYou don't have permission for this command");

		/// Verify min number of arguments
		if (args.length < cmd.min()){
			throw new InvalidArgumentException(ChatColor.RED+"You need at least "+cmd.min()+" arguments");
		}
		/// Verfiy max number of arguments
		if (args.length > cmd.max()){
			throw new InvalidArgumentException(ChatColor.RED+"You need less than "+cmd.max()+" arguments");
		}
		/// Verfiy max number of arguments
		if (cmd.exact()!= -1 && args.length != cmd.exact()){
			throw new InvalidArgumentException(ChatColor.RED+"You need exactly "+cmd.exact()+" arguments");
		}
		final boolean isPlayer = sender instanceof Player;
		final boolean isOp = (isPlayer && sender.isOp()) || sender == null || sender instanceof ConsoleCommandSender;

		if (cmd.op() && !isOp)
			throw new InvalidArgumentException(ChatColor.RED +"You need to be op to use this command");

		/// the first ArenaPlayer or Player parameter is the sender
		boolean getSenderAsPlayer = cmd.inGame();

		/// In game check
		if (cmd.inGame() && !isPlayer || getSenderAsPlayer && !isPlayer){
			throw new InvalidArgumentException(ONLY_INGAME);			
		}

		Arguments newArgs = new Arguments(); /// Our return value
		Object[] objs = new Object[mwrapper.method.getParameterTypes().length]; /// Our new array of castable arguments
		newArgs.args = objs; /// Set our return object with the new castable arguments
		for (Class<?> theclass : mwrapper.method.getParameterTypes()){
			try{
				if (CommandSender.class == theclass){
					objs[objIndex] = sender;
				} else if (Command.class == theclass){
					objs[objIndex] = command;				
				} else if (Player.class ==theclass){
					if (getSenderAsPlayer){
						objs[objIndex] = sender;
						getSenderAsPlayer = false;
					} else {
						objs[objIndex] = verifyPlayer(args[strIndex++]);
					}
				} else if (OfflinePlayer.class ==theclass){
					objs[objIndex] = verifyOfflinePlayer(args[strIndex++]);
				} else if (String.class == theclass){
					objs[objIndex] = args[strIndex++]; 
				} else if (Integer.class == theclass){
					objs[objIndex] = verifyInteger(args[strIndex++]);
				} else if (String[].class == theclass){
					objs[objIndex] = args; 
				} else if (Object[].class == theclass){
					objs[objIndex] = args;
				} else if (Boolean.class == theclass){
					objs[objIndex] = Boolean.parseBoolean(args[strIndex++]);
				} else if (Object.class == theclass){
					objs[objIndex] = args[strIndex++];
				}
			} catch (ArrayIndexOutOfBoundsException e){
				throw new InvalidArgumentException("You didnt supply enough arguments for this method");
			}
			objIndex++;
		}

		/// Verify alphanumeric
		if (cmd.alphanum().length > 0){
			for (int index: cmd.alphanum()){
				if (index >= args.length)
					throw new InvalidArgumentException("String Index out of range. ");
				if (!args[index].matches("[a-zA-Z0-9_]*")) {
					throw new InvalidArgumentException("&eargument '"+args[index]+"' can only be alphanumeric with underscores");
				}
			}
		}

		/// Check to see if the players are online
		if (cmd.online().length > 0){
			if (DEBUG)System.out.println("isPlayer " + cmd.online());

			for (int playerIndex : cmd.online()){
				if (playerIndex >= args.length)
					throw new InvalidArgumentException("PlayerIndex out of range. ");
				Player p = findPlayer(args[playerIndex]);
				if (p == null || !p.isOnline())
					throw new InvalidArgumentException(args[playerIndex]+" must be online ");
				/// Change over our string to a player
				objs[playerIndex] = p;
			}
		}
		return newArgs; /// Success
	}


	private OfflinePlayer verifyOfflinePlayer(String name) throws InvalidArgumentException {

		OfflinePlayer p = findOfflinePlayer(name);
		if (p == null)
			throw new InvalidArgumentException("Player " + name+" can not be found");
		return p;
	}

	private Player verifyPlayer(String name) throws InvalidArgumentException {
		Player p = findPlayer(name);
		if (p == null || !p.isOnline())
			throw new InvalidArgumentException(name+" is not online ");
		return p;
	}

	private Integer verifyInteger(Object object) throws InvalidArgumentException {
		try {
			return Integer.parseInt(object.toString());
		}catch (NumberFormatException e){
			throw new InvalidArgumentException(ChatColor.RED+(String)object+" is not a valid integer.");
		}
	}

	private String getUsage(Command c, MCCommand cmd) {
		if (!cmd.usage().isEmpty()) /// Get from usage
			return "&6"+c.getName()+" " + cmd.usage();
		if (config!=null && config.contains(cmd.usageNode())) /// Maybe a default message node??
			return config.getString(cmd.usageNode());
		return "&6/"+c.getName()+" " + usage.get(cmd); /// Return the usage from our map
	}

	public static class InvalidArgumentException extends Exception {
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
				sendMessage(sender, ChatColor.RED+" " + args[1] +" is not a number, showing help for page 1.");
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
			sendMessage(sender, "&4That page doesnt exist, try 1-"+npages);
			return;
		}
		if (command != null)
			sendMessage(sender, "&eShowing page &6"+page +"/"+npages +"&6 :[Usage] /"+command.getName()+" help <page number>");
		else 
			sendMessage(sender, "&eShowing page &6"+page +"/"+npages +"&6 :[Usage] /cmd help <page number>");
		int i=0;
		for (String use : available){
			i++;
			if (i < (page-1) *LINES_PER_PAGE || i >= page*LINES_PER_PAGE)
				continue;
			sendMessage(sender, use);
		}
		for (String use : unavailable){
			i++;
			if (i < (page-1) *LINES_PER_PAGE || i >= page *LINES_PER_PAGE)
				continue;
			sendMessage(sender, ChatColor.RED+"[Insufficient Perms] " + use);
		}
		if (sender.isOp()){
			for (String use : onlyop){
				i++;
				if (i < (page-1) *LINES_PER_PAGE || i >= page *LINES_PER_PAGE)
					continue;
				sendMessage(sender, ChatColor.AQUA+"[OP only] &6"+use);
			}			
		}
	}

	public static boolean sendMessage(CommandSender p, String message){
		if (message ==null) return true;
		if (p instanceof Player){
			if (((Player) p).isOnline())
				p.sendMessage(colorChat(message));			
		} else {
			p.sendMessage(colorChat(message));
		}
		return true;
	}
	public static String colorChat(String msg) {
		return msg.replaceAll("&", Character.toString((char) 167));
	}

	public static Player findPlayer(String name) throws InvalidArgumentException{
		List<Player> ps = Bukkit.matchPlayer(name);
		if (ps == null || ps.isEmpty())
			throw new InvalidArgumentException("no players matched " + name);
		if (ps.size() > 1)
			throw new InvalidArgumentException("multiple players match "+name);
		Player p = ps.iterator().next();
		return p;
	}
	
	public static OfflinePlayer findOfflinePlayer(String name) {
		OfflinePlayer p;
		try {
			p = findPlayer(name);
		} catch (InvalidArgumentException e) {
			return null;
		}
		if (p != null){
			return p;
		} else{
			/// Iterate over the worlds to see if a player.dat file exists
			for (World w : Bukkit.getWorlds()){
				File f = new File(w.getName()+"/players/"+name+".dat");
				if (f.exists()){
					return Bukkit.getOfflinePlayer(name);
				}
			}
			return null;
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	public static @interface MCCommand {
		/// This is required, the cmd and all its aliases
	    String[] cmds() default {};

	    /// Verify the number of parameters, inGuild and notInGuild imply min if they have an index > number of args
	    int min() default 0;
	    int max() default Integer.MAX_VALUE;
	    int exact() default -1;
	    
	    int order() default -1;

	    boolean op() default false;
	    boolean admin() default false;
	    String perm() default "";
	    
	    boolean inGame() default false;
	    int[] online() default {}; /// Implies inGame = true
	    int[] ints() default {};
	    
	    int[] ports() default {};
	    int[] playerQuery() default {};
	    
	    String usage() default "";
	    String usageNode() default "";

		int[] alphanum() default {};   
	}
}



