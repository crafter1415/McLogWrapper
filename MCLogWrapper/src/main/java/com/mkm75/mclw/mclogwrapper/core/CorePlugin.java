package com.mkm75.mclw.mclogwrapper.core;

import com.mkm75.mclw.mclogwrapper.base.ConsoleReader;
import com.mkm75.mclw.mclogwrapper.base.ProcessReader;
import com.mkm75.mclw.mclogwrapper.base.ProcessWriter;
import com.mkm75.mclw.mclogwrapper.extensions.Extension;
import com.mkm75.mclw.mclogwrapper.extensions.Extensions;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.Initializable;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.LogConsumer;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.LogWrapperExtension;

/**
 * MCLWの根幹を担うプラグインです。
 * <br><br>
 * 各種インスタンスの登録と必要最低限のイベント呼び出しを行います。<br>
 * 該当インスタンスの上書きを行う場合、本プラグインを前提として登録する必要があります。
 */
@LogWrapperExtension(major_version=CorePlugin.MAJOR_VERSION, minor_version=CorePlugin.MINOR_VERSION,
				name=CorePlugin.NAME, requirements_name={}, requirements_version={})
public class CorePlugin implements Initializable, LogConsumer {

	public static final String NAME="MCLWCore@mkm75";
	public static final double MAJOR_VERSION=0;
	public static final double MINOR_VERSION=1.2;

	/**
	 * インスタンスを登録します。
	 */
	public void setInstances() {
		ClassHolder.put(new ConsoleReader());
		ClassHolder.put(new ProcessReader());
		ClassHolder.put(new ProcessWriter());
	}

	public void override() {

	}

	public void preInitialize() {

	}

	public void postInitialize() {

	}

	/**
	 * onDoneイベントの呼び出しを行います。
	 */
	public void consumeLog(String line) {
		System.out.println(line);
		String buffer[] = line.split(" - ", 2);
		if (buffer.length != 2) return;
		if (buffer[1].startsWith("Done")) {
			Extensions.doAllParallel(Extension::onDone);
		}
	}

}
