package com.onniserver.idlookup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	public final Logger logger = Logger.getLogger("Minecraft");
	public static Main plugin;
	
	private FileConfiguration items = null;
	private File itemsFile = null;
	
	public static boolean update = false;
	public static String name = "";
	public static String type = "";
	public static String version = "";
	public static String link = "";

	public void reloadItems() {
		if (itemsFile == null) {
			itemsFile = new File(this.getDataFolder(), "items.yml");
		}
		items = YamlConfiguration.loadConfiguration(itemsFile);

		InputStream defConfigStream = this.getResource("items.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			items.setDefaults(defConfig);
			items.options().copyDefaults(true);
		}
	}

	public FileConfiguration getItems() {
		if (items == null) {
			reloadItems();
		}
		return items;
	}

	public void saveItems() {
		if (items == null || itemsFile == null) {
			return;
		}
		try {
			getItems().save(itemsFile);
		}catch (IOException ex) {
			this.getLogger().log(Level.SEVERE, "Could not save config to " + itemsFile, ex);
		}
	}
	
	public void saveDefaultItems() {
		if (!itemsFile.exists()) {
			this.saveResource("items.yml", true);
		}
	}
	
	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName()+" V"+pdfFile.getVersion()+" has been disabled");
	}
	
	@Override
	public void onEnable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName()+" v"+pdfFile.getVersion()+" has been enabled");
		this.getConfig();
		this.saveDefaultConfig();
		this.getItems();
		this.saveDefaultItems();
		
		String autoUpdate = this.getConfig().getString("autoUpdate");
		if(autoUpdate.equalsIgnoreCase("true")) {
			new Updater(this, 70522, this.getFile(), Updater.UpdateType.DEFAULT, true);
		}
		String getDefaultItemList = this.getConfig().getString("getDefaultItemList");
		if(getDefaultItemList.equalsIgnoreCase("true")) {
			new File(getDataFolder(), "items.yml").delete();
			this.getItems();
			this.saveDefaultItems();
			this.logger.info("[IDLookup] getDefaultItemList is set to true. The itemlist was updated.");
		}else if(getDefaultItemList.equalsIgnoreCase("false")) {
			this.logger.info("[IDLookup] getDefaultItemList is set to false. The itemlist will not be updated.");
		}else {
			this.logger.severe("[IDLookup] getDefaultItemList is neither set to false or true. This will be set to true.");
			this.getConfig().set("getDefaultItemList", "true");
			this.saveConfig();
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if(commandLabel.equalsIgnoreCase("idlookup") && player.hasPermission("idlookup.menu")) {
				if(args.length == 0) {
					player.sendMessage(ChatColor.RED+"You typed too few arguments. Try "+ChatColor.GOLD+"/idlookup help");
				}else if(args.length == 1){
					String arg1 = args[0];
					if(arg1.equalsIgnoreCase("help") && player.hasPermission("idlookup.help")) {
						player.sendMessage(ChatColor.GREEN+"IDLookup help");
						player.sendMessage(ChatColor.GOLD+"/idlookup help - displays this page.");
						player.sendMessage(ChatColor.GOLD+"/idlookup reload - reloads the config-file and items-file from disk.");
						player.sendMessage(ChatColor.GOLD+"/id <itemname> - returns the ItemID of mentioned item.");
					}else if(arg1.equalsIgnoreCase("reload") && player.hasPermission("idlookup.reload")) {
						this.reloadConfig();
						this.reloadItems();
						player.sendMessage(ChatColor.GREEN+"ItemID's config.yml and items.yml has been reloaded.");
					}
				}else if(args.length >= 2) {
					player.sendMessage(ChatColor.RED+"You typed too many arguments.");
				}
			}else if (commandLabel.equalsIgnoreCase("id") && player.hasPermission("idlookup.id")) {
				if(args.length == 0) {
					player.sendMessage(ChatColor.RED+"You typed too few arguments.");
				}else if(args.length == 1) {
					String arg1 = args[0];
					String lowerArg1 = arg1.toLowerCase();
					String id = this.getItems().getString(lowerArg1);
					player.sendMessage(ChatColor.GOLD+arg1+" = "+id);
				}else if(args.length >= 2) {
					player.sendMessage(ChatColor.RED+"You typed too many arguments.");
				}
			}
		}else if(sender instanceof ConsoleCommandSender) {
			Server server = getServer();
			ConsoleCommandSender console = server.getConsoleSender();
			
			if(commandLabel.equalsIgnoreCase("idlookup")) {
				if(args.length == 0) {
					console.sendMessage(ChatColor.RED+"You typed too few arguments. Try "+ChatColor.GOLD+"idlookup help");
				}else if(args.length == 1){
					String arg1 = args[0];
					if(arg1.equalsIgnoreCase("help")) {
						console.sendMessage(ChatColor.GREEN+"IDLookup help");
						console.sendMessage(ChatColor.GOLD+"/idlookup help - displays this page.");
						console.sendMessage(ChatColor.GOLD+"/idlookup reload - reloads the config-file and items-file from disk.");
						console.sendMessage(ChatColor.GOLD+"/id <itemname> - returns the ItemID of mentioned item.");
					}else if(arg1.equalsIgnoreCase("reload")) {
						this.reloadConfig();
						this.reloadItems();
						console.sendMessage(ChatColor.GREEN+"IDLookup's config.yml and items.yml has been reloaded.");
					}
				}else if(args.length >= 2) {
					console.sendMessage(ChatColor.RED+"You typed too many arguments.");
				}
			}else if (commandLabel.equalsIgnoreCase("id")) {
				if(args.length == 0) {
					console.sendMessage(ChatColor.RED+"You typed too few arguments.");
				}else if(args.length == 1) {
					String arg1 = args[0];
					String lowerArg1 = arg1.toLowerCase();
					String id = this.getItems().getString(lowerArg1);
					console.sendMessage(ChatColor.GOLD+arg1+" = "+id);
				}else if(args.length >= 2) {
					console.sendMessage(ChatColor.RED+"You typed too many arguments.");
				}
			}
		}
		return true;
	}
}
