/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.panryba.mc.hardcore.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.panryba.mc.hardcore.PluginApi;

/**
 *
 * @author PanRyba.pl
 */
public class WarningCommand implements CommandExecutor {
    private final PluginApi api;

    public WarningCommand(PluginApi api) {
        this.api = api;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if(!(cs instanceof Player)) {
            return false;
        }
        
        Player player = (Player)cs;
        if(this.api.switchWarning(player)) {
            player.sendMessage(ChatColor.GRAY + "Wlaczyles wyswietlanie ostrzezenia w czasie walki");
        } else {
            player.sendMessage(ChatColor.GRAY + "Wylaczyles wyswietlanie ostrzezenia w czasie walki");
        }
        
        return true;
    }
    
}
