package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;


class Configuration{
	
	private static enum ConfigParts{
		commandLineStartCommand(s->s),
		serverStopCommand(s->s),
		address(address -> {
			try {
				return InetAddress.getByName(address);
			} catch (UnknownHostException e) {
				throw new RuntimeException(e);
			}
		}),
		port(port -> Integer.parseInt(port)),
		emptyServerTimeout(timeout -> Integer.parseInt(timeout)),
		shutdownTimeout(timeout -> Integer.parseInt(timeout)),
		serverReadyRegex(string -> Pattern.compile(string)),
		playerJoinRegex(string -> Pattern.compile(string)),
		playerLeaveRegex(string -> Pattern.compile(string));
		
		
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
	public final int emptyServerTimeout;
	public final int shutdownTimeout;
	public final Pattern serverReadyRegex;
	public final Pattern playerJoinRegex;
	public final Pattern playerLeaveRegex;
	
	private Configuration(Map<ConfigParts, Object> map) {
		if(map.keySet().containsAll(Arrays.asList(ConfigParts.values())))
			throw new RuntimeException("Configuration does not contain all settings.");
		commandLineStartCommand = (String) map.get(ConfigParts.commandLineStartCommand);
		serverStopCommand = (String) map.get(ConfigParts.serverStopCommand);
		address = (InetAddress) map.get(ConfigParts.address);
		port = (int) map.get(ConfigParts.port);
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
}
