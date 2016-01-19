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
public class HardcoreCalculator {
    public static long calculateNewbieProtection(long nowSeconds, long firstPlayedSeconds, long protectionSeconds) {
        if(protectionSeconds == 0) {
            return 0;
        }
        if(firstPlayedSeconds > nowSeconds) {
            firstPlayedSeconds = nowSeconds;
        }
        
        int secondsPlayed = (int)nowSeconds - (int)firstPlayedSeconds;
        int protectionLeft = (int)protectionSeconds - secondsPlayed;
        
        if(protectionLeft < 0)
            protectionLeft = 0;

        return protectionLeft;
    }
}
