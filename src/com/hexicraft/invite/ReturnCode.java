package com.hexicraft.invite;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;

/**
 * @author Ollie
 * @version %I%, %G%
 */
public enum ReturnCode {
    SUCCESS("", false),
    NOT_PLAYER("This command can only be run by a player.", false),
    INVALID_COMMAND("An invalid command was entered.", true),
    NOT_LOADED_PROPERLY("The plugin has not been loaded properly.", false),
    ALREADY_REDEEMED("You can only redeem a code once!", false),
    TOO_FEW_ARGUMENTS("You didn't enter enough arguments.", true),
    CODE_NOT_VALID("The code you have entered isn't valid.", false),
    INVITED_SELF("You cannot invite yourself.", false);

    private String message;
    private boolean sendUsage;

    ReturnCode(String message, boolean sendUsage) {
        this.message = message;
        this.sendUsage = sendUsage;
    }

    /**
     * Does the code have a message
     * @return true if has a message, false if empty
     */
    public boolean hasMessage() {
        return !(message.equals(""));
    }

    /**
     * Gets the return message, along with usage if required
     * @param cmd The command that was sent
     * @return The message
     */
    public String getMessage(Command cmd) {
        return message + (sendUsage ? ("\n" + ChatColor.GOLD + "Usage: " + ChatColor.RESET + cmd.getUsage()) : "");
    }

    /**
     * Gets the return message
     * @return The message
     */
    public String getMessage() {
        return message;
    }
}
