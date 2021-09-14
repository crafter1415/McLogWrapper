package com.mkm75.mclw.mclogwrapper.base;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProcessWriter {

	// write JAR input

	OutputStream stream;
	List<String> lines;

	public ProcessWriter() {
		lines=new ArrayList<>();
	}

	public void setStream(OutputStream stream) {
		this.stream = stream;
	}

	public void run() {
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(stream));
			while (true) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					break;
				}
				while (0 < lines.size()) {
					bw.write(lines.get(0)+"\n");
					bw.flush();
					lines.remove(0);
				}
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void append(String line) {
		Objects.requireNonNull(line);
		lines.add(line);
	}
	public void append(List<String> lines) {
		Objects.requireNonNull(lines);
		for (String line : lines) append(line);
	}

}
