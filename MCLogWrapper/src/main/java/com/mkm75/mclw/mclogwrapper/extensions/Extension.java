package com.mkm75.mclw.mclogwrapper.extensions;

import java.io.File;
import java.util.function.Supplier;

import com.google.gson.JsonObject;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.ConsoleInputConsumer;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.Initializable;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.LogConsumer;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.LogWrapperExtension;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.ServerStateEvents;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.UseConfig;

public class Extension implements LogConsumer, ConsoleInputConsumer, ServerStateEvents, Comparable<Extension>, UseConfig, Initializable {
	// ?implements InputConsumer, OutputConsumer, ServerStateEvents

	boolean isConsumeConsoleIn;
	boolean isConsumeLog;
	boolean hasStateEvents;
	boolean useConfig;
	boolean isInitializable;

	String id;
	String dependencies[];
	double dependencies_version[];
	Extension dep_extension[];
	Extension use_extension[];
	double major_version;
	String version_site;
	boolean is_optional;
	int currentState;

	Object instance;
	Class<?> baseClass;

	File source;

	boolean isConfigLoaded;
	Config config;
	boolean isOverrided;
	boolean isPreInitialized;
	boolean isPostInitialized;

	public Extension(File source, Class<?> base) {
		this.source=source;
		LogWrapperExtension lwe = base.getAnnotation(LogWrapperExtension.class);
		if (lwe == null) throw new IllegalArgumentException(base.getCanonicalName());
		baseClass=base;
		isOverrided=true;
		for (Class<?> clazz : baseClass.getInterfaces()) {
			if (clazz.equals(LogConsumer.class)) {
				isConsumeLog=true;
			}
			if (clazz.equals(ServerStateEvents.class)) {
				hasStateEvents=true;
			}
			if (clazz.equals(Initializable.class)) {
				isInitializable=true;
				isOverrided=false;
				isPreInitialized=false;
				isPostInitialized=false;
			}
			if (clazz.equals(UseConfig.class)) {
				isConfigLoaded=false;
				useConfig=true;
			}
			if (clazz.equals(ConsoleInputConsumer.class)) {
				isConsumeConsoleIn=true;
			}
		}
		id = lwe.name();
		dependencies = lwe.requirements_name();
		dep_extension=new Extension[dependencies.length];
		major_version=lwe.major_version();
		dependencies_version=lwe.requirements_version();
		version_site=lwe.version_info_site();
		is_optional=lwe.is_optional();
		currentState=0;
		if (dependencies.length != dependencies_version.length) throw new IllegalArgumentException("@interface exception");
	}

	public Extension(String id) {
		// VoidExtension
		this.id=id;
		currentState=-1;
	}

	boolean load() {
		if (currentState == 2) return true;
		if (currentState == -1) return false;
		currentState=1;
		for (Extension extension : dep_extension) {
			if (extension.currentState == 1) {
				System.out.println("[ExtensionLoader] 循環参照が確認されたためプラグイン "+id+" は読み込まれませんでした");
				currentState=-1;
				return false;
			}
			if (!extension.load()) {
				if (!is_optional) System.out.println("[ExtensionLoader] 前提プラグイン "+extension.id+" が読み込みに失敗したため "+id+" は読み込まれませんでした");
				return false;
			}
		}
		try {
			instance=baseClass.newInstance();
		} catch (Exception e) {
			System.err.println("[ExtensionLoader] プラグイン "+id+" の読み込み中にエラーが発生しました。下記のスタックトレースを確認してください:");
			e.printStackTrace();
			currentState=-1;
			return false;
		}
		currentState=2;
		return true;
	}

	protected void checkState() {
		if (!validate()) throw new IllegalStateException();
	}

	public boolean validate() {
		if (currentState != 2) return false;
		for (Extension extension : dep_extension) {
			if (!extension.validate()) return false;
		}
		return true;
	}

	protected void checkType() {
		if (!useConfig) throw new IllegalStateException();
	}

	protected void disable() {
		currentState=-1;
		for (Extension extension : use_extension) {
			extension.disable();
		}
	}

	protected void accept(Runnable runnable) {
		try {
			runnable.run();
		} catch (RuntimeException e) {
			disable();
			System.err.println("[ExtensionProcessor] 関数 reserveConfigs() の処理中にハンドルされていない例外が投げられました");
			System.err.println("[ExtensionProcessor] プラグイン "+id+" とそれに依存するプラグインは無効化されました");
			System.err.println("[ExtensionProcessor] スタックトレース (開発者に投げると原因究明につながる可能性があります): ");
			e.printStackTrace();
		}
	}
	protected <T> T accept(Supplier<T> consumer) {
		try {
			return consumer.get();
		} catch (RuntimeException e) {
			disable();
			System.err.println("[ExtensionProcessor] 関数 reserveConfigs() の処理中にハンドルされていない例外が投げられました");
			System.err.println("[ExtensionProcessor] プラグイン "+id+" とそれに依存するプラグインは無効化されました");
			System.err.println("[ExtensionProcessor] スタックトレース (開発者に投げると原因究明につながる可能性があります): ");
			e.printStackTrace();
			return null;
		}
	}

	public Config reserveConfigs() {
		checkState();
		checkType();
		if (isConfigLoaded) throw new IllegalStateException("Config already loaded");
		return accept(()->((UseConfig)instance).reserveConfigs());
	}
	public void onConfigLoaded() {
		checkState();
		accept(()->((UseConfig)instance).onConfigLoaded());
	}
	public void loadConfig(JsonObject obj) {
		checkState();
		if (useConfig) {
			accept(()->config.load(obj));;
		}
	}
	public JsonObject saveConfig() {
		checkState();
		if (!useConfig) return null;
		return config.save();
	}

	public void consumeLog(String line) {
		checkState();
		if (isConsumeLog) {
			accept(()->((LogConsumer)instance).consumeLog(line));
		}
	}

	public void consumeConsoleIn(String line) {
		checkState();
		if (isConsumeConsoleIn) {
			accept(()->((ConsoleInputConsumer)instance).consumeConsoleIn(line));
		}
	}

	public void onDone() {
		checkState();
		if (hasStateEvents) {
			accept(()->((ServerStateEvents)instance).onDone());
		}
	}
	public void onStop() {
		checkState();
		if (hasStateEvents) {
			accept(()->((ServerStateEvents)instance).onStop());
		}
	}
	public void setInstances() {
		checkState();
		if (isInitializable) {
			accept(()->((Initializable)instance).setInstances());
		}
	}
	public void override() {
		checkState();
		// 循環参照が存在しないことが保証されている
		// なおかつ依存する拡張機能が全て読み込み済みであることも保証されている
		if (isOverrided) return;
		for (Extension extension : dep_extension) {
			extension.override();
		}
		if (isInitializable) {
			accept(()->((Initializable)instance).override());
		}
		isOverrided=true;
	}
	public void preInitialize() {
		checkState();
		// 循環参照が存在しないことが保証されている
		// なおかつ依存する拡張機能が全て読み込み済みであることも保証されている
		if (isPreInitialized) return;
		for (Extension extension : dep_extension) {
			extension.preInitialize();
		}
		if (isInitializable) {
			accept(()->((Initializable)instance).preInitialize());
		}
		isPreInitialized=true;
	}
	public void postInitialize() {
		checkState();
		// 循環参照が存在しないことが保証されている
		// なおかつ依存する拡張機能が全て読み込み済みであることも保証されている
		if (isPostInitialized) return;
		for (Extension extension : use_extension) {
			extension.postInitialize();
		}
		if (isInitializable) {
			accept(()->((Initializable)instance).postInitialize());
		}
		isPostInitialized=true;
	}

	public int compareTo(Extension o) {
		return id.compareTo(o.id);
	}

	public Object getInstance() {
		return instance;
	}

}
