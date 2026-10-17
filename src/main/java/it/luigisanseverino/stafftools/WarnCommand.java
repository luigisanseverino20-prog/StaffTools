package it.luigisanseverino.stafftools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class WarnCommand implements CommandExecutor {

    private final Main plugin;

    public WarnCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Comando /warn
        if (command.getName().equalsIgnoreCase("warn")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Uso: /warn <giocatore> <categoria> <motivo>");
                return true;
            }

            String playerName = args[0];
            String categoria = args[1].toLowerCase();
            String motivo = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));

            Player target = Bukkit.getPlayer(playerName);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Giocatore non trovato o non online.");
                return true;
            }

            // Aggiunge il warn
            int count = plugin.getWarnManager().addWarn(playerName, categoria);
            int limit = plugin.getWarnManager().getLimit(categoria);

            // Messaggio all'admin
            String adminMsg = plugin.getConfig().getString("messages.warn-sent")
                    .replace("{player}", playerName)
                    .replace("{motivo}", motivo);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', adminMsg));

            // Messaggio al giocatore
            String playerMsg = plugin.getConfig().getString("messages.warn-received")
                    .replace("{motivo}", motivo)
                    .replace("{count}", String.valueOf(count))
                    .replace("{max}", limit > 0 ? String.valueOf(limit) : "∞");
            target.sendMessage(ChatColor.translateAlternateColorCodes('&', playerMsg));

            // Ban automatico se raggiunge il limite
            if (limit > 0 && count >= limit) {
                String banMsg = plugin.getConfig().getString("messages.auto-ban")
                        .replace("{player}", playerName)
                        .replace("{count}", String.valueOf(count))
                        .replace("{categoria}", categoria);
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', banMsg));
                target.kickPlayer(ChatColor.RED + "Sei stato bannato automaticamente dopo " + count + " warn per: " + categoria);
                Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(playerName, "Bannato automaticamente: " + count + " warn per " + categoria, null, "StaffTools");
            }

            return true;
        }

        // Comando /warnlist
        if (command.getName().equalsIgnoreCase("warnlist")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Uso: /warnlist <giocatore>");
                return true;
            }

            String playerName = args[0];
            Map<String, Integer> playerWarns = plugin.getWarnManager().getWarns(playerName);

            if (playerWarns.isEmpty()) {
                sender.sendMessage(ChatColor.GREEN + playerName + " non ha warn.");
                return true;
            }

            sender.sendMessage(ChatColor.YELLOW + "=== Warn di " + playerName + " ===");
            for (Map.Entry<String, Integer> entry : playerWarns.entrySet()) {
                sender.sendMessage(ChatColor.RED + entry.getKey() + ": " + ChatColor.WHITE + entry.getValue());
            }
            return true;
        }

        // Comando /clearwarns
        if (command.getName().equalsIgnoreCase("clearwarns")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Uso: /clearwarns <giocatore>");
                return true;
            }

            String playerName = args[0];
            plugin.getWarnManager().clearWarns(playerName);
            sender.sendMessage(ChatColor.GREEN + "Warn di " + playerName + " azzerati.");
            return true;
        }

        // Comando /unban
        if (command.getName().equalsIgnoreCase("unban")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Uso: /unban <giocatore>");
                return true;
            }

            String playerName = args[0];
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(playerName);
            sender.sendMessage(ChatColor.GREEN +
                    "[StaffTools] " + playerName + " è stato sbannato.");
            return true;
        }

// Comando /unwarn
        if (command.getName().equalsIgnoreCase("unwarn")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Uso: /unwarn <giocatore> <categoria>");
                return true;
            }

            String playerName = args[0];
            String categoria = args[1].toLowerCase();
            Map<String, Integer> playerWarns = plugin.getWarnManager().getWarns(playerName);

            if (!playerWarns.containsKey(categoria)) {
                sender.sendMessage(ChatColor.RED + playerName +
                        " non ha warn per la categoria: " + categoria);
                return true;
            }

            playerWarns.remove(categoria);
            plugin.getWarnManager().saveWarns();
            sender.sendMessage(ChatColor.GREEN +
                    "[StaffTools] Warn " + categoria + " di " + playerName + " rimosso.");
            return true;
        }

        return false;
    }
}