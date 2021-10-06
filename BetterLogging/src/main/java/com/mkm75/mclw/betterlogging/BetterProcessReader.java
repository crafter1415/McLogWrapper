package com.mkm75.mclw.betterlogging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import com.mkm75.mclw.mclogwrapper.base.ProcessReader;
import com.mkm75.mclw.mclogwrapper.core.EventHooks;
import com.mkm75.mclw.mclogwrapper.extensions.Extensions;

public class BetterProcessReader extends ProcessReader {
	public void run() {
		Objects.requireNonNull(stream, "Stream is null");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			String str;
			while ((str = br.readLine()) != null) {
				check:
				{
					String buf0[] = str.split("]: ", 2);
					if (buf0.length == 1) break check;
					String buf1[] = buf0[1].split(BetterLogging.REGEX);
					if (!Langs.isLocalizable(buf1[0])) break check;
					String buf2[] = new String[buf1.length-1];
					System.arraycopy(buf1, 1, buf2, 0, buf2.length);
					String buf3[] = buf0[0].split("\\] \\[", 2);
					if (buf3.length == 1) break check;
					String buf4[] = buf3[1].split("/", 2);
					if (buf4.length == 1) break check;
					LogEvent event = new LogEvent(buf3[0].substring(1), buf4[0], buf4[1], buf1[0], buf2);
					EventHooks.call(LogEvent.class, event);
					String fin=event.toString();
					Extensions.extensions.values().parallelStream().forEach(e->e.consumeLog(fin));
					continue;
				}
				String fin=str;
				Extensions.extensions.values().parallelStream().forEach(e->e.consumeLog(fin));
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
