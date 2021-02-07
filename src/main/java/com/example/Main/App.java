package com.example.Main;

import java.io.File;
import java.nio.file.Paths;

import com.example.Logging.Logger;

/**
 * Hello world!
 *
 */
public class App{
    public static void main( String[] args ){
    	String configFileName = "config.txt";
    	Logger.logLineAndPrint("Reading "+configFileName);
    	Configuration config = null;
    	try {
    		File configFile = Paths.get(configFileName).toFile();
    		if(!configFile.exists()) {
    			Logger.logLineAndPrint("No config file found. Trying to create '"+configFileName+"'.");
        		Configuration.writeDefault(configFileName);
        		Logger.logLineAndPrint("Wrote default '"+configFileName+"'. Please customize and restart.");
        		System.exit(1);
    		}
    		config = Configuration.fromFile(configFileName);
    	} 
    	catch(Throwable e) {
    		Logger.logLineAndPrint(e.toString());
    		Logger.logLineAndPrint("Error occuried during parse of the config file '"+configFileName+"'.");
    		System.exit(1);
    	}
    	
    	Logger.logLineAndPrint("Starting sleep cycling");
    	try {
    		SleepCycle sleepCycle = new SleepCycle(config);
    		sleepCycle.run();
    	}catch(Throwable e) {
    		Logger.logLineAndPrint(e.toString());
    		Logger.logLineAndPrint("Fatal error occuried during sleep cycling");
    		System.exit(1);
    	}
    }
    

    
    
}
