package mc.alk.v1r7.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class SerializerUtil {
	public static final boolean TESTING = false;

	public static HashMap<String, String> createSaveableLocations(Map<Integer, Location> mlocs) {
		HashMap<String,String> locations = new HashMap<String,String>();
		for (Integer key: mlocs.keySet()){
			String s = SerializerUtil.getLocString(mlocs.get(key));
			locations.put(key+"",s);
		}
		return locations;
	}


	public static void expandMapIntoConfig(ConfigurationSection conf, Map<String, Object> map) {
		for (Entry<String, Object> e : map.entrySet()) {
			if (e.getValue() instanceof Map<?,?>) {
				ConfigurationSection section = conf.createSection(e.getKey());
				@SuppressWarnings("unchecked")
				Map<String,Object> subMap = (Map<String, Object>) e.getValue();
				expandMapIntoConfig(section, subMap);
			} else {
				conf.set(e.getKey(), e.getValue());
			}
		}
	}

	public static Location getLocation(String locstr) throws IllegalArgumentException {
		//		String loc = node.getString(nodestr,null);
		if (locstr == null)
			throw new IllegalArgumentException("Error parsing location. Location string was null");
		StringTokenizer scan = new StringTokenizer(locstr,",");
		String w = scan.nextToken();
		float x = Float.valueOf(scan.nextToken());
		float y = Float.valueOf(scan.nextToken());
		float z = Float.valueOf(scan.nextToken());
		float yaw = 0, pitch = 0;
		if (scan.hasMoreTokens()){yaw = Float.valueOf(scan.nextToken());}
		if (scan.hasMoreTokens()){pitch = Float.valueOf(scan.nextToken());}

		World world = null;
		if (TESTING) return new Location(world,x,y,z,yaw,pitch);
		if (w != null){
			world = Bukkit.getWorld(w);}
		if (world ==null){
			throw new IllegalArgumentException("Error parsing location, World was null");}
		return new Location(world,x,y,z,yaw,pitch);
	}

	public static String getLocString(Location l){
		if (l == null) return null;
		if (TESTING && l.getWorld()==null)
			return "null," + l.getX() + "," + l.getY() + "," + l.getZ()+","+l.getYaw()+","+l.getPitch();
		return l.getWorld().getName()+","+l.getX()+","+l.getY()+","+l.getZ()+","+l.getYaw()+","+l.getPitch();
	}

	public static String getBlockLocString(Location l){
		if (l == null) return null;
		return l.getWorld().getName() +"," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
	}

	public static Map<Integer, Location> parseLocations(ConfigurationSection cs) throws IllegalArgumentException {
		if (cs == null)
			return null;
		HashMap<Integer,Location> locs = new HashMap<Integer,Location>();
		Set<String> indices = cs.getKeys(false);
		for (String locIndexStr : indices){
			Location loc = null;
			loc = SerializerUtil.getLocation(cs.getString(locIndexStr));
			Integer i = Integer.valueOf(locIndexStr);
			locs.put(i, loc);
		}
		return locs;
	}

}
