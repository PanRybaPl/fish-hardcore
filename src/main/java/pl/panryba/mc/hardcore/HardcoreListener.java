package pl.panryba.mc.hardcore;

import java.util.Date;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import pl.panryba.mc.auth.PlayerRegisteredEvent;
import pl.panryba.mc.hardcore.entities.HardcorePeriod;
import pl.panryba.mc.pl.LanguageHelper;

/**
 *
 * @author PanRyba.pl
 */
public class HardcoreListener implements Listener {

    private final PluginApi api;

    public HardcoreListener(PluginApi api) {
        this.api = api;
    }

    @EventHandler
    public void onPlayerRegistered(PlayerRegisteredEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        api.playerRegistered(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event == null) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        if (event.getDamage() <= 0) {
            return;
        }

        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }

        Player damaged = (Player) event.getEntity();
        if (damaged == null) {
            return;
        }

        Player damager = null;

        if (event.getDamager() instanceof Player) {
            damager = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                damager = (Player) projectile.getShooter();
            }
        }

        if (damager == null) {
            return;
        }

        if (damaged.getName().equals(damager.getName())) {
            return;
        }

        api.setLastDamager(damaged, damager);

        Date now = new Date();

        if (!damaged.isOp()) {
            api.setLastPvp(damaged, damager, now);
        }

        if (!damager.isOp()) {
            api.setLastPvp(damager, damaged, now);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (player == null) {
            return;
        }

        api.strikeLightning(player);
        api.clearPvp(player);

        HardcorePeriod hcPeriod = null;

        BanAffectionResult banCheck = api.checkLightweightBanAffection(player);
        if (banCheck.getReason() == BanAffectionReason.AFFECTED) {
            hcPeriod = api.getHardcorePeriod(player);
            banCheck = api.checkBanAffection(hcPeriod);
        }

        switch (banCheck.getReason()) {
            case HAS_BAN_PROTECTION:
                player.sendMessage(ChatColor.YELLOW + "Zginales, jednak posiadasz ochrone przed banem i mozesz powrocic do gry bez czekania!");
                return;
        }

        if (banCheck.getReason() != BanAffectionReason.AFFECTED) {
            return;
        }

        Long banSeconds = api.getBanSeconds();
        if (banSeconds > 0) {
            Date date = getDeathBanTimstamp(banSeconds);

            api.banPlayerForDeath(player, hcPeriod, date);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        if (player.isOp()) {
            return;
        }
        
        if(!this.api.gotScheduledTeleport(player)) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if ((from.getBlockX() != to.getBlockX())
                || (from.getBlockY() != to.getBlockY())
                || (from.getBlockZ() != to.getBlockZ())
                || (from.getWorld() != to.getWorld())) {
            api.cancelTeleport(player);        
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) {
            return;
        }

        api.cancelTeleport(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        api.cancelTeleport(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInventoryClick(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        api.cancelTeleport(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        if (event.getCause() != PlayerTeleportEvent.TeleportCause.COMMAND) {
            return;
        }

        if (player.isOp()) {
            return;
        }

        if (api.schedulePlayerTeleport(player, event.getTo())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event == null) {
            return;
        }

        Player player = event.getPlayer();

        if (player == null) {
            return;
        }

        api.clearDisabledWarning(player);

        if (!api.isCurrentlyInvolvedInPvp(player)) {
            return;
        }

        api.clearPvp(player);

        player.setHealth(0.0d);
        api.sendToOtherPlayers(player, ChatColor.GRAY + "Gracz " + ChatColor.RED + player.getName() + ChatColor.GRAY + " zostal ukarany smiercia, poniewaz wyszedl z gry w czasie walki!");
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        if (event == null || event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        // Clear player pvp so he's not killed when kicked while in pvp
        api.clearPvp(player);
    }

    @EventHandler
    public void playerLogin(PlayerLoginEvent event) {
        if (event == null) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        BanAffectionResult banResult = api.checkLightweightBanAffection(player);
        if (banResult.getReason() != BanAffectionReason.AFFECTED) {
            Bukkit.getLogger().log(Level.INFO, "[HC LOGIN] {0} not banned: {1}", new Object[]{player.getName(), banResult.toString()});
            return;
        }

        HardcorePeriod hcPeriod = api.getHardcorePeriod(player);
        banResult = api.checkBanAffection(hcPeriod);
        if (banResult.getReason() != BanAffectionReason.AFFECTED) {
            Bukkit.getLogger().log(Level.INFO, "[HC LOGIN] {0} not banned: {1}", new Object[]{player.getName(), banResult.toString()});
            return;
        }

        long secondsOfBanLeft = api.getBanSeconds(hcPeriod);
        Bukkit.getLogger().log(Level.INFO, "[HC LOGIN] {0} ban seconds: {1}", new Object[]{player.getName(), Long.toString(secondsOfBanLeft)});

        if (secondsOfBanLeft == 0) {
            return;
        }

        event.disallow(
                PlayerLoginEvent.Result.KICK_OTHER,
                getNotAllowedMessage(player, secondsOfBanLeft));
    }

    @EventHandler
    public void playerRespawn(PlayerRespawnEvent event) {
        if (event == null) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        if (api.checkLightweightBanAffection(player).getReason() != BanAffectionReason.AFFECTED) {
            return;
        }

        HardcorePeriod period = api.getHardcorePeriod(player);

        if (api.checkBanAffection(period).getReason() != BanAffectionReason.AFFECTED) {
            return;
        }

        long secondsOfBanLeft = api.getBanSeconds(period);
        if (secondsOfBanLeft == 0) {
            return;
        }

        String message = getNotAllowedMessage(player, secondsOfBanLeft);
        api.kickPlayerAfterDeath(player, message);
    }

    private String getNotAllowedMessage(Player player, long banSeconds) {
        String timeString = getFormattedSeconds(banSeconds);
        String unbanUrl = api.getUnbanUrl(player);

        return ChatColor.YELLOW + "Twoja postac zginela!\n\n"
                + ChatColor.GREEN + "Mozesz powrocic do gry za\n" + ChatColor.YELLOW + timeString
                + ChatColor.GREEN + "\n\nAby odbanowac sie bez czekania, wejdz na strone:\n"
                + ChatColor.YELLOW + unbanUrl;

    }

    private Date getDeathBanTimstamp(long seconds) {
        return new Date(new Date().getTime() + seconds * 1000);
    }

    /* Not used in Hardcore
     private boolean deathCausedByAnotherPlayer(Player player) {
     return player.getKiller() != null && player.getKiller() != player;
     }
     */
    private String getFormattedSeconds(long banSeconds) {
        return LanguageHelper.formatDHMS(banSeconds);
    }
}
