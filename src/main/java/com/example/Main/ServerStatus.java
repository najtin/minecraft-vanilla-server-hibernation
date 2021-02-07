package com.example.Main;
public class ServerStatus{
	
	public static enum States{
		offline,
		online,
		starting,
		unkown
	}
	private States state = States.unkown;
	private int playerCount = 0;
	private long lastChange = System.currentTimeMillis();
	
	public ServerStatus() {
		
	}
	
	public synchronized void setState(States state) {
		lastChange = System.currentTimeMillis();
		this.state = state; 
	}
	
	public synchronized void incrementPlayerCount() {
		lastChange = System.currentTimeMillis();
		playerCount++;
	}
	
	public synchronized void dencrementPlayerCount() {
		lastChange = System.currentTimeMillis();
		playerCount--;
	}
	
	public synchronized int getPlayerCount() {
		return playerCount;
	}
	
	public synchronized long getLastChange() {
		return lastChange;
	}
	
	public synchronized States getState() {
		return state; 
	}
}