package com.mkm75.mclw.betterlogging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class Langs {

	public static String language = "en_us";
	public static Map<String, String> translate = new HashMap<>();

	public static void load() {
		translate.clear();
		File file = new File("./lang/"+language+"/");
		Gson gson = new Gson();
		for (File file2 : file.listFiles()) {
			try {
				BufferedReader bis = new BufferedReader(new InputStreamReader(new FileInputStream(file2), StandardCharsets.UTF_8));
				JsonObject jo = gson.fromJson(bis, JsonObject.class);
				for (Entry<String, JsonElement> entry : jo.entrySet()) {
					try {
						translate.put(entry.getKey(), entry.getValue().getAsString());
					} catch (JsonParseException e) {
						e.printStackTrace();
						continue;
					}
				}
				System.out.println("[LangLoader] 読み込みに成功しました: "+file2.toString());
			} catch (IOException e) {
				System.out.println("[LangLoader] 読み込みに失敗しました: "+file2.toString());
				e.printStackTrace();
			} catch (JsonParseException e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	public static boolean isLocalizable(String id) {
		return translate.containsKey(id);
	}

	public static String Localize(String id) {
		return translate.getOrDefault(id, id);
	}

}
