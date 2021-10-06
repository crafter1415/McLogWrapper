package com.mkm75.mclw.betterlogging;

import java.util.Arrays;

public class LogEvent {

	String id;
	String args[];

	// @Optional
	String time;   // 12:34:56
	String thread; // Server thread
	String type;   // INFO

	public LogEvent(String format, String args[]) {
		this.id=format;
		this.args=Arrays.copyOf(args, args.length);
	}

	public LogEvent(String time, String thread, String type, String format, String args[]) {
		this.id=format;
		this.args=Arrays.copyOf(args, args.length);
		this.time=time;
		this.thread=thread;
		this.type=type;
	}

	public String[] getArgs() {
		return Arrays.copyOf(args, args.length);
	}
	public String getFormat() {
		return id;
	}
	public String getArg(int index) {
		return args[index];
	}

	public String toString() {
		Object buffer[] = new Object[args.length];
		for (int i=0;i<args.length;i++) {
			buffer[i]=Langs.Localize(args[i]);
		}
		return String.format("["+time+"] ["+thread+"/"+type+"]: "+Langs.Localize(id), buffer);
	}
}
