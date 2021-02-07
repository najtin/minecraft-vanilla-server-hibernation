package com.example.Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;


public class Configuration{
	
	private static enum ConfigParts{
		commandLineStartCommand(s->s),
		serverStopCommand(s->s),
		address(ConfigParts::parseInetAdress),
		monitorAddress(ConfigParts::parseInetAdress),
		port(Integer::parseInt),
		monitorPort(Integer::parseInt),
		emptyServerTimeout(Integer::parseInt),
		shutdownTimeout(Integer::parseInt),
		serverReadyRegex(Pattern::compile),
		playerJoinRegex(Pattern::compile),
		playerLeaveRegex(Pattern::compile);
		
		private static InetAddress parseInetAdress(String address) {
			try {
				return InetAddress.getByName(address);
			} catch (UnknownHostException e) {
				throw new RuntimeException(e);
			}
		}
		
		
		private Function<String, Object> fun;
		private ConfigParts(Function<String, Object> fun) {
			this.fun = fun;
		}
		
		public Object from(String string) {
			return fun.apply(string);
		}
	}
	
	public final String commandLineStartCommand;
	public final String serverStopCommand;
	public final InetAddress address;
	public final int port;
	public final InetAddress monitorAddress;
	public final int monitorPort;
	public final int emptyServerTimeout;
	public final int shutdownTimeout;
	public final Pattern serverReadyRegex;
	public final Pattern playerJoinRegex;
	public final Pattern playerLeaveRegex;
	
	private Configuration(Map<ConfigParts, Object> map) {
		if(!map.keySet().containsAll(Arrays.asList(ConfigParts.values()))) {
			String errorMessage = "Configuration does not contain all settings. It is missing:";
			List<ConfigParts> missingKeys = new ArrayList<>(Arrays.asList(ConfigParts.values()));
			missingKeys.removeAll(map.keySet());
			errorMessage += missingKeys.toString();
			throw new RuntimeException(errorMessage);
		}
		commandLineStartCommand = (String) map.get(ConfigParts.commandLineStartCommand);
		serverStopCommand = (String) map.get(ConfigParts.serverStopCommand);
		address = (InetAddress) map.get(ConfigParts.address);
		port = (int) map.get(ConfigParts.port);
		monitorAddress = (InetAddress) map.get(ConfigParts.monitorAddress);
		monitorPort = (int) map.get(ConfigParts.monitorPort);
		emptyServerTimeout = (int) map.get(ConfigParts.emptyServerTimeout);
		shutdownTimeout = (int) map.get(ConfigParts.shutdownTimeout);
		serverReadyRegex = (Pattern) map.get(ConfigParts.serverReadyRegex);
		playerJoinRegex = (Pattern) map.get(ConfigParts.playerJoinRegex);
		playerLeaveRegex = (Pattern) map.get(ConfigParts.playerLeaveRegex);
		
	}
	
	public static Configuration fromFile(String path) throws IOException {
		File configFile = new File(path);
		if(!configFile.isFile()) throw new IOException(path+" is not a file.");
		Map<ConfigParts, Object> map = new HashMap<ConfigParts, Object>();
		BufferedReader reader = new BufferedReader(new FileReader(path));
		reader.lines().forEach(line ->{
			int splitAt = line.indexOf("=");
			String name = line.substring(0, splitAt).trim();
			ConfigParts configPart = ConfigParts.valueOf(name);
			String valueAsString = line.substring(splitAt+1).trim();
			map.put(configPart, configPart.from(valueAsString));
		});
		reader.close();
		return new Configuration(map);
	}
	
	public static void writeDefault(String fileName) throws IOException {
		PrintStream printer= new PrintStream(
			     new FileOutputStream(fileName, true)); 
		printer.println("commandLineStartCommand = java -jar server.jar");
		printer.println("serverStopCommand = stop");
		printer.println("address = 192.168.202.40");
		printer.println("port = 25565");
		printer.println("emptyServerTimeout = 240");
		printer.println("shutdownTimeout = 30");
		printer.println("serverReadyRegex = \\[[0-9]{2}:[0-9]{2}:[0-9]{2}\\][^:]*: Done \\([0-9]+\\.[0-9]+s\\)! For help, type \"help\"");
		printer.println("playerJoinRegex = \\[[0-9]{2}:[0-9]{2}:[0-9]{2}\\][^:]*: [A-Za-z0-9]+ joined the game");
		printer.println("playerLeaveRegex = \\[[0-9]{2}:[0-9]{2}:[0-9]{2}\\][^:]*: [A-Za-z0-9]+ left the game");
		printer.println("monitorAddress = 127.0.0.1");
		printer.println("monitorPort = 8080");
		printer.close();
	}
}
