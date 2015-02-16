package mc.alk.v1r7.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class Log {
    private static final boolean DEBUG_MSGS = false;
    private static Logger log;
    public static void setLogger(Logger log) {
        Log.log = log;
    }

    public static void info(String msg){
        if (msg == null) return;
        try{
            sendMessage(Bukkit.getConsoleSender(), msg);
        } catch (Exception e){
            if (log != null)
                log.info(colorChat(msg));
            else
                System.out.println(colorChat(msg));
        }
    }

    public static void warn(String msg){
        if (msg == null) return;
        try{
            sendMessage(Bukkit.getConsoleSender(), msg);
        } catch (Exception e){
            if (log != null)
                log.warning(colorChat(msg));
            else
                System.out.println(colorChat(msg));
        }
    }

    public static void err(String msg){
        if (msg == null) return;
        try{
            sendMessage(Bukkit.getConsoleSender(), msg);
        } catch (Exception e){
            if (log != null)
                log.severe(colorChat(msg));
            else
                System.err.println(colorChat(msg));
        }
    }

    public static String colorChat(String msg) {
        return msg.replace('&', (char) 167);
    }

    public static void debug(String msg){
        if (DEBUG_MSGS){
            try{
                sendMessage(Bukkit.getConsoleSender(), msg);
            } catch (Exception e){
                System.out.println(msg);
            }
        }
    }

    public static void printStackTrace(Throwable e) {
        e.printStackTrace();
    }

    public static boolean sendMessage(CommandSender p, String message){
        if (message ==null) return true;
        if (message.contains("\n"))
            return sendMultilineMessage(p,message);
        if (p instanceof Player){
            if (((Player) p).isOnline())
                p.sendMessage(colorChat(message));
        } else {
            p.sendMessage(colorChat(message));
        }
        return true;
    }

    public static boolean sendMultilineMessage(CommandSender p, String message){
        if (message ==null || message.isEmpty()) return true;
        String[] msgs = message.split("\n");
        for (String msg: msgs){
            if (p instanceof Player){
                if (((Player) p).isOnline())
                    p.sendMessage(colorChat(msg));
            } else {
                p.sendMessage(colorChat(msg));
            }
        }
        return true;
    }
}
