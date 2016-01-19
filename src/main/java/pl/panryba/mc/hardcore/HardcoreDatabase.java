package pl.panryba.mc.hardcore;

import pl.panryba.mc.hardcore.entities.HardcorePeriod;

public interface HardcoreDatabase {
    public HardcorePeriod find(String playerName);
    public void save(HardcorePeriod period);
}
