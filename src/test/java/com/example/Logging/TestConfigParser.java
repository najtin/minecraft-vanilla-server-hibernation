package com.example.Logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.example.Main.Configuration;

public class TestConfigParser {
	final String exampleConfig = 
			"commandLineStartCommand = java -jar server.jar\n" + 
			"serverStopCommand = stop\n" + 
			"address = 127.0.0.1\n" + 
			"port = 25565\n" +
			"monitorAddress = 127.0.0.1\n"+
			"monitorPort = 8080\n" +
			"emptyServerTimeout = 200\n" + 
			"shutdownTimeout = 30\n" + 
			"serverReadyRegex = .*Done.*\n" + 
			"playerJoinRegex = .*joined.*\n" + 
			"playerLeaveRegex = .*disconnected.*";
	final String commandLineStartCommand = "java -jar server.jar";
	final String serverStopCommand = "stop";
	final InetAddress address;
	final int port = 25565;
	final InetAddress monitorAddress;
	final int monitorPort = 8080;
	final int emptyServerTimeout = 200;
	final int shutdownTimeout = 30;
	final Pattern serverReadyRegex = Pattern.compile(".*Done.*");
	final Pattern playerJoinRegex = Pattern.compile(".*joined.*");
	final Pattern playerLeaveRegex = Pattern.compile(".*disconnected.*");
	
	public TestConfigParser() throws UnknownHostException {
		address = InetAddress.getByName("127.0.0.1");
		monitorAddress = InetAddress.getByName("127.0.0.1");
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
		Assertions.assertEquals(monitorAddress, config.monitorAddress);
		Assertions.assertEquals(monitorPort, config.monitorPort);
		Assertions.assertEquals(emptyServerTimeout, config.emptyServerTimeout);
		Assertions.assertEquals(shutdownTimeout, config.shutdownTimeout);
		Assertions.assertEquals(serverReadyRegex.pattern(), config.serverReadyRegex.pattern());
		Assertions.assertEquals(playerJoinRegex.pattern(), config.playerJoinRegex.pattern());
		Assertions.assertEquals(playerLeaveRegex.pattern(), config.playerLeaveRegex.pattern());
	}
}
