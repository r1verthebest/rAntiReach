package net.antireach.r1ver.api;

import net.antireach.r1ver.AntiReach;
import net.antireach.r1ver.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.function.Predicate;

public class AntiReachAPI implements Listener {

    private static final long MESSAGE_DELAY = 10000L;
    private static final double TOLERANCE = 0.1D;
    private long lastMessageTime = 0L;

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (isValidAttack(event)) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            double distance = calculateDistance(attacker, victim);

            if (isReachOutOfRange(distance, attacker)) {
                int update = updateReachLevel(attacker);

                if (canSendMessage()) {
                    notifyStaff(attacker, distance);
                    lastMessageTime = System.currentTimeMillis();
                }

                if (update == AntiReach.MAX) {
                    AntiReach.LEVELS.remove(attacker.getUniqueId());
                }

                cancelDamage(event);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        if (isMoveSuspicious(from, to, player)) {
        }
    }

    private boolean canSendMessage() {
        return System.currentTimeMillis() - lastMessageTime >= MESSAGE_DELAY;
    }

    private boolean isValidAttack(EntityDamageByEntityEvent event) {
        return event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK &&
                event.getDamager() instanceof Player &&
                event.getEntity() instanceof Player &&
                ((Player) event.getDamager()).getGameMode() != GameMode.CREATIVE;
    }

    private boolean isReachOutOfRange(double distance, Player attacker) {
        double threshold = 4.8D + getLatency(attacker);
        return distance >= threshold;
    }

    private boolean isMoveSuspicious(Location from, Location to, Player player) {
        double distance = from.distance(to);
        double toleranceAdjusted = TOLERANCE + getLatency(player);
        return distance > toleranceAdjusted;
    }

    private void cancelDamage(EntityDamageByEntityEvent event) {
        event.setDamage(0.0D);
    }

    private void notifyStaff(Player attacker, double distance) {
        Bukkit.getOnlinePlayers().stream()
                .filter(hasAnticheatPermission())
                .forEach(staff -> AntiReach.notify(staff, attacker, distance));
    }

    private Predicate<? super Player> hasAnticheatPermission() {
        return staff -> staff.hasPermission("shield.anticheat");
    }

    private int updateReachLevel(Player attacker) {
        return AntiReach.LEVELS.compute(attacker.getUniqueId(), (uuid, current) -> current == null ? 1 : current + 1);
    }

    private double calculateDistance(Player attacker, Player victim) {
        Location loc1 = attacker.getLocation();
        Location loc2 = victim.getLocation();
        double distance = loc1.distance(loc2);

        if (loc1.getY() < loc2.getY()) {
            distance -= (loc2.getY() - loc1.getY()) / 5.0D;
        }

        return Utils.cut(distance, 1);
    }

    private double getLatency(Player player) {
        int ping = ((CraftPlayer) player).getHandle().ping;

        if (ping < 300 && ping > 100) {
            return (double) ping / 100.0D / 10.0D * 1.1D;
        }

        return 0.0D;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        AntiReach.LEVELS.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        AntiReach.LEVELS.remove(event.getPlayer().getUniqueId());
    }
}
