package com.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class GameServerHandler implements Closeable, AutoCloseable {

	final private BufferedReader input;
	final private BufferedWriter output;
	final private Process process;
	final private ExecutorService service = Executors.newSingleThreadExecutor();
	private final Thread ioThread;
	final private Configuration config;
	
	private int playerCount = 0;
	
	public GameServerHandler(Configuration config) throws IOException {
		this.config = config;
		ProcessBuilder ps = new ProcessBuilder(chunkCommand(config.commandLineStartCommand));
		ps.redirectErrorStream(true);

		process = ps.start();  

		input = new BufferedReader(new InputStreamReader(process.getInputStream()));
		output = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
		


		ioThread = new Thread(() -> {
			System.out.println("Start passing input from stdin to GameServer");
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				String line = "";
				while(true) {
					if(!in.ready()) {Thread.sleep(30); continue;}
					int i =  in.read();
					if(i==-1) break;
					char c = (char) i;
					if(c!='\n') {line+=c; continue;}
					output.write(line);
					output.newLine();
					output.flush();
					line = "";
				}
			} catch (Throwable e) {}
			System.out.println("Stop passing input from stdin to GameServer");
		});
		ioThread.start();
		
		String line;
		while ((line=input.readLine())!=null) {
			System.out.println(line);
			if (config.serverReadyRegex.matcher(line).matches()) break;
		}
	}
	
	public synchronized void waitTillEmptyOrTimeout() throws ExecutionException, InterruptedException {
    	long lastPlayerSeenAt = System.currentTimeMillis();
		while(true) {
			String line;
			try {
				line = readLine();
				System.out.println(line);
			} catch (TimeoutException e) {
				line = "";
			}
        	if(config.playerJoinRegex.matcher(line).matches()) {
        		playerCount++;
        		lastPlayerSeenAt = System.currentTimeMillis();
        	} else if(config.playerLeaveRegex.matcher(line).matches()) {
        		playerCount--;
        		lastPlayerSeenAt = System.currentTimeMillis();
        	} else {
        		if(playerCount == 0 && lastPlayerSeenAt+1000*config.emptyServerTimeout<System.currentTimeMillis())
        			break;
        	}
		}
    }
	
	private String readLine() throws ExecutionException, InterruptedException, TimeoutException {
    	final Future<Object> futureLine = service.submit(() -> input.readLine());
    	String line = "";
    	line = (String) futureLine.get(config.emptyServerTimeout, TimeUnit.SECONDS);
    	if (line == null) throw new ExecutionException(
    			"Program unexpectidly stopped execution.",
    			new RuntimeException("unkown cause"));
    	return line;
	}
	
	@Override
	public void close() throws IOException {
		ioThread.interrupt();
        service.shutdown();
        if(!process.isAlive()) return;
		output.write(config.serverStopCommand);
		output.newLine();
		output.flush();
		try {
			process.waitFor(config.shutdownTimeout, TimeUnit.SECONDS);
			process.destroy();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static String[] chunkCommand(String input) {
		//from https://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes/2120714#2120714
		List<String> result = new ArrayList<String>();
		int start = 0;
		boolean inQuotes = false;
		for (int current = 0; current < input.length(); current++) {
		    if (input.charAt(current) == '\"') inQuotes = !inQuotes; // toggle state
		    else if (input.charAt(current) == ' ' && !inQuotes) {
		        result.add(input.substring(start, current));
		        start = current + 1;
		    }
		}
		result.add(input.substring(start));
		return result.toArray(String[]::new);
	}
}
