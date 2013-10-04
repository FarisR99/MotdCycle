package me.faris.motdcycle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main class of the MotdCycle plugin.
 * @author KingFaris10
 */
public class Main extends JavaPlugin implements Listener {
	private String pluginVersion = "1.0.0"; // The plugin's version.

	private long motdCycleTime = 0; // Time (seconds) on how long the current MOTD should be shown.
	private List<String> motdList = new ArrayList<String>(); // The list of MOTDs.
	private String currentMotd = "&dWelcome to the server!"; // The current MOTD.

	private Permission addMotd = new Permission("motdcycle.add"); // The permission node to add MOTDs.
	private Permission deleteMotd = new Permission("motdcycle.delete"); // The permission node to delete MOTDs.
	private Permission listMotds = new Permission("motdcycle.list"); // The permission node to list the MOTDs.

	private int currentPosition = 0;

	public void onEnable() { // Called when the plugin is enabled.
		this.loadConfiguration(); // Load the configuration before doing anything.
		this.pluginVersion = this.getDescription().getVersion(); // Set the plugin version variable to the plugin's version.

		this.getCommand("motdcycle").setExecutor(this); // Register the "motdcycle" command.
		this.getCommand("addmotd").setExecutor(this); // Register the "addmotd" command.
		this.getCommand("deletemotd").setExecutor(this); // Register the "deletemotd" command.
		this.getCommand("deletemotd").setAliases(Arrays.asList("delmotd")); // Register the "delmotd" alias to the "deletemotd" command.
		this.getCommand("listmotds").setExecutor(this); // Register the "listmotd" command.

		PluginManager pluginManager = this.getServer().getPluginManager(); // Get the plugin manager.
		pluginManager.addPermission(this.addMotd); // Register the add MOTD permission node.
		pluginManager.addPermission(this.deleteMotd); // Register the delete MOTD permission node.
		pluginManager.addPermission(this.listMotds); // Register the list MOTDs permission node.
		pluginManager.registerEvents(this, this); // Register the listener.

		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				if (motdList.size() > currentPosition) {
					try {
						currentMotd = ChatColor.translateAlternateColorCodes('&', motdList.get(currentPosition));
						currentPosition++;
					} catch (Exception ex) {
						currentPosition = 0;
						if (motdList.isEmpty()) motdList.add("&dWelcome to the server!");
						currentMotd = ChatColor.translateAlternateColorCodes('&', motdList.get(currentPosition));
					}
				} else {
					currentPosition = 0;
					if (motdList.isEmpty()) motdList.add("&dWelcome to the server!");
					currentMotd = ChatColor.translateAlternateColorCodes('&', motdList.get(currentPosition));
				}
			}
		}, this.motdCycleTime * 20L, this.motdCycleTime * 20L);
	}

	public void onDisable() { // Called when the plugin is disabled.
		this.getServer().getScheduler().cancelTasks(this); // Cancel all the scheduled tasks scheduled from this plugin.
		this.currentPosition = 0;
		this.currentMotd = "&dWelcome to the server!";
		this.motdList.clear();

		PluginManager pluginManager = this.getServer().getPluginManager(); // Get the plugin manager.
		pluginManager.removePermission(this.addMotd); // Unregister the add MOTD permission node.
		pluginManager.removePermission(this.deleteMotd); // Unregister the delete MOTD permission node.
		pluginManager.removePermission(this.listMotds); // Unregister the list MOTDs permission node.
	}

	private void loadConfiguration() { // Load the configuration.
		this.getConfig().options().header("MotdCycle configuration"); // Set the header of the configuration to "MotdCycle configuration".
		this.getConfig().addDefault("MOTD cycle time", 30L); // If the "MOTD cycle time" key does not exist in the configuration, add it with the default value of 30 seconds.
		this.getConfig().addDefault("MOTD list", Arrays.asList("&dWelcome to the server!")); // If the "MOTD list" key does not exist in the configuration, add it with the value of "&dWelcome to the server!".
		this.getConfig().options().copyDefaults(true); // Copy the default values to the configuration.
		this.getConfig().options().copyHeader(true); // Copy the header value to the configuration.
		this.saveConfig(); // Save the configuration.

		this.currentPosition = 0;
		this.motdCycleTime = this.getConfig().getLong("MOTD cycle time");
		this.motdList = this.getConfig().getStringList("MOTD list"); // Set the list of MOTDs to the list in the configuration.
		if (this.motdList.isEmpty()) this.motdList.add("&dWelcome to the server!"); // If the MOTD list is empty, add a default value.
		this.currentMotd = ChatColor.translateAlternateColorCodes('&', this.motdList.get(0)); // Make the current MOTD the first MOTD in the MOTD list and replace the colour codes with chat colours.
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) { // Called when a command registered to this plugin is entered.
		String command = cmd.getName();
		if (command.equals("motdcycle")) { // If the command is "motdcycle"
			sender.sendMessage(ChatColor.GOLD + "MotdCycle v" + this.pluginVersion + " by " + ChatColor.RED + "KingFaris10"); // Send them plugin information.
			sender.sendMessage(ChatColor.GOLD + "Current MOTD: " + this.currentMotd); // Send them the current MOTD.
			return true; // Makes the command valid.
		} else if (command.equals("addmotd")) { // If the command is "addmotd"
			if (sender.hasPermission(this.addMotd)) { // If the player has permission to use that command.
				if (args.length > 0) { // If the command's usage is "addmotd <motd>".
					String motd = ""; // The MOTD string.
					for (int i = 0; i < args.length; i++) { // Convert the command arguments array into a String.
						if (i == args.length - 1) motd += args[i];
						else motd += args[i] + " ";
					}
					if (!this.removeListColours(this.motdList).contains(ChatColor.stripColor(motd))) { // If the MOTD list doesn't contain the MOTD.
						String theMotd = ChatColor.translateAlternateColorCodes('&', motd); // Replace the MOTD's colour codes with chat colours.
						this.motdList.add(motd); // Add the MOTD to the MOTD list.
						this.getConfig().set("MOTD list", this.motdList); // Update the MOTD in the configuration.
						this.saveConfig(); // Save the configuration.
						sender.sendMessage(ChatColor.GOLD + "Added the MOTD: " + ChatColor.WHITE + theMotd);
					} else {
						sender.sendMessage(ChatColor.RED + "That MOTD already exists.");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.DARK_RED + "/addmotd <motd>");
				}
			} else {
				sender.sendMessage(this.getAccessDeniedMessage()); // Send the player the access denied message.
			}
			return true; // Makes the command valid.
		} else if (command.equals("deletemotd")) { // If the command is "deletemotd"
			if (sender.hasPermission(this.deleteMotd)) { // If the player has permission to use that command.
				if (args.length > 0) {
					if (args.length == 1 && this.isInteger(args[0])) { // If the command's usage is "deletemotd <motdposition>".
						int motdPosition = Integer.parseInt(args[0]); // Get the MOTD position.
						if (motdPosition > 0 && this.motdList.size() >= motdPosition) { // If the MOTD position is bigger than 0 and exists in the MOTD list. 
							String motd = this.motdList.get(motdPosition - 1); // Get the deleted MOTD.
							this.motdList.remove(motd); // Remove the MOTD from the list.
							String theMotd = ChatColor.translateAlternateColorCodes('&', motd); // Replace the colour codes with chat colours.
							sender.sendMessage(ChatColor.GOLD + "Removed the MOTD: " + ChatColor.WHITE + theMotd); // Send the player a message.
							if (ChatColor.stripColor(this.currentMotd) == ChatColor.stripColor(theMotd)) { // If the current MOTD is the deleted MOTD.
								if (this.motdList.isEmpty()) { // If the MOTD list is empty.
									this.motdList.add("&dWelcome to the server!"); // Add the default MOTD.
								}
								this.currentMotd = ChatColor.translateAlternateColorCodes('&', this.motdList.get(0));
							}
							this.getConfig().set("MOTD list", this.motdList); // Update the MOTD in the configuration.
							this.saveConfig(); // Save the configuration.
						} else {
							sender.sendMessage(ChatColor.RED + "An MOTD at that position could not be found.");
						}
					} else {
						String motd = ""; // The MOTD string.
						for (int i = 0; i < args.length; i++) { // Convert the command arguments array into a String.
							if (i == args.length - 1) motd += args[i];
							else motd += args[i] + " ";
						}
						if (this.removeListColours(this.motdList).contains(ChatColor.stripColor(motd))) { // If the MOTD list contains the MOTD.
							this.motdList.remove(motd); // Remove the MOTD from the list.
							String theMotd = ChatColor.translateAlternateColorCodes('&', motd); // Replace the colour codes with chat colours.
							sender.sendMessage(ChatColor.GOLD + "Removed the MOTD: " + ChatColor.WHITE + theMotd); // Send the player a message.
							if (ChatColor.stripColor(this.currentMotd) == ChatColor.stripColor(theMotd)) { // If the current MOTD is the deleted MOTD.
								if (this.motdList.isEmpty()) { // If the MOTD list is empty.
									this.motdList.add("&dWelcome to the server!"); // Add the default MOTD.
								}
								this.currentMotd = ChatColor.translateAlternateColorCodes('&', this.motdList.get(0));
							}
							this.getConfig().set("MOTD list", this.motdList); // Update the MOTD in the configuration.
							this.saveConfig(); // Save the configuration.
						} else {
							sender.sendMessage(ChatColor.RED + "That MOTD does not exist in the MOTD list.");
						}
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.DARK_RED + "/deletemotd <motd|motdposition>");
				}
			} else {
				sender.sendMessage(this.getAccessDeniedMessage()); // Send the player the access denied message.
			}
			return true; // Makes the command valid.
		} else if (command.equals("listmotds")) { // If the command is "listmotds"
			if (sender.hasPermission(this.listMotds)) { // If the player has permission to use that command.
				sender.sendMessage(ChatColor.GOLD + "MotdCycle MOTDs:"); // Send them the default message.
				for (int i = 0; i < this.motdList.size(); i++) { // Loop through all the MOTDs
					sender.sendMessage(ChatColor.GOLD + String.valueOf(i + 1) + ". " + ChatColor.DARK_RED + ChatColor.translateAlternateColorCodes('&', this.motdList.get(i))); // Send them the MOTD and its position..
				}
			} else {
				sender.sendMessage(this.getAccessDeniedMessage()); // Send the player the access denied message.
			}
			return true; // Makes the command valid.
		}

		return false; // Makes the command invalid.
	}

	@EventHandler
	public void onServerPing(ServerListPingEvent event) { // Called when the server is pinged on the server list.
		try {
			if (this.currentMotd != null) event.setMotd(this.currentMotd); // If the current MOTD is not null, make the MOTD the current MOTD.
		} catch (Exception ex) {
		}
	}

	private String getAccessDeniedMessage() { // Get the access denied message.
		return ChatColor.DARK_RED + "You do not have access to that command."; // Return the access denied message.
	}

	private boolean isInteger(String aString) { // Get if a string is numeric.
		try {
			Integer.parseInt(aString); // Try converting the String into an Integer.
			return true; // Return true if successful.
		} catch (Exception ex) {
			return false; // Return false if the string is not numeric.
		}
	}

	private List<String> removeListColours(List<String> list) {
		List<String> list2 = new ArrayList<String>();
		for (String listItem : list)
			list2.add(ChatColor.stripColor(listItem));
		return list2;
	}

}
