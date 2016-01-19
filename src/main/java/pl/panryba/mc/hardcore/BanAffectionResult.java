/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.panryba.mc.hardcore;

/**
 *
 * @author PanRyba.pl
 */
public class BanAffectionResult {
    private BanAffectionReason reason;
    private Long protectionSeconds;
    
    public BanAffectionResult(BanAffectionReason reason, Long protectionSeconds) {
        this.reason = reason;
        this.protectionSeconds = protectionSeconds;
    }
    
    public BanAffectionResult(BanAffectionReason reason) {
        this(reason, null);
    }
    
    public BanAffectionReason getReason() {
        return this.reason;
    }
    
    public Long getProtectionSeconds() {
        return this.protectionSeconds;
    }
}