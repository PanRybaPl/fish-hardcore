/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.panryba.mc.hardcore;

import org.bukkit.entity.Player;

/**
 *
 * @author PanRyba.pl
 */
public class HardcoreManager {
    private static HardcoreManager manager;
    public static HardcoreManager getInstance() {
        return manager;
    }
    
    static void setup(PluginApi api) {
        HardcoreManager.manager = new HardcoreManager(api);
    }
    
    private final PluginApi api;
    private HardcoreManager(PluginApi api) {
        this.api = api;
    }

    public boolean getHasNewbieProtection(Player player) {
        // Newbie protection no longer available
        return false;
    }
}
