/*
 * To change this template, choose Tools | Templates
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
public class PvpCommand implements CommandExecutor {

    private final PluginApi api;
    
    public PvpCommand(PluginApi api) {
        this.api = api;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if(!(cs instanceof Player))
            return false;
        
        Player player = (Player)cs;
        if(api.isCurrentlyInvolvedInPvp(player)) {
            player.sendMessage(ChatColor.YELLOW + "Jestes w czasie walki z innym graczem. Jesli wyjdziesz teraz z gry, Twoja postac zostanie ukarana smiercia!");
        } else {
            player.sendMessage(ChatColor.GREEN + "Nie walczysz w tej chwili z innym graczem.");
        }
        
        return true;
    }
    
}
