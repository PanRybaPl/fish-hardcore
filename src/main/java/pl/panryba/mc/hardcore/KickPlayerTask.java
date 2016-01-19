/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.panryba.mc.hardcore;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author PanRyba.pl
 */
public class KickPlayerTask extends BukkitRunnable {

    private final Player player;
    private final String message;
    
    public KickPlayerTask(final Player player, String message) {
        this.player = player;
        this.message = message;
    }
    
    @Override
    public void run() {
        this.player.kickPlayer(this.message);
    }
    
}
