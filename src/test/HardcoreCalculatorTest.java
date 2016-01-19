/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.junit.Test;
import static org.junit.Assert.*;
import pl.panryba.mc.hardcore.HardcoreCalculator;

/**
 *
 * @author PanRyba.pl
 */
public class HardcoreCalculatorTest {
    
    private static long calculate(long now, long first, long protection) {
        return HardcoreCalculator.calculateNewbieProtection(now, first, protection);
    }
    
    @Test
    public void testReturnsZeroIfProtectionIsZero() {
        long TenSeconds = 10;
        long OneMinute = 60;
        
        assertEquals(0, calculate(TenSeconds, OneMinute, 0));
        assertEquals(0, calculate(TenSeconds, 0, 0));
        assertEquals(0, calculate(0, 0, 0));
        assertEquals(0, calculate(0, OneMinute, 0));
    }
    
    @Test
    public void testCalculation() {
        long OneMinute = 60;
        long TwoMinutes = 2 * OneMinute;
        long ThreeMinutes = 3 * OneMinute;
        
        assertEquals(TwoMinutes, calculate(OneMinute, OneMinute, TwoMinutes));
        assertEquals(TwoMinutes, calculate(0, 0, TwoMinutes));
        assertEquals(TwoMinutes, calculate(TwoMinutes, TwoMinutes, TwoMinutes));
        
        assertEquals(OneMinute, calculate(TwoMinutes, OneMinute, TwoMinutes));
        assertEquals(OneMinute, calculate(OneMinute, 0, TwoMinutes));
        
        assertEquals(0, calculate(TwoMinutes, 0, TwoMinutes));
        assertEquals(0, calculate(ThreeMinutes, 0, TwoMinutes));
    }
}
