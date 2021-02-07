package com.example.GameServer;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.example.Main.Configuration;
import com.example.Main.ServerStatus;


public class GameServerHandler implements Closeable, AutoCloseable {

	final private Process process;
	private final InputBridge terminalInputBridge;
	private final InputBridge serverInputBridge;
	private final ServerStatus serverStatus;
	private final Thread shutdownHock;
	final private Configuration config;
	
	
	public GameServerHandler(Configuration config, ServerStatus serverStatus) throws IOException {
		this.config = config;
		
		this.serverStatus = serverStatus;
		
		ProcessBuilder ps = new ProcessBuilder(chunkCommand(config.commandLineStartCommand));
		ps.redirectErrorStream(true);

		process = ps.start();  
		this.serverStatus.setState(ServerStatus.States.starting);

		
		terminalInputBridge = new InputBridge(System.in, process.getOutputStream());
		terminalInputBridge.start();
		
		serverInputBridge = new InputBridge(process.getInputStream(), System.out);
		serverInputBridge.setOnNewLine((line) -> {
			if (config.serverReadyRegex.matcher(line).matches()) 
				serverStatus.setState(ServerStatus.States.online);
		});
		serverInputBridge.start();
		
		while(true) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			if(serverStatus.getState() == ServerStatus.States.online) break;
		}
		
		serverInputBridge.setOnNewLine((line) -> {
			if (config.playerJoinRegex.matcher(line).matches()) 
				serverStatus.incrementPlayerCount();
			else if (config.playerLeaveRegex.matcher(line).matches()) 
				serverStatus.dencrementPlayerCount();
		});
		
		shutdownHock = new Thread(()-> this.closeWithoutRemovingHook());
		Runtime.getRuntime().addShutdownHook(shutdownHock);
	}
	
	public void waitTillEmptyOrTimeout() throws ExecutionException {
		while(true) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			//here we might lose an update (race condition) but it shouldn't matter in practice
			//since then the lastChange case will probably hit in this case
			if(!process.isAlive()) throw new ExecutionException(new RuntimeException("process died unexpectidly"));
			if(serverStatus.getPlayerCount() != 0) continue;
			if(System.currentTimeMillis()<serverStatus.getLastChange()+1000*config.emptyServerTimeout) continue;
			//if a player joins after this check, well, thats simply bad luck
			
			break;
		}
    }
	
	@Override
	public void close() throws IOException {
		closeWithoutRemovingHook();
		Runtime.getRuntime().removeShutdownHook(shutdownHock);
	}
	
	private void closeWithoutRemovingHook() {
        if(!process.isAlive()) return;
        terminalInputBridge.ingestLine(config.serverStopCommand);
		
		serverInputBridge.terminate();
		terminalInputBridge.terminate();
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
