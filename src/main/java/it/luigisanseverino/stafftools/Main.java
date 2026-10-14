package it.luigisanseverino.stafftools;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private WarnManager warnManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Inizializza il gestore dei warn
        warnManager = new WarnManager(this);

        // Registra il comando /warn
        getCommand("warn").setExecutor(new WarnCommand(this));
        getCommand("warnlist").setExecutor(new WarnCommand(this));
        getCommand("clearwarns").setExecutor(new WarnCommand(this));

        getLogger().info("StaffTools avviato!");
    }

    @Override
    public void onDisable() {
        // Salva i dati prima di spegnersi
        if (warnManager != null) {
            warnManager.saveWarns();
        }
        getLogger().info("StaffTools disattivato.");
    }

    public WarnManager getWarnManager() {
        return warnManager;
    }
}