package com.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestConfigParser {
	final String exampleConfig = 
			"commandLineStartCommand = java -jar server.jar\n" + 
			"serverStopCommand = stop\n" + 
			"address = 127.0.1.1\n" + 
			"port = 25565\n" + 
			"emptyServerTimeout = 10\n" + 
			"shutdownTimeout = 10\n" + 
			"serverReadyRegex = .*Done.*\n" + 
			"playerJoinRegex = .*joined.*\n" + 
			"playerLeaveRegex = .*disconnected.*";
	final String commandLineStartCommand = "java -jar server.jar";
	final String serverStopCommand = "stop";
	final InetAddress address;
	final int port = 25565;
	final int emptyServerTimeout = 10;
	final Pattern serverReadyRegex = Pattern.compile(".*Done.*");
	final Pattern playerJoinRegex = Pattern.compile(".*joined.*");
	final Pattern playerLeaveRegex = Pattern.compile(".*disconnected.*");
	
	public TestConfigParser() throws UnknownHostException {
		address = InetAddress.getByName("127.0.1.1");
	}
	@Test
	public void testFromFile() throws IOException {
		File file = File.createTempFile("config", ".txt");
		file.deleteOnExit();
		FileWriter writer = new FileWriter(file);
		writer.append(exampleConfig);
		writer.flush();
		writer.close();
		Configuration config = Configuration.fromFile(file.getAbsolutePath());
		Assertions.assertEquals(commandLineStartCommand, config.commandLineStartCommand);
		Assertions.assertEquals(serverStopCommand, config.serverStopCommand);
		Assertions.assertEquals(address, config.address);
		Assertions.assertEquals(port, config.port);
		Assertions.assertEquals(emptyServerTimeout, config.emptyServerTimeout);
		Assertions.assertEquals(serverReadyRegex.pattern(), config.serverReadyRegex.pattern());
		Assertions.assertEquals(playerJoinRegex.pattern(), config.playerJoinRegex.pattern());
		Assertions.assertEquals(playerLeaveRegex.pattern(), config.playerLeaveRegex.pattern());
	}
}
