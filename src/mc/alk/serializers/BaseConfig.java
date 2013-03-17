package mc.alk.serializers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class BaseConfig {
	protected FileConfiguration config;
	protected File file = null;

	public int getInt(String node,int defaultValue) {return config.getInt(node, defaultValue);}

	public boolean getBoolean(String node, boolean defaultValue) {return config.getBoolean(node, false);}

	public double getDouble(String node, double defaultValue) {return config.getDouble(node, defaultValue);}

	public String getString(String node,String defaultValue) {return config.getString(node,defaultValue);}

	public ConfigurationSection getConfigurationSection(String node) {return config.getConfigurationSection(node);}

	public BaseConfig(){}

	public BaseConfig(File file){
		setConfig(file);
	}

	public FileConfiguration getConfig() {
		return config;
	}

	public File getFile() {
		return file;
	}

	public boolean setConfig(String file){
		return setConfig(new File(file));
	}
	public boolean setConfig(File file){
		this.file = file;
		if (!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.err.println("Couldn't create the config file=" + file);
				e.printStackTrace();
				return false;
			}
		}

		config = new YamlConfiguration();
		try {
			config.load(file);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void reloadFile(){
		try {
			config.load(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void save() {
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<String> getStringList(String node) {
		return config.getStringList(node);
	}

	public void load(File file) {
		this.file = file;
		reloadFile();
	}

}
