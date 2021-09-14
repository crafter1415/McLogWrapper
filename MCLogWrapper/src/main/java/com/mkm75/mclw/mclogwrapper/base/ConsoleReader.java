package com.mkm75.mclw.mclogwrapper.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.mkm75.mclw.mclogwrapper.extensions.Extensions;

public class ConsoleReader {

	// check CONSOLE input

	InputStream stream;
	ProcessWriter writer;

	public ConsoleReader() {

	}

	public void setStream(InputStream stream) {
		this.stream = stream;
	}

	public void setWriter(ProcessWriter writer) {
		this.writer = writer;
	}

	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			while (!Thread.interrupted()) {
				String str;
				try {
					str = br.readLine();
				} catch (IOException e1) {
					continue;
				}
				if (str == null) break;
				writer.append(str);
				Extensions.extensions.forEach(e->new Thread(()->e.consumeConsoleIn(str)).start());
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
