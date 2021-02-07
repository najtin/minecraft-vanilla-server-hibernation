package com.example.GameServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.function.Consumer;

public class InputBridge {
	
	private final InputStreamReader in;
	private final OutputStreamWriter output;
	private final Thread ioThread;
	private Consumer<String> onNewLine = (s)->{};
	
	public InputBridge(InputStream from, OutputStream to) {
		in = new InputStreamReader(from);
		output = new OutputStreamWriter(to);
		ioThread = new Thread(() -> this.passIO());
	}
	
	public void start() {
		ioThread.start();
	}
	
	public synchronized void setOnNewLine(Consumer<String> onNewLine) {
		this.onNewLine = onNewLine;
	}
	
	public synchronized Consumer<String> getOnNewLine() {
		return this.onNewLine;
	}
	
	public synchronized void ingestLine(String line) {
		try {
			output.write(line);
			output.write(System.lineSeparator());
			output.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void terminate() {
		if(ioThread.isAlive()) ioThread.interrupt();
	}
	
	private void passIO() {
		try {
			String line = "";
			while(true) {
				if(!in.ready()) {Thread.sleep(1000); continue;}
				int i =  in.read();
				if(i==-1) break;
				char c = (char) i;
				if(c!='\n') {line+=c; continue;}
				this.getOnNewLine().accept(line);
				this.ingestLine(line);
				line = "";
			}
		} catch (Throwable e) {}
	}
}
