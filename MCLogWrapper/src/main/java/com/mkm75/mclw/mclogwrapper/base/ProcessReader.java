package com.mkm75.mclw.mclogwrapper.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.mkm75.mclw.mclogwrapper.extensions.Extensions;

public class ProcessReader {

	// check JAR output

	InputStream stream;

	public ProcessReader() {

	}

	public void setStream(InputStream stream) {
		this.stream = stream;
	}

	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			String str;
			while ((str = br.readLine()) != null) {
				final String buffer = str;
				Extensions.extensions.forEach(e->e.consumeLog(buffer));
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
