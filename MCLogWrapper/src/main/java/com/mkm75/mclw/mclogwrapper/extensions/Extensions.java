package com.mkm75.mclw.mclogwrapper.extensions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mkm75.mclw.mclogwrapper.core.CorePlugin;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.LogWrapperExtension;

public class Extensions {

	public static final class Settings {
		public static File ConfigFile = new File("./config.json");
		public static File PluginFolder = new File("./plugins/");
	}

	// 汎用クラス コピペ用
	public static class Pair<K, V> {
		K key;
		V value;
		public Pair(K key, V value) {
			this.key=key;
			this.value=value;
		}
		public K getKey() {
			return key;
		}
		public void setKey(K key) {
			this.key = key;
		}
		public V getValue() {
			return value;
		}
		public void setValue(V value) {
			this.value = value;
		}

		public boolean equals(Object obj) {
			if (!(obj instanceof Pair<?, ?>)) return false;
			if (!instanceOf(obj)) return false;
			// checked
			@SuppressWarnings("unchecked")
			Pair<K, V> obj2 = (Pair<K, V>) obj;
			if (!key.equals(obj2.key)) return false;
			return value.equals(obj2.value);
		}
		private boolean instanceOf(Object obj) {
			return obj.getClass() == this.getClass();
		}
	}

	// セキュリティー上の観点からこの辺は後々変えるかもしれない。
	private static List<Extension> extensions_dummy = new ArrayList<>();
	public static Map<String, Extension> extensions = new HashMap<>();

	public static void load() {
		System.out.println("[ExtensionLoader] プラグイン読み込み開始");
		loadSystemExtension();
		if (!Settings.PluginFolder.exists()) Settings.PluginFolder.mkdir();
		// 再帰的探索
		loadDir(Settings.PluginFolder);
		// 重複・依存の確認
		Map<String, Extension> extensions = new HashMap<>();
		Map<String, List<Extension>> dupes = new HashMap<>();
		for (Extension extension : extensions_dummy) {
			if (extensions.containsKey(extension.id)) {
				if (dupes.containsKey(extension.id)) {
					dupes.put(extension.id, new ArrayList<>());
					dupes.get(extension.id).add(extensions.get(extension.id));
					extensions.get(extension.id).currentState=-1;
				}
				dupes.get(extension.id).add(extension);
				extension.currentState=-1;
			}
			extensions.put(extension.id, extension);
		}
		Map<String, List<String>> missings = new HashMap<>();
		Map<String, List<Pair<String, Double>>> req_versions = new HashMap<>();
		for (Extension extension : extensions_dummy) {
			for (int i=0;i<extension.dependencies.length;i++) {
				String s = extension.dependencies[i];
				if (!extensions.containsKey(s)) {
					extension.currentState=-1;
					if (!extension.is_optional) {
						if (!missings.containsKey(s)) {
							missings.put(s, new ArrayList<>());
						}
						missings.get(s).add(extension.id);
					}
				} else {
					if (extension.dependencies_version[i] != extensions.get(s).major_version) {
						if (!req_versions.containsKey(extension.id)) {
							req_versions.put(extension.id, new ArrayList<>());
						}
						req_versions.get(extension.id).add(new Pair<>(extension.id, extension.dependencies_version[i]));
					}
					extension.dep_extension[i]=extensions.get(s);
				}
			}
		}
		for (int i=0,size=extensions_dummy.size();i<size;i++) {
			Extension extension = extensions_dummy.get(i);
			if (extension.currentState != -1) {
				Extensions.extensions.put(extension.id, extension);
			}
		}
		{
			Map<Extension, List<Extension>> check_dep = new HashMap<>();
			for (Extension extension : Extensions.extensions.values()) {
				check_dep.put(extension, new ArrayList<>());
			}
			for (Extension extension : Extensions.extensions.values()) {
				for (Extension extension2 : extension.dep_extension) {
					check_dep.get(extension2).add(extension);
				}
			}
			for (Entry<Extension, List<Extension>> set : check_dep.entrySet()) {
				set.getKey().use_extension = set.getValue().toArray(new Extension[set.getValue().size()]);
			}
		}
		missings.forEach((s, l)->{
			System.out.println("[ExtensionLoader] プラグイン "+s+" が存在しないため以下のプラグインが読み込まれませんでした: ");
			l.forEach(t->{
				System.out.println("[ExtensionLoader]  - "+t);
			});
		});
		req_versions.forEach((s, l)->{
			System.out.println("[ExtensionLoader] プラグイン "+s+" には以下のバージョンのプラグインが必要です: ");
			l.forEach(t->{
				System.out.println("[ExtensionLoader]    "+t.key+" - "+t.value);
			});
		});
		// インスタンスの作成
		// これ自体は何も行わないことを推奨している
		for (Extension extension : Extensions.extensions.values()) {
			extension.load();
		}
		// コンフィグの割り当て
		List<Extension> configuring_extensions = Extensions.extensions.values().stream().filter(e->e.useConfig).collect(Collectors.toList());
		JsonObject jo;
		{
			if (Settings.ConfigFile.exists()) {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Settings.ConfigFile), StandardCharsets.UTF_8));
					Gson gson = new Gson();
					jo = gson.fromJson(br, JsonObject.class);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
					return; // 本来到達しえない
				} catch (JsonParseException e) {
					System.out.println("[ConfigLoader] 既存コンフィグの読み込みに失敗しました");
					jo = new JsonObject();
				}
			} else {
				jo = new JsonObject();
			}
		}
		for (Extension extension : configuring_extensions) {
			Config config = extension.reserveConfigs();
			JsonObject jo2 = config.save();
			if (!jo.has(extension.id)) {
				jo.add(extension.id, jo2);
			}
			JsonObject ext_base = jo.get(extension.id).getAsJsonObject();
			for (String key : jo2.keySet()) {
				if (!ext_base.has(key)) {
					ext_base.add(key, jo2.get(key));
				}
			}
			config.load(ext_base);
			extension.config=config;
		}
		if (!Settings.ConfigFile.exists()) {
			save_configs();
		}
		doAll(e->{
			if (e.useConfig) e.onConfigLoaded();
		});
		doAll(Extension::setInstances);
		doAll(Extension::override);
		doAll(Extension::preInitialize);
		doAll(Extension::postInitialize);
		System.out.println("[ExtensionLoader] プラグイン読み込み終了");
	}

	public static void save_configs() {
		JsonObject jo = new JsonObject();
		for (Extension extension : extensions.values()) {
			if (!extension.useConfig) continue;
			jo.add(extension.id, extension.config.save());
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(Settings.ConfigFile));
			gson.toJson(jo, bw);
			bw.flush();
		} catch (IOException e) {
			System.err.println("[ConfigLoader] コンフィグの保存に失敗しました");
			e.printStackTrace();
			return;
		}
		System.out.println("[ConfigLoader] コンフィグが保存されました");
	}

	private static void loadDir(File dir) {
		File list[] = dir.listFiles();
		for (File file : list) {
			if (file.isDirectory()) {
				loadDir(dir);
			} else {
				if (!file.toString().substring(file.toString().lastIndexOf('.')).equals(".jar")) continue;
				try {
					JarFile jf = new JarFile(file);
					URL buffer[] = new URL[1];
					buffer[0]=file.toURI().toURL();
					URLClassLoader loader = new URLClassLoader(buffer);
					Enumeration<JarEntry> entries = jf.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						if (entry.isDirectory()) continue;
						if (!entry.toString().toLowerCase().endsWith(".class")) continue;
						String name=entry.toString().substring(0, entry.toString().length()-6).replace('/', '.');
						Class<?> loaded = loader.loadClass(name);
						Annotation annotations[] = loaded.getAnnotations();
						for (Annotation annotation : annotations) {
							if (annotation.annotationType().equals(LogWrapperExtension.class)) {
								extensions_dummy.add(new Extension(file, loaded));
							}
						}
					}
					loader.close();
					jf.close();
				} catch (IOException e) {
					System.err.println("ファイルの読み込み中に例外が発生しました: \n");
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void loadSystemExtension() {
		extensions_dummy.add(new Extension(null, CorePlugin.class));
	}

	public static void doAll(Consumer<Extension> consumer) {
		for (Extension extension : extensions_dummy) {
			if (!extension.validate()) {
				if (extensions.containsKey(extension.id)) extensions.remove(extension.id);
				continue;
			}
			try {
				consumer.accept(extension);
			} catch (RuntimeException e) {
				continue;
			}
		}
	}

	// @Async
	public static void doAllParallel(Consumer<Extension> consumer) {
		doAll(e->new Thread(()->consumer.accept(e)).start());
	}

}
