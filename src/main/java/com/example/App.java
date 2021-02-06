package com.example;

/**
 * Hello world!
 *
 */
public class App{
    public static void main( String[] args ){
    	System.out.println("Reading config.txt");
    	Configuration config = null;
    	try {
    		config = Configuration.fromFile("config.txt");
    	} catch(Throwable e) {
    		System.err.println(e);
    		System.err.println("Error occuried during parse of the config file \"config.txt\"");
    		System.exit(1);
    	}
    	
    	System.out.println("Starting sleep cycling");
    	try {
    		SleepCycle sleepCycle = new SleepCycle(config);
    		sleepCycle.run();
    	}catch(Throwable e) {
    		System.err.println(e);
    		System.err.println("Fatal error occuried during sleep cycling");
    		System.exit(1);
    	}
    }
    

    
    
}
