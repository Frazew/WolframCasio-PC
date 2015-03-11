package net.frazew.wolframcasio;

import java.io.IOException;

import jssc.SerialPortException;

public class Main {

	
    public static void main(String[] args) {
        	WolframCasio wol = new WolframCasio("COM3", args[0]);
            try {
            	wol.connect();
            	wol.loop();
    			wol.close();
    		} catch (SerialPortException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		} finally {
    			try {
    				wol.close();
    			} catch (SerialPortException e) {
    				e.printStackTrace();
    			}
    		}
        System.exit(0);
    }
}
