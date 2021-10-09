package com.mkm75.mclw.langutil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mkm75.mclw.mclogwrapper.core.Runner;
import com.mkm75.mclw.mclogwrapper.extensions.Config;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.Initializable;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.LogWrapperExtension;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.UseConfig;

@LogWrapperExtension(major_version = LangUtil.MAJOR_VERSION, minor_version = LangUtil.MINOR_VERSION,
					name = LangUtil.NAME, requirements_name = {}, requirements_version = {})
public class LangUtil implements Initializable, UseConfig {

	public static final double MAJOR_VERSION=0;
	public static final double MINOR_VERSION=1;
	public static final String NAME="LangUtil@mkm75";

	Config config;

	public String mc_home;
	public String mc_ver;

	boolean version_changed;

	public void setInstances() {

	}

	public void override() {

	}

	public void preInitialize() {
		System.out.println("[LangUtil] 読み込み開始");
		version_changed=false;
		mc_home = config.get("mc_home", String.class);
		init0:
		{
			check:
			{
				if (mc_home.equals("undefined")) break check;
				try {
					Paths.get(mc_home);
				} catch (InvalidPathException e) {
					break check;
				}
				if (Files.notExists(Paths.get(mc_home, "versions"))) break check;
				break init0;
			}
			System.out.println("[LangUtil] 有効なMinecraftインストールを確認できませんでした");
			System.out.println("[LangUtil] コンフィグからminecraftのホームディレクトリを指定してください");
		}
		init1:
		try {
			String hash;
			{
				MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
				byte buf[] = new byte[8192];
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(Runner.SERVER)));
				while (true) {
					int len = bis.read(buf);
					if (len == -1) break;
					sha1.update(buf, 0, len);
				}
				bis.close();
				byte bytes[] = sha1.digest();
				char hex[] = "0123456789abcdef".toCharArray();
				StringBuilder sb = new StringBuilder();
				for (int i=0;i<bytes.length;i++) {
					sb.append(hex[(bytes[i]&0xf0)>>>4]);
					sb.append(hex[bytes[i]&0xf]);
				}
				hash=sb.toString();
			}
			// no pretty print cuz read only
			Gson gson = new Gson();
			check:
			{
				mc_ver = config.get("version", String.class);
				if (mc_ver == null) break check;
				if (mc_ver.equals("untracked")) break check;
				if (mc_ver.equals("undefined")) break init1;
				File file = new File(mc_home+"/versions/"+mc_ver+"/"+mc_ver+".json");
				if (!file.exists()) break check;
				try {
					BufferedReader br = new BufferedReader(new FileReader(file));
					JsonObject jo = gson.fromJson(br, JsonObject.class);
					if (!hash.equalsIgnoreCase(jo.get("downloads").getAsJsonObject().get("server").getAsJsonObject().get("sha1").getAsString())) break check;
				} catch (IOException e) {
					break check;
				} catch (JsonParseException e) {
					break check;
				}
				break init1;
			}
			// 全探索

			// え？何か？
			{
				File base = new File(mc_home, "versions");
				mc_ver=null;
				for (String str : base.list()) {
					try {
						File file = new File(base.toString()+"/"+str+"/"+str+".json");
						// btw, did you know about this?
						// new BufferedReader(new FileReader(file)) is same with
						// new BufferedReader(new InputStreamReader(new FileInputStream(file)))
						// idk which is better about adding BufferedInputStream between those...
						BufferedReader br = new BufferedReader(new FileReader(file));
						// also, I'm not sure about this resource work closed:/
						JsonObject jo = gson.fromJson(br, JsonObject.class);
						if (!hash.equalsIgnoreCase(jo.get("downloads").getAsJsonObject().get("server").getAsJsonObject().get("sha1").getAsString())) continue;
						mc_ver=jo.get("id").getAsString();
						break;
					} catch (IOException e) {
						continue;
					} catch (RuntimeException e) {
						continue;
					}
				}
				if (mc_ver == null) {
					System.out.println("[LangUtil] Minecraftのバージョンを取得できませんでした");
					System.out.println("[LangUtil] サーバーが改変済みでないこと、正規の入手手段で取得している事を確認してください");
					throw new RuntimeException();
				}
			}
			version_changed=true;
		} catch (NoSuchAlgorithmException e) {
			// RuntimeExceptionでいいだろ!!!!!!

			// 絶対に到達しません
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		config.set("version", mc_ver);
		System.out.println("[LangUtil] 読み込み完了");
	}

	public void postInitialize() {

	}

	public Config reserveConfigs() {
		config = new Config();
		config.reserve("version", "untracked");
		{
			String os = System.getProperty("os.name").toLowerCase();
			if (os.startsWith("windows")) {
				config.reserve("mc_home", System.getProperty("user.home").replace('\\', '/')+"/Appdata/Roaming/.minecraft");
			} else if (os.startsWith("mac")) {
				config.reserve("mc_home", System.getProperty("user.home").replace('\\', '/')+"/Library/Application Support/minecraft");
			} else if (os.startsWith("linux")) {
				config.reserve("mc_home", System.getProperty("user.home").replace('\\', '/')+"/.minecraft");
			} else {
				config.reserve("mc_home", "undefined");
			}
		}
		return config;
	}
}
