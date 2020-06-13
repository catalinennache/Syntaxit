/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syntaxit;

import java.awt.AWTException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author Enache
 */
public class Syntaxit {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException, AWTException {
     
     
      
      //prs.scanForDevs().forEach(Syntaxit::present);
      Emulator emu = new Emulator();
      emu.init();
      emu.stats();
      Scanner sc = new Scanner(System.in);
      System.out.println("System ready -- pres any key to start");
     while( !sc.next().equals("stop") && ! emu.finishedJob()){  
        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        long timestamp = System.currentTimeMillis() + 5 * 1000;
        int i = 0;
        while (System.currentTimeMillis() < timestamp) {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            for (int j = 0; j < i * 2; j = j + 1) {
                System.out.print("=");
            }
            for (int j = 0; j < 10 - i * 2; j++) {
              //  System.out.print("-");
            }
            i = i + 2;
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
              
      }
        emu.emulateDeviceConfig();
       // emu.write();
     }
      
      
      
    }
    
    
    
}
