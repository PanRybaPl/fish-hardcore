/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.panryba.mc.hardcore;

import com.avaje.ebean.EbeanServer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;
import pl.panryba.mc.db.FishDbPlugin;
import pl.panryba.mc.hardcore.commands.HardcoreCommand;
import pl.panryba.mc.hardcore.commands.ProtectionCommand;
import pl.panryba.mc.hardcore.commands.PvpCommand;
import pl.panryba.mc.hardcore.commands.WarningCommand;
import pl.panryba.mc.hardcore.entities.HardcorePeriod;

import java.util.List;

/**
 *
 * @author PanRyba.pl
 */
public class Plugin extends FishDbPlugin {

    private PluginApi api;
    private BukkitTask updatePvpTask;
    
    private class SetPvpBars implements Runnable {

        @Override
        public void run() {
            api.updatePvpBars();
        }
    }
    
    @Override
    public void onEnable() {
        FileConfiguration config = getConfig();
        EbeanServer database = getCustomDatabase();
        HardcoreDatabase hardcoreDb = new EbeanHardcoreDatabase(database);

        this.api = new PluginApi(this, config, hardcoreDb);
        PluginApi.setup(this.api);
        HardcoreManager.setup(api);
        
        getCommand("pvp").setExecutor(new PvpCommand(api));
        getCommand("ochrona").setExecutor(new ProtectionCommand(api));
        getCommand("hardcore").setExecutor(new HardcoreCommand(this, api));
        getCommand("ostrzezenie").setExecutor(new WarningCommand(api));
        
        HardcoreListener listener = new HardcoreListener(api);
        getServer().getPluginManager().registerEvents(listener, this);
        
        updatePvpTask = getServer().getScheduler().runTaskTimer(this, new SetPvpBars(), 20 * 10, 20 * 5);
    }

    @Override
    public void onDisable() {
        if(updatePvpTask != null) {
            updatePvpTask.cancel();
            updatePvpTask = null;
        }
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = super.getDatabaseClasses();
        list.add(HardcorePeriod.class);
        
        return list;
    }
}
