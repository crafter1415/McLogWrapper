package com.mkm75.mclw.mclogwrapper.extensions;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import com.google.gson.Gson;
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

	public static List<Extension> extensions = new ArrayList<>();
	public static List<Extension> extensions_sub = new ArrayList<>();

	public static void load() {
		System.out.println("[ExtensionLoader] プラグイン読み込み開始");
		loadSystemExtension();
		if (!Settings.PluginFolder.exists()) Settings.PluginFolder.mkdir();
		// 再帰的探索
		loadDir(Settings.PluginFolder);
		// 重複・依存の確認
		Map<String, Extension> extensions = new HashMap<>();
		Map<String, List<Extension>> dupes = new HashMap<>();
		for (Extension extension : Extensions.extensions) {
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
		for (Extension extension : Extensions.extensions) {
			for (int i=0;i<extension.dependencies.length;i++) {
				String s = extension.dependencies[i];
				if (!extensions.containsKey(s)) {
					extension.currentState=-1;
					if (!missings.containsKey(s)) {
						missings.put(s, new ArrayList<>());
					}
					missings.get(s).add(extension.id);
				} else {
					if (extension.dependencies_version[i] != extension.major_version) {
						if (!req_versions.containsKey(s)) {
							req_versions.put(s, new ArrayList<>());
						}
						req_versions.get(s).add(new Pair<>(s, extension.dependencies_version[i]));
					}
					extension.dep_extension[i]=extensions.get(s);
				}
			}
		}
		Extensions.extensions_sub = new ArrayList<>(Extensions.extensions);
		for (int i=0;i<Extensions.extensions.size();) {
			if (Extensions.extensions.get(i).currentState == -1) {
				Extensions.extensions.remove(i);
				continue;
			}
			i++;
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
		for (Extension extension : Extensions.extensions) {
			extension.load();
		}
		// コンフィグの割り当て
		List<Extension> configuring_extensions = Extensions.extensions.stream().filter(e->e.useConfig).collect(Collectors.toList());
		JsonObject jo;
		{
			if (Settings.ConfigFile.exists()) {
				try {
					FileReader reader = new FileReader(Settings.ConfigFile);
					Gson gson = new Gson();
					jo = gson.fromJson(reader, JsonObject.class);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
					return; // 本来到達しえない
				} catch (JsonParseException e) {
					e.printStackTrace();
					System.exit(1);
					return; // が、ランタイムの終了のなんやかんやが不確実なのかコンパイル時はこれがないとエラー
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
					ext_base.add(key, jo.get(key));
				}
			}
			config.load(ext_base);
		}
		for (Extension extension : configuring_extensions) {
			extension.onConfigLoaded();
		}
		for (Extension extension : Extensions.extensions) {
			extension.setInstances();
		}
		for (Extension extension : Extensions.extensions) {
			extension.override();
		}
		for (Extension extension : Extensions.extensions) {
			extension.initialize();
		}
		System.out.println("[ExtensionLoader] プラグイン読み込み終了");
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
								extensions.add(new Extension(file, loaded));
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
		extensions.add(new Extension(null, CorePlugin.class));
	}
}
