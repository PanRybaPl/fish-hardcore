/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.panryba.mc.hardcore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import pl.panryba.mc.hardcore.Plugin;
import pl.panryba.mc.hardcore.PluginApi;

/**
 *
 * @author PanRyba.pl
 */
public class HardcoreCommand implements CommandExecutor {
    private final PluginApi api;
    private final Plugin plugin;
    
    public HardcoreCommand(Plugin plugin, PluginApi api) {
        this.plugin = plugin;
        this.api = api;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if(strings.length != 1) {
            return false;
        }
        
        this.plugin.reloadConfig();
        FileConfiguration config = this.plugin.getConfig();
        
        api.loadConfig(config);
        
        cs.sendMessage("Konfiguracja Hardcore zostala przeladowana");
        return true;
    }
}
