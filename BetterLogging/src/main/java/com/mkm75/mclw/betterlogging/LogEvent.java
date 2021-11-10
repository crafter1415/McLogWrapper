package com.mkm75.mclw.betterlogging;

import java.util.Arrays;
import java.util.IllegalFormatException;

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
		resolveArgs();
	}

	public LogEvent(String time, String thread, String type, String format, String args[]) {
		this.id=format;
		this.args=Arrays.copyOf(args, args.length);
		this.time=time;
		this.thread=thread;
		this.type=type;
		resolveArgs();
	}

	protected void resolveArgs() {
		for (int i=args.length-1;0<=i;i--) {
			if (Langs.isLocalizable(args[i])) {
				args[i]=Langs.Localize(args[i]);
			}
			if (args[i].contains("%s") | args[i].contains("$s") || args[i].contains("%S") | args[i].contains("$S")) {
				int cnt = count(args[i]);
				Object buf[] = new Object[cnt];
				for (int j=0;j<cnt;j++) {
					try {
						buf[j]=args[i+j+1];
					} catch (IndexOutOfBoundsException e) {
						break;
					}
				}
				try {
					args[i]=String.format(args[i], buf);
				} catch (IllegalFormatException e) {
					// then do nothing
					continue;
				}
				if (cnt <= args.length) {
					String array[] = new String[args.length-cnt];
					System.arraycopy(args, 0, array, 0, i+1);
					System.arraycopy(args, i+cnt+1, array, i+1, args.length-i-cnt-1);
					args=array;
				}
			}
		}
	}

	protected static int count(String value) {
		char chs[] = value.toCharArray();
		int max=0;
		int cur=0;
		boolean percent=false;
		for (char ch : chs) {
			if (percent) {
				if ('0'<ch & ch<='9') max=(max<ch-'0'?ch-'0':max);
				if (ch=='s'|ch=='S'|ch=='d'|ch=='D') cur++;
				percent=false;
			} else {
				if (ch == '%') {
					percent=true;
				}
			}
		}
		return (max>cur)?max:cur;
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

	public String format() {
		return String.format(Langs.Localize(id), (Object[]) args);
	}

	public String toString() {
		return "["+time+"] ["+thread+"/"+type+"]: "+format();
	}
}
