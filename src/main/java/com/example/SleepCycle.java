package com.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

public class SleepCycle {
	private final Configuration config;
	public SleepCycle(Configuration config) {
		this.config = config;
	}
	
	public void run() throws IOException, InterruptedException {
		while(true) {
			System.out.println("Sleeping");
			waitForPoke();
			System.out.println("Starting");
			GameServerHandler gameServer = new GameServerHandler(config);
			try{
				System.out.println("Wait till server is empty");
				gameServer.waitTillEmptyOrTimeout();
			} catch (ExecutionException e) {
				System.out.println("Game server was already down");
			} finally {
				System.out.println("Putting server to sleep");
				gameServer.close();
			}
		}
	}
	
		
    private void waitForPoke() throws IOException {
    	ServerSocket server = new ServerSocket(config.port, 1, config.address);
    	Socket client = server.accept();
    	client.close();
    	server.close();
    }
}
