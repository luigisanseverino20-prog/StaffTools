package it.luigisanseverino.stafftools;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WarnManager {

    private final Main plugin;
    private final File warnsFile;
    private YamlConfiguration warnsConfig;

    // Mappa: nomGiocatore -> (categoria -> contatore)
    private final Map<String, Map<String, Integer>> warns = new HashMap<>();

    public WarnManager(Main plugin) {
        this.plugin = plugin;
        this.warnsFile = new File(plugin.getDataFolder(), "warns.yml");
        loadWarns();
    }

    // Carica i warn dal file
    private void loadWarns() {
        if (!warnsFile.exists()) {
            try {
                warnsFile.getParentFile().mkdirs();
                warnsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Errore creazione warns.yml!");
            }
        }
        warnsConfig = YamlConfiguration.loadConfiguration(warnsFile);

        for (String player : warnsConfig.getKeys(false)) {
            Map<String, Integer> playerWarns = new HashMap<>();
            for (String categoria : warnsConfig.getConfigurationSection(player).getKeys(false)) {
                playerWarns.put(categoria, warnsConfig.getInt(player + "." + categoria));
            }
            warns.put(player, playerWarns);
        }
    }

    // Salva i warn nel file
    public void saveWarns() {
        for (Map.Entry<String, Map<String, Integer>> entry : warns.entrySet()) {
            for (Map.Entry<String, Integer> warnEntry : entry.getValue().entrySet()) {
                warnsConfig.set(entry.getKey() + "." + warnEntry.getKey(), warnEntry.getValue());
            }
        }
        try {
            warnsConfig.save(warnsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Errore salvataggio warns.yml!");
        }
    }

    // Aggiunge un warn e restituisce il contatore attuale
    public int addWarn(String player, String categoria) {
        warns.putIfAbsent(player, new HashMap<>());
        Map<String, Integer> playerWarns = warns.get(player);
        int count = playerWarns.getOrDefault(categoria, 0) + 1;
        playerWarns.put(categoria, count);
        saveWarns();
        return count;
    }

    // Restituisce tutti i warn di un giocatore
    public Map<String, Integer> getWarns(String player) {
        return warns.getOrDefault(player, new HashMap<>());
    }

    // Azzera i warn di un giocatore
    public void clearWarns(String player) {
        warns.remove(player);
        warnsConfig.set(player, null);
        saveWarns();
    }

    // Restituisce il limite warn per una categoria
    public int getLimit(String categoria) {
        return plugin.getConfig().getInt("warn-limits." + categoria, 0);
    }
}