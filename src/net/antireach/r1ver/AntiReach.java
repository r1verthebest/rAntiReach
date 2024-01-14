package net.antireach.r1ver;

import net.antireach.r1ver.api.AntiReachAPI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AntiReach extends JavaPlugin {

    public static final Map<UUID, Integer> LEVELS = new HashMap<>();
    public static int MAX;

    public static void notify(Player sender, Player target, double reach) {
        int level = (Integer) LEVELS.get(target.getUniqueId());
        String notificationMessage = "§c§lANTICHEAT §fO jogador §e" + target.getName()
                + " §festá supostamente de §eReach §fBlocos: §a" + reach;
        sender.sendMessage(notificationMessage);
    }

    @Override
    public void onEnable() {
        getLogger().info("Habilitado! [v" + getDescription().getVersion() + "]");
        getServer().getPluginManager().registerEvents(new AntiReachAPI(), this);
    }
}
