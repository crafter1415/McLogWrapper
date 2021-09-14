package com.mkm75.mclw.mclogwrapper.core;

import com.mkm75.mclw.mclogwrapper.base.ConsoleReader;
import com.mkm75.mclw.mclogwrapper.base.ProcessReader;
import com.mkm75.mclw.mclogwrapper.base.ProcessWriter;
import com.mkm75.mclw.mclogwrapper.extensions.Extensions;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.Initializable;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.LogConsumer;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.LogWrapperExtension;

@LogWrapperExtension(major_version=0, minor_version=0, name="McLogWrapperCore", requirements_name={}, requirements_version={})
public class CorePlugin implements Initializable, LogConsumer {

	public void setInstances() {
		ClassHolder.put(new ConsoleReader());
		ClassHolder.put(new ProcessReader());
		ClassHolder.put(new ProcessWriter());
	}

	public void override() {

	}

	public void initialize() {

	}

	public void consumeLog(String line) {
		System.out.println(line);
		String buffer[] = line.split(" - ", 2);
		if (buffer.length != 2) return;
		if (buffer[1].startsWith("Done")) {
			Extensions.extensions.forEach(e->new Thread(e::onDone).start());
		}
	}

}
