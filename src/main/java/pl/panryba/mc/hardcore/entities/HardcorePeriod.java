package pl.panryba.mc.hardcore.entities;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import pl.panryba.mc.hardcore.HardcoreDatabase;

/**
 *
 * @author PanRyba.pl
 */

@Entity
@Table(name = "hardcore_periods")
public class HardcorePeriod {

    public static HardcorePeriod getForPlayer(HardcoreDatabase database, String playerName) {
        return database.find(playerName);
    }

    public static HardcorePeriod setForPlayer(HardcoreDatabase database, HardcorePeriod period, String name, long until) {
        if(period == null) {
            period = new HardcorePeriod();
            period.setPlayerName(name);
        }
        
        period.setValidUntil(until);
        database.save(period);
        
        return period;
    }
    
    @Id
    @Column(name = "player_name", unique = true)
    private String playerName;
    
    @Column(name = "valid_until")
    private long validUntil;
    
    @Column(name = "protected_until")
    private Long protectedUntil;
    
    public void setPlayerName(String value) {
        this.playerName = value;
    }
    
    public String getPlayerName() {
        return this.playerName;
    }
    
    public void setValidUntil(long value) {
        this.validUntil = value;
    }
    
    public long getValidUntil() {
        return this.validUntil;
    }
    
    public void setProtectedUntil(Long value) {
        this.protectedUntil = value;
    }
    
    public Long getProtectedUntil() {
        return this.protectedUntil;
    }

    public long getProtectionSeconds() {
        if(this.protectedUntil == null) {
            return 0;
        }
        
        long now = (new Date()).getTime();
        if(now > this.protectedUntil) {
            return 0;
        }
        
        return (this.protectedUntil - now) / 1000;
    }
}
