package com.mkm75.mclw.langutil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mkm75.mclw.betterlogging.BetterLogging;
import com.mkm75.mclw.betterlogging.Langs;
import com.mkm75.mclw.mclogwrapper.extensions.Config;
import com.mkm75.mclw.mclogwrapper.extensions.Extensions;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.Initializable;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.LogWrapperExtension;

@LogWrapperExtension(major_version = LangUtil.MAJOR_VERSION, minor_version = LangUtil.MINOR_VERSION, name = BetterLoggingIntegr.NAME,
					requirements_name = { LangUtil.NAME, BetterLogging.NAME }, requirements_version = { LangUtil.MAJOR_VERSION, 0 }, is_optional = true)
public class BetterLoggingIntegr implements Initializable {

	// but yeah please use each one to regist requirements...
	public static final String NAME="LangUtil_BtLgIntegr@mkm75";

	public void setInstances() {

	}

	public void override() {

	}

	public void preInitialize() {

	}

	public void postInitialize() {
		Config config = ((LangUtil)Extensions.extensions.get(LangUtil.NAME).getInstance()).config;
		if (config.get("lang", String.class) != null) Langs.language=config.get("lang", String.class);
		LangUtil lu = (LangUtil) Extensions.extensions.get(LangUtil.NAME).getInstance();
		if (lu.version_changed) {
			try {
				Gson gson = new Gson();
				String assetIndex;
				{
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(lu.mc_home+"/versions/"+lu.mc_ver+"/"+lu.mc_ver+".json")), StandardCharsets.UTF_8));
					JsonObject jo = gson.fromJson(br, JsonObject.class);
					assetIndex=jo.get("assetIndex").getAsJsonObject().get("id").getAsString();
				}
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(lu.mc_home+"/assets/indexes/"+assetIndex+".json")), StandardCharsets.UTF_8));
				JsonObject jo = gson.fromJson(br, JsonObject.class);
				for (Entry<String, JsonElement> entries : jo.get("objects").getAsJsonObject().entrySet()) {
					if (!entries.getKey().contains("lang/")) continue;
					String hash = entries.getValue().getAsJsonObject().get("hash").getAsString();
					String lang = entries.getKey();
					String base = lang.substring(0, lang.indexOf('/'));
					lang=lang.substring(lang.lastIndexOf('/'));
					String type = lang.substring(lang.lastIndexOf('.'));
					lang=lang.substring(0, lang.lastIndexOf('.'));
					File from = new File(lu.mc_home+"/assets/objects/"+hash.substring(0, 2)+"/"+hash);
					File to = new File("./lang/"+lang+"/"+base+type);
					to.getParentFile().mkdirs();
					Files.copy(from, to);
					if (type.equals(".lang")) {
						convert1(to);
					} else if (type.equals(".json")) {
						convert2(to);
					} else {
						System.out.println("[LangUtil] ????????????????????????????????????????????????????????????????????????: "+lang+type);
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			// JsonParseException???????????????????????????RuntimeException
		}
	}

	protected static void convert1(File file) throws IOException {
		File buffer = new File(file.toString().substring(0, file.toString().lastIndexOf('.'))+".json");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), StandardCharsets.UTF_8));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(buffer)), StandardCharsets.UTF_8));
		JsonObject jo2 = new JsonObject();
		while (true) {
			String str = br.readLine();
			if (str == null) break;
			String array[] = str.split("=", 2);
			if (array.length != 2) continue;
			jo2.addProperty(array[0], array[1].replace("%d", "%s").replace("% ", "%% ").replace("%\"", "%%\""));
		}
		gson.toJson(jo2, bw);
		br.close();
		bw.close();
		file.delete();
	}
	protected static void convert2(File file) throws IOException {
		File buffer = new File(file.toString()+".tmp");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), StandardCharsets.UTF_8));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(buffer)), StandardCharsets.UTF_8));
		JsonObject jo = gson.fromJson(br, JsonObject.class);
		JsonObject jo2 = new JsonObject();
		for (Entry<String, JsonElement> entry : jo.entrySet()) {
			jo2.addProperty(entry.getKey(), entry.getValue().getAsString().replace("%d", "%s").replace("% ", "%% ").replace("%\"", "%%\""));
		}
		gson.toJson(jo2, bw);
		br.close();
		bw.close();
		file.delete();
		Files.move(buffer, file);
	}

}
