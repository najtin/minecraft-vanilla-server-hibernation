package com.example.Main;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.sun.net.httpserver.*;

import com.example.Logging.Logger;
import com.example.GameServer.GameServerHandler;

public class SleepCycle {
	private final Configuration config;
	private final ServerStatus serverStatus;

    @SuppressWarnings("restriction")
	public SleepCycle(Configuration config) throws IOException {
		this.config = config;
		this.serverStatus = new ServerStatus();
		
		ObjectWriter ow = new ObjectMapper().writer();
		String response = ow.writeValueAsString(this.serverStatus);

		InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 4444);
		HttpServer server = HttpServer.create(socketAddress, 10);
		server.createContext("/", new MonitorResponseHandler(serverStatus));
		server.start();
	}
	
	public void run() throws IOException, InterruptedException {
		while(true) {
			this.serverStatus.setState(ServerStatus.States.offline);
			Logger.logLineAndPrint("Sleeping");
			waitForPoke();
			Logger.logLineAndPrint("Starting");
			GameServerHandler gameServer = new GameServerHandler(config, serverStatus);
			try{
				Logger.logLineAndPrint("Wait till server is empty");
				gameServer.waitTillEmptyOrTimeout();
			} catch (ExecutionException e) {
				Logger.logLineAndPrint("Game server was already down");
			} finally {
				Logger.logLineAndPrint("Putting server to sleep");
				gameServer.close();
			}
		}
	}

	
	@SuppressWarnings("restriction")
	private static class MonitorResponseHandler implements HttpHandler {
		private final ServerStatus serverStatus;
		
		public MonitorResponseHandler(ServerStatus serverStatus) {
			this.serverStatus = serverStatus;
		}

		@SuppressWarnings("restriction")
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			ObjectWriter ow = new ObjectMapper().writer();
			String response = ow.writeValueAsString(this.serverStatus);
			exchange.getResponseHeaders().set("Content-Type", String.format("application/json; charset=%s", StandardCharsets.UTF_8));
			exchange.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes());
			os.flush();
			os.close();
		}
		
	}
	
		
    private void waitForPoke() throws IOException {
    	ServerSocket server = new ServerSocket(config.port, 1, config.address);
    	Socket client = server.accept();
    	
    	byte[] bytes = ("{\"text\":\""+"You just poked me! Server will be up soon."+"\"}").getBytes();
    	OutputStream out = client.getOutputStream();
    	// two bytes length of entire stream
    	out.write((byte) (bytes.length%128 + 128 + 3));
    	out.write((byte) (bytes.length/128));
    	// one 0
    	out.write(0);
    	// two bytes length of message
    	out.write((byte) (bytes.length%128 + 128));
    	out.write((byte) (bytes.length/128));
    	out.write(bytes);
    	out.flush();
    	
    	client.close();
    	server.close();
    }
}
