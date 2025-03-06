package org.frizzlenpop.frizzlenEco.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Utility class for sending formatted messages
 */
public class MessageUtil {
    
    /**
     * Sends an info message to a command sender
     * @param sender the command sender
     * @param message the message to send
     */
    public static void sendInfo(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.GREEN + "[FrizzlenEco] " + ChatColor.RESET + message);
    }
    
    /**
     * Sends a success message to a command sender
     * @param sender the command sender
     * @param message the message to send
     */
    public static void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.GREEN + "[FrizzlenEco] " + ChatColor.YELLOW + message);
    }
    
    /**
     * Sends an error message to a command sender
     * @param sender the command sender
     * @param message the message to send
     */
    public static void sendError(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.RED + "[FrizzlenEco] " + ChatColor.RESET + message);
    }
    
    /**
     * Sends a warning message to a command sender
     * @param sender the command sender
     * @param message the message to send
     */
    public static void sendWarning(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.GOLD + "[FrizzlenEco] " + ChatColor.RESET + message);
    }
    
    /**
     * Formats a message with color codes
     * @param message the message to format
     * @return the formatted message
     */
    public static String formatMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
} 