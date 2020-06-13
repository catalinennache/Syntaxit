/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syntaxit;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Enache
 */
public class Emulator {

    private Robot robo;
    private Parser parser;
    private Iterator iterator;
    private ArrayList<Device> discovered_devices;

    public Emulator() throws AWTException {
        robo = new Robot();
        this.parser = new Parser(new File("devices.desc"));

    }

    public void writeTEST() {

        robo.keyPress(KeyEvent.VK_3);
        robo.keyRelease(KeyEvent.VK_3);
    }

    public boolean finishedJob() {

        return !iterator.hasNext();
    }

    void emulateDeviceConfig() throws IOException {
        Device dev = (Device) iterator.next();
        parser.loadConf(dev).forEach(line -> {
            System.out.println("writing line: " + line);
            String[] symbols = line.split("");
            for (String symbol : symbols) {
                writeSymbol(symbol);
            }
            robo.keyPress(KeyEvent.VK_ENTER);
            robo.keyRelease(KeyEvent.VK_ENTER);
            sleep(0.2);
        });

    }

    private void writeSymbol(String symbol) {

        boolean upperCase = Character.isUpperCase(symbol.charAt(0));
        String variableName = "VK_" + (symbol.indexOf(" ") != -1 ? "SPACE" : symbol.toUpperCase());

        Class clazz = KeyEvent.class;
        Field field = null;
        try {
            field = clazz.getField(variableName);
        } catch (NoSuchFieldException ex) {
            // Logger.getLogger(Emulator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            //Logger.getLogger(Emulator.class.getName()).log(Level.SEVERE, null, ex);
        }
        int keyCode = 0;
        try {
            if (field != null) {
                keyCode = field.getInt(null);
            } else {
                keyCode = symbol.charAt(0);
            }
        } catch (IllegalArgumentException ex) {
            //  Logger.getLogger(Emulator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            //    Logger.getLogger(Emulator.class.getName()).log(Level.SEVERE, null, ex);
        }

        robo.delay(10);

        if (upperCase) {
            robo.keyPress(KeyEvent.VK_SHIFT);
        }

        robo.keyPress(keyCode);
        robo.keyRelease(keyCode);

        if (upperCase) {
            robo.keyRelease(KeyEvent.VK_SHIFT);
        }

    }

    void init() throws IOException {
        this.discovered_devices = this.parser.scanForDevs();
        iterator = this.discovered_devices.iterator();
    }

    void stats() {

        System.out.println("Detected Devices: " + this.discovered_devices.size());
       // this.discovered_devices.forEach(Emulator::present);
    }

    public static void present(Device dev) {
        System.out.println(dev.name + " - " + dev.type.toString());
    }

    private void sleep(double seconds) {
        try {
            Thread.sleep((long) (seconds * 1000));
        } catch (Exception e) {
        };
    }
}
