package pl.panryba.mc.hardcore;

import com.avaje.ebean.EbeanServer;
import pl.panryba.mc.hardcore.entities.HardcorePeriod;

public class EbeanHardcoreDatabase implements HardcoreDatabase {

    private final EbeanServer database;
    
    public EbeanHardcoreDatabase(EbeanServer database) {
        this.database = database;
    }
    
    @Override
    public HardcorePeriod find(String playerName) {
        return this.database.find(HardcorePeriod.class, playerName);
    }

    @Override
    public void save(HardcorePeriod period) {
        this.database.save(period);
    }
    
}
