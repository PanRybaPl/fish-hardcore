package pl.panryba.mc.hardcore;

import me.confuser.barapi.BarAPI;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import pl.panryba.mc.hardcore.entities.HardcorePeriod;
import pl.panryba.mc.hardcore.events.HardcoreTeleportEvent;

import java.util.*;

public class PluginApi {

    private long minBanSeconds;
    private long maxBanSeconds;
    private String unbanUrlTemplate;
    private boolean banEnabled;
    private long protectionSeconds;
    private long teleportDelaySeconds;
    private long tpCooldownSeconds;

    private final Set<Player> disabledWarnings;
    private final Map<Player, BukkitTask> schedules;
    private final Map<String, Map<String, Date>> pvp;
    private final Map<String, String> lastDamagers;
    private final Map<String, Long> lastTeleports;
    private final HardcoreDatabase database;
    private final Plugin plugin;

    private static PluginApi instance;

    public static void setup(PluginApi instance) {
        PluginApi.instance = instance;
    }

    public static PluginApi getInstance() {
        return PluginApi.instance;
    }

    public PluginApi(Plugin plugin, FileConfiguration config, HardcoreDatabase database) {
        this.plugin = plugin;
        this.database = database;

        loadConfig(config);

        this.schedules = new HashMap<>();
        this.pvp = new HashMap<>();
        this.lastDamagers = new HashMap<>();
        this.disabledWarnings = new HashSet<>();
        this.lastTeleports = new HashMap<>();
    }

    public void setLastDamager(Player damaged, Player damager) {
        this.lastDamagers.put(damaged.getName(), damager.getName());
    }

    public Player getLastDamager(Player damaged) {
        String damagerName = this.lastDamagers.get(damaged.getName());

        if (damagerName == null || damagerName.isEmpty()) {
            return null;
        }

        return damaged.getServer().getPlayerExact(damagerName);
    }

    public void banPlayerForDeath(Player player, HardcorePeriod period, Date date) {
        HardcorePeriod.setForPlayer(this.database, period, player.getName(), date.getTime());
    }

    void strikeLightning(Entity entity) {
        World world = entity.getWorld();
        world.strikeLightningEffect(entity.getLocation());
    }

    void kickPlayerAfterDeath(Player player, String message) {
        new KickPlayerTask(player, message).runTaskLater(this.plugin, 1);
    }

    long getBanSeconds() {
        Server server = Bukkit.getServer();
        long seconds = this.minBanSeconds
                + (long) (server.getOnlinePlayers().length / (float) server.getMaxPlayers())
                * (this.maxBanSeconds - this.minBanSeconds);

        return seconds;
    }

    boolean isBanEnabled() {
        return this.banEnabled;
    }

    public boolean schedulePlayerTeleport(final Player player, final Location to) {
        if (player == null) {
            return false;
        }

        if (to == null) {
            return false;
        }

        cancelTeleport(player);

        HardcoreTeleportEvent testEvent = new HardcoreTeleportEvent(player, to);
        player.getServer().getPluginManager().callEvent(testEvent);

        if (testEvent.isCancelled()) {
            player.sendMessage(ChatColor.GRAY + "Nie mozesz teleportowac sie do wybranego miejsca");
            return true;
        }

        if (canTeleportWithoutDelay(player)) {
            return false;
        }

        if (isTpCooldownEnabled()) {
            if (!handleTpCooldown(player)) {
                return true;
            }
        }

        player.sendMessage(ChatColor.GRAY + "Teleportacja nastapi za " + this.teleportDelaySeconds + " sekund (nie ruszaj sie)");
        schedules.put(player, new BukkitRunnable() {

            @Override
            public void run() {
                schedules.remove(player);
                player.teleport(to);

                if (PluginApi.this.isTpCooldownEnabled()) {
                    PluginApi.this.lastTeleports.put(player.getName(), new Date().getTime());
                }
            }

        }.runTaskLater(this.plugin, 20 * this.teleportDelaySeconds));
        return true;
    }

    boolean gotScheduledTeleport(Player player) {
        return this.schedules.get(player) != null;
    }

    void cancelTeleport(Player player) {
        if (player == null) {
            return;
        }

        BukkitTask task = schedules.get(player);
        if (task == null) {
            return;
        }

        task.cancel();
        schedules.remove(player);

        player.sendMessage(ChatColor.GRAY + "Teleportowanie zostalo anulowane");
    }

    void setLastPvp(Player player, Player opponent, Date now) {
        Map<String, Date> playerPvp = getOrCreatePlayerPvp(player);
        playerPvp.put(opponent.getName(), now);
    }

    public boolean isInvolvedInPvp(Player player, Date after) {
        Map<String, Date> playerPvp = getOrCreatePlayerPvp(player);

        for (Date date : playerPvp.values()) {
            if (date.getTime() > after.getTime()) {
                return true;
            }
        }

        return false;
    }

    public boolean isCurrentlyInvolvedInPvp(Player player) {
        Date after = new Date(new Date().getTime() - 10 * 1000);
        return isInvolvedInPvp(player, after);
    }

    public void clearPvp(Player player) {
        String playerName = player.getName();
        Map<String, Date> playerPvp = getPlayerPvp(playerName);

        if (playerPvp == null) {
            return;
        }

        // Remove the player pvp info from his opponents pvp infos
        for (String opponent : playerPvp.keySet()) {
            Map<String, Date> opponentPvp = getPlayerPvp(opponent);
            if (opponentPvp != null) {
                opponentPvp.remove(playerName);
            }
        }

        this.pvp.remove(playerName);
    }

    private Map<String, Date> getPlayerPvp(String playerName) {
        return this.pvp.get(playerName);
    }

    private Map<String, Date> getOrCreatePlayerPvp(Player player) {
        String playerName = player.getName();
        Map<String, Date> playerPvp = getPlayerPvp(playerName);

        if (playerPvp == null) {
            playerPvp = new HashMap<>();
            this.pvp.put(playerName, playerPvp);
        }

        return playerPvp;

    }

    void sendToOtherPlayers(Player player, String message) {
        for (Player otherPlayer : player.getServer().getOnlinePlayers()) {
            if (otherPlayer.getName().equals(player.getName())) {
                continue;
            }

            otherPlayer.sendMessage(message);
        }
    }

    public long getBanSeconds(HardcorePeriod period) {
        if (period == null) {
            return 0;
        }

        long hardcorePeriod = period.getValidUntil();

        Long now = new Date().getTime();
        if (now >= hardcorePeriod) {
            return 0;
        }

        return (long) Math.floor((hardcorePeriod - now) / 1000);
    }

    String getUnbanUrl(Player player) {
        return String.format(this.unbanUrlTemplate, player.getName());
    }

    boolean hasBanProtection(Player player) {
        return player.hasPermission("hardcore.ban.protection");
    }

    public BanAffectionResult checkLightweightBanAffection(Player player) {
        if (!isBanEnabled()) {
            return new BanAffectionResult(BanAffectionReason.BAN_NOT_ENABLED);
        }

        if (player.isOp()) {
            return new BanAffectionResult(BanAffectionReason.IS_OP);
        }

        if (hasBanProtection(player)) {
            return new BanAffectionResult(BanAffectionReason.HAS_BAN_PROTECTION);
        }

        return new BanAffectionResult(BanAffectionReason.AFFECTED);
    }

    public BanAffectionResult checkBanAffection(HardcorePeriod period) {
        if (period != null) {
            Long playerProtectionSeconds = period.getProtectionSeconds();

            if (playerProtectionSeconds > 0) {
                return new BanAffectionResult(BanAffectionReason.HAS_BAN_PROTECTION, playerProtectionSeconds);
            }
        }

        return new BanAffectionResult(BanAffectionReason.AFFECTED);
    }

    public final void loadConfig(FileConfiguration config) {
        this.protectionSeconds = config.getLong("newbie_protection_seconds", 3 * 24 * 60 * 60);
        this.minBanSeconds = config.getLong("min_ban_time", 3600);
        this.maxBanSeconds = config.getLong("max_ban_time", 7200);
        this.banEnabled = config.getBoolean("ban_enabled", false);
        this.unbanUrlTemplate = config.getString("unban_url");
        this.teleportDelaySeconds = config.getLong("teleport_delay_seconds", 5);
        this.tpCooldownSeconds = config.getLong("teleport_cooldown_seconds", 0);
    }

    public boolean extendProtection(Player player, long seconds) {
        HardcorePeriod period = HardcorePeriod.getForPlayer(database, player.getName());

        long protectedUntil = 0;

        if (period == null) {
            period = new HardcorePeriod();
            period.setPlayerName(player.getName());
        } else {
            protectedUntil = period.getProtectedUntil();
        }

        long now = new Date().getTime();
        if(protectedUntil < now) {
            protectedUntil = now;
        }

        protectedUntil += seconds * 1000;

        period.setProtectedUntil(protectedUntil);
        this.database.save(period);

        return true;
    }

    public boolean protectFor(Player player, long secondsFromNow) {
        HardcorePeriod period = HardcorePeriod.getForPlayer(database, player.getName());

        long now = new Date().getTime();

        if (period != null) {
            if (period.getProtectedUntil() >= now) {
                return false;
            }
        }

        if (period == null) {
            period = new HardcorePeriod();
            period.setPlayerName(player.getName());
        }

        period.setProtectedUntil(now + secondsFromNow * 1000);
        this.database.save(period);

        return true;
    }

    void playerRegistered(Player player) {
        if (this.protectionSeconds > 0) {
            this.protectFor(player, protectionSeconds);
        }
    }

    void updatePvpBars() {
        Date after = new Date(new Date().getTime() - 10 * 1000);

        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            boolean inPvp = isInvolvedInPvp(player, after);
            if (inPvp) {
                if (!this.disabledWarnings.contains(player) && !BarAPI.hasBar(player)) {
                    BarAPI.setMessage(player, "Walczysz - nie wychodz z gry!");
                }
            } else {
                if (BarAPI.hasBar(player)) {
                    BarAPI.removeBar(player);
                }
            }
        }
    }

    public boolean switchWarning(Player player) {
        if (this.disabledWarnings.contains(player)) {
            this.clearDisabledWarning(player);
            return true;
        } else {
            this.disabledWarnings.add(player);
            return false;
        }
    }

    void clearDisabledWarning(Player player) {
        this.disabledWarnings.remove(player);
    }

    public HardcorePeriod getHardcorePeriod(Player player) {
        return HardcorePeriod.getForPlayer(database, player.getName());
    }

    private boolean handleTpCooldown(Player player) {
        if (!player.hasPermission("hardcore.teleport.nocooldown")) {
            Long lastTp = this.lastTeleports.get(player.getName());
            if (lastTp != null) {
                long diff = new Date().getTime() - lastTp;
                long toWait = this.tpCooldownSeconds * 1000 - diff;

                if (toWait > 0) {
                    long toWaitSecs = (long) Math.ceil(toWait / 1000.0d);
                    player.sendMessage(ChatColor.GRAY + "Musisz ochlonac po ostatniej teleportacji - poczekaj jeszcze " + ChatColor.BLUE + toWaitSecs + "s");
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isTpCooldownEnabled() {
        return this.tpCooldownSeconds > 0;
    }

    private boolean canTeleportWithoutDelay(Player player) {
        return player.hasPermission("hardcore.teleport.instant");
    }

}
