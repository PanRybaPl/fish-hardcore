package pl.panryba.mc.hardcore.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.panryba.mc.hardcore.BanAffectionReason;
import pl.panryba.mc.hardcore.BanAffectionResult;
import pl.panryba.mc.hardcore.PluginApi;
import pl.panryba.mc.hardcore.entities.HardcorePeriod;
import pl.panryba.mc.pl.LanguageHelper;

public class ProtectionCommand implements CommandExecutor {

    private final PluginApi api;

    public ProtectionCommand(PluginApi api) {
        this.api = api;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if (!(cs instanceof Player)) {
            return false;
        }
        
        if (strings.length > 1) {
            return false;
        }

        Player player = (Player) cs;

        if (strings.length == 1) {
            String subCommand = strings[0].toLowerCase();
            return handleSubcommand(player, subCommand);
        }
        
        BanAffectionResult result = api.checkLightweightBanAffection(player);
        if(result.getReason() == BanAffectionReason.AFFECTED) {
            HardcorePeriod period = api.getHardcorePeriod(player);
            result = api.checkBanAffection(period);
        }
        
        switch(result.getReason()) {
            case AFFECTED:
                player.sendMessage(ChatColor.YELLOW + "Nie posiadasz juz ochrony");
                break;
            case BAN_NOT_ENABLED:
                player.sendMessage(ChatColor.YELLOW + "Ochrona jest stale wlaczona");
                break;
            case HAS_BAN_PROTECTION:
                if(result.getProtectionSeconds() != null) {
                    player.sendMessage(ChatColor.YELLOW + "Posiadasz ochrone przed banem jeszcze przez:\n" +
                            ChatColor.GREEN + LanguageHelper.formatDHMS(result.getProtectionSeconds()));
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Posiadasz ochrone przed banem");
                }
                break;
            case IS_OP:
                player.sendMessage(ChatColor.YELLOW + "Jestes OPem wiec nie jestes banowany za smierc :-)");
                break;
        }        

        return true;
    }

    private boolean handleSubcommand(Player player, String subCommand) {
        return false;
    }

}
