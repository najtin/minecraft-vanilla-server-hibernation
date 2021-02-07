package com.example.Logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Logger{
	private static int lastYear=-1, lastMonth=-1, lastDay=-1;
	private static BufferedWriter writer = instantiate();
			
	public static BufferedWriter instantiate() {
	    try {
			LocalDate currentDate = LocalDate.now();
			lastYear = currentDate.getYear();
			lastMonth = currentDate.getMonthValue();
			lastDay = currentDate.getDayOfMonth();
			
	    	createFolder();
	    	File currentLog = createCurrentLog();
			updateSymlink(currentLog);

		    return new BufferedWriter(new FileWriter(currentLog, true));
	    } catch (IOException e) {
			throw new RuntimeException(e);
		}
	    
	}
	
	private static void createFolder() {
		File logsFolder = Paths.get("./hibernate-logs").toFile();
		if (logsFolder.isDirectory()) return;
		if(logsFolder.exists()) throw new RuntimeException("Can't create logs folder: File already exists");
		logsFolder.mkdir();
	}
	
	private static File createCurrentLog() throws IOException {
		File currentLog = Paths.get("./hibernate-logs/"+lastYear+"-"+lastMonth+"-"+lastDay+".log").toFile();
		if(!currentLog.exists()) currentLog.createNewFile();
		if(currentLog.isDirectory()) throw new RuntimeException(currentLog.getPath()+" is a direcory.");
		return currentLog;
	}
	
	private static void updateSymlink(File currentLog) throws IOException {
		File latestLog = Paths.get("./hibernate-logs/latest.log").toFile();
		if (latestLog.exists()) {
			latestLog.delete();
		}
		Files.createSymbolicLink(latestLog.toPath(), currentLog.toPath().getFileName());
	}
	
	private static String pad(int n) {
		return n<10?"0"+n:String.valueOf(n);
	}
	
	public static synchronized void logLineAndPrint(String line) {
		try {
			LocalDateTime currentDate = LocalDateTime.now();
			if(lastDay!=currentDate.getDayOfMonth()) {
				writer.close();
				writer = instantiate();
			}
			String s = ""
				+ "["
				+ pad(currentDate.getHour())
				+ ":"
				+ pad(currentDate.getMinute())
				+ ":"
				+ pad(currentDate.getSecond())
				+ "] [hibernation]: "
				+ line
				+ System.lineSeparator();
			System.out.print(s);
			writer.append(s);
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
