package com.hexicraft.invite;

import com.hexicraft.invite.logger.HexiLogger;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

/**
 * @author Ollie
 * @version 1.0
 */
public class Main extends JavaPlugin {

    private YamlFile codes;
    private ListFile redeemed;
    private boolean enabled;
    private Random random = new Random();
    private Economy econ = null;
    private HexiLogger logger = new HexiLogger(getDataFolder());

    private int inviteeMoney;
    private int inviterMoney;

    /**
     * Run when the plugin is enabled
     */
    @Override
    public void onEnable() {
        reload();
    }

    private void reload() {
        enabled = false;

        if (!setupEconomy()) {
            getLogger().severe("Missing dependency: Vault and/or compatible economy plugin.");
            return;
        }

        if (!setupConfig()) {
            getLogger().severe("Could not load codes.yml.");
            return;
        }

        if (!setupCodes()) {
            getLogger().severe("Could not load codes.yml.");
            return;
        }

        if (!setupRedeemed()) {
            getLogger().severe("Could not load redeemed.txt.");
            return;
        }

        enabled = true;
    }

    /**
     * Sets the economy plugin
     * @return True if Vault is found, false otherwise
     */
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupConfig() {
        YamlFile config = new YamlFile(this, "config.yml");
        if (!config.loadFile()) {
            return false;
        }

        inviteeMoney = config.getInt("invitee-money");
        inviterMoney = config.getInt("inviter-money");
        return true;
    }

    private boolean setupCodes() {
        codes = new YamlFile(this, "codes.yml");
        return codes.loadFile();
    }

    private boolean setupRedeemed() {
        redeemed = new ListFile(this, getDataFolder(), "redeemed.txt");
        return redeemed.loadFile();
    }

    /**
     * Executes the given command, returning its success
     * @param sender Source of the command
     * @param cmd Command which was executed
     * @param label Alias of the command which was used
     * @param args Passed command arguments
     * @return Always returns true, error messages are handled internally
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        ReturnCode code;

        if (!enabled && !(Objects.equals(cmd.getName().toLowerCase(), "invite") &&
                args.length > 0 &&
                Objects.equals(args[0], "reload"))) {
            code = ReturnCode.NOT_LOADED_PROPERLY;
        } else if (!(sender instanceof Player)) {
            code = ReturnCode.NOT_PLAYER;
        } else {
            Player player = (Player) sender;
            switch (cmd.getName().toLowerCase()) {
                case "invite":
                    code = invite(player, args);
                    break;
                case "code":
                    code = code(player);
                    break;
                case "redeem":
                    code = redeem(player, args);
                    break;
                default:
                    code = ReturnCode.INVALID_COMMAND;
            }
        }

        if (code != null && code.hasMessage()) {
            // Send the resulting message to the sender
            sender.sendMessage(ChatColor.RED + code.getMessage(cmd));
        }
        return true;
    }

    private ReturnCode invite(Player player, String[] args) {
        if (args.length == 1 && Objects.equals(args[0], "reload") && player.hasPermission("hexiinvite.admin")) {
            reload();
            player.sendMessage(ChatColor.GOLD + "HexiInvite has been reloaded.");
            return ReturnCode.SUCCESS;
        } else {
            player.sendMessage(ChatColor.DARK_GRAY + "- - - " +
                    ChatColor.RED + "⬢" + ChatColor.GOLD + "⬢" + ChatColor.DARK_RED + "⬢" +
                    ChatColor.WHITE + " HexiInvite " +
                    ChatColor.RED + "⬢" + ChatColor.GOLD + "⬢" + ChatColor.DARK_RED + "⬢" +
                    ChatColor.DARK_GRAY + " - - -");
            player.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD + "/code" + ChatColor.WHITE +
                    " - Display your unique invite code");
            player.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD + "/redeem <code>" + ChatColor.WHITE +
                    " - Redeem an invite code to receive a reward");
            player.sendMessage(ChatColor.DARK_GRAY + "- ");
            player.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD + "You will receive " +
                    ChatColor.WHITE + inviterMoney + ChatColor.GOLD + " for inviting a player.");
            player.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD + "They will receive " +
                    ChatColor.WHITE + inviteeMoney + ChatColor.GOLD + " for being invited.");
            return ReturnCode.SUCCESS;
        }
    }

    private ReturnCode code(Player player) {
        String code = getCode(player.getUniqueId().toString());

        TextComponent codeMessage = new TextComponent(code);
        codeMessage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                "http://www.hexicraft.com/invite?c=" + code));
        codeMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Go to code webpage").create()));
        codeMessage.setColor(net.md_5.bungee.api.ChatColor.WHITE);

        TextComponent message = new TextComponent("  ----> ");
        message.setColor(net.md_5.bungee.api.ChatColor.GOLD);
        message.addExtra(codeMessage);
        message.addExtra(" <----");

        player.sendMessage(ChatColor.DARK_GRAY + "- - - - - " +
                ChatColor.GOLD + "Your " +
                ChatColor.WHITE + "code" +
                ChatColor.DARK_GRAY + " - - - - -");

        player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC +
                "       Give your code to a\n" +
                "  friend and when they type\n" +
                "   /redeem <code> you will\n" +
                "    both receive rewards.\n" +
                " \n" +
                ChatColor.GOLD + "          Click the code!");

        player.spigot().sendMessage(message);

        player.sendMessage(" \n" +
                ChatColor.DARK_GRAY + "- - - - - - -  -  - - - - - - -");
        return ReturnCode.SUCCESS;
    }

    private String getCode(String uuid) {
        String code = findCode(uuid);
        if (code != null) {
            return code;
        } else {
            code = new BigInteger(64, random).toString(32);
            codes.set(code, uuid);
            return code;
        }
    }

    private String findCode(String uuid) {
        Map<String, Object> values = codes.getValues(false);
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (Objects.equals(entry.getValue(), uuid)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private ReturnCode redeem(Player player, String[] args) {
        if (redeemed.contains(player.getUniqueId().toString())) {
            return ReturnCode.ALREADY_REDEEMED;
        } else if (args.length == 0) {
            return ReturnCode.TOO_FEW_ARGUMENTS;
        } else if (!codes.contains(args[0])) {
            return ReturnCode.CODE_NOT_VALID;
        } else if(Objects.equals(player.getUniqueId().toString(), codes.getString(args[0]))) {
            return ReturnCode.INVITED_SELF;
        } else {
            UUID inviterUuid = UUID.fromString(codes.getString(args[0]));
            econ.depositPlayer(player, inviteeMoney);
            econ.depositPlayer(Bukkit.getOfflinePlayer(inviterUuid), inviterMoney);
            redeemed.add(player.getUniqueId().toString());

            player.sendMessage(ChatColor.GOLD + "Code redeemed!");
            player.sendMessage(ChatColor.WHITE + econ.format(inviteeMoney) +
                    ChatColor.GOLD + " has been added to your account.");

            Player inviter = Bukkit.getServer().getPlayer(inviterUuid);
            if (inviter != null) { // Message person who owns the code if they're online
                inviter.sendMessage(ChatColor.GOLD + "You have invited " +
                        ChatColor.WHITE + player.getName() +
                        ChatColor.GOLD + " to Hexicraft!");
                inviter.sendMessage(ChatColor.WHITE + econ.format(inviterMoney) +
                        ChatColor.GOLD + " has been added to your account.");
            }
            logger.log("\"" + inviterUuid + "\" invited \"" + player.getUniqueId() + "\"");
            return ReturnCode.SUCCESS;
        }
    }
}
