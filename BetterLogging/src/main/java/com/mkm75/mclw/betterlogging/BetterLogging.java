package com.mkm75.mclw.betterlogging;

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
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mkm75.mclw.mclogwrapper.base.ProcessReader;
import com.mkm75.mclw.mclogwrapper.core.ClassHolder;
import com.mkm75.mclw.mclogwrapper.core.CorePlugin;
import com.mkm75.mclw.mclogwrapper.core.Runner;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.Initializable;
import com.mkm75.mclw.mclogwrapper.extensions.interfaces.LogWrapperExtension;

@LogWrapperExtension(major_version=BetterLogging.MAJOR_VERSION, minor_version=BetterLogging.MINOR_VERSION,
					name=BetterLogging.NAME, requirements_name = { CorePlugin.NAME }, requirements_version = { 0 })
public class BetterLogging implements Initializable {

	public static final double MAJOR_VERSION=0;
	public static final double MINOR_VERSION=1.1;
	public static final String NAME="BetterLogging@mkm75";

	public static final String REGEX="<mAw99vWVcx6u56>";
	public static final byte SIGNATURE[]= {0x6D, 0x6B, 0x75};
	public static final String SERVER_NAME="./server_btlg.bin";

	public void setInstances() {

	}

	public void override() {
		ClassHolder.put(ProcessReader.class, new BetterProcessReader());
	}

	public void preInitialize() {

	}

	public void postInitialize() {
		System.out.println("[BetterLogging] 読み込み開始");
		init:
		try {
			File server_btlg = new File(SERVER_NAME);
			check:
			{
				if (!server_btlg.exists()) break check;
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(server_btlg));
				byte buf[] = new byte[SIGNATURE.length];
				bis.read(buf);
				if (!Arrays.equals(SIGNATURE, buf)) break check;
				byte sum=(byte)bis.read();
				bis.close();
				bis = new BufferedInputStream(new FileInputStream(Runner.SERVER));
				buf=new byte[8192];
				byte sum2=0;
				while (true) {
					int len = bis.read(buf);
					if (len == -1) break;
					for (int i=0;i<len;i++) {
						sum2+=buf[i];
					}
				}
				if (sum!=sum2) break check;
				bis.close();
				break init;
			}
			System.out.println("[BetterLogging] 出力取得最適化のための初期化を行います");
			System.out.println("[BetterLogging] この作業には少し時間がかかります");
			System.out.println("[BetterLogging] この作業によりBetterLoggingに特化されたserver_btlg.binが作成されます");
			// require initialize
			// reset pos ... to close once

			System.out.println("[BetterLogging] お湯を沸かしています...");
			String filename = "temp0_BtLog";

			{
				// CPUの駆動率でお湯が沸かせます
				ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(new File(Runner.SERVER))));
				ZipEntry e;
				byte buf[] = new byte[8192];
				while ((e=zis.getNextEntry()) != null) {
					File dest = new File("./"+filename+"/"+e.getName());
					if (e.isDirectory()) {
						dest.mkdirs();
					} else {
						dest.getParentFile().mkdirs();
						FileOutputStream fos = new FileOutputStream(dest);
						while (zis.available()!=0) {
							int len=zis.read(buf);
							if (len==-1) break;
							fos.write(buf, 0, len);
						}
						fos.close();
					}
				}
				zis.close();
			}
			System.out.println("[BetterLogging] 豆を挽いています...");
			{
				File lang = new File("./lang");
				lang.mkdir();
				if (new File("./"+filename+"/lang/").exists()) {
					// ~1.5.2
					for (File file : new File("./"+filename+"/lang/").listFiles()) {
						File target = new File(lang, file.getName().substring(0, 5).toLowerCase()+"/vanilla.lang");
						target.getParentFile().mkdirs();
						Files.copy(file, target);
						convert1(target);
						format1(file);
					}
				} else if (new File("./"+filename+"/assets/minecraft/lang/en_us.lang").exists()) {
					// Country code became lower case since somewhere but i dont take care because those are approximately same
					// 1.6~1.12.2
					for (File file : new File("./"+filename+"/assets/minecraft/lang/").listFiles()) {
						File target = new File(lang, file.getName().substring(0, 5).toLowerCase()+"/vanilla.lang");
						target.getParentFile().mkdirs();
						Files.copy(file, target);
						convert1(target);
						format1(file);
					}
				} else if (new File("./"+filename+"/assets/minecraft/lang/en_us.json").exists()) {
					// 1.13~
					for (File file : new File("./"+filename+"/assets/minecraft/lang/").listFiles()) {
						File target = new File(lang, file.getName().substring(0, 5).toLowerCase()+"/vanilla.json");
						target.getParentFile().mkdirs();
						Files.copy(file, target);
						convert2(target);
						format2(file);
					}
				}
			}
			System.out.println("[BetterLogging] コーヒーを淹れています...");
			File temp1 = new File("./temp1_BtLog.bin");
			{
				compress(new File("./"+filename), temp1);
				byte sum=0;
				{
					BufferedInputStream bis = new BufferedInputStream(new FileInputStream(Runner.SERVER));
					byte buf[] = new byte[8192];
					while (true) {
						int len = bis.read(buf);
						if (len == -1) break;
						for (int i=0;i<len;i++) {
							sum+=buf[i];
						}
					}
					bis.close();
				}
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(temp1));
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(SERVER_NAME));
				byte buf[] = new byte[8192];
				bos.write(SIGNATURE);
				bos.write(sum);
				while (true) {
					int len = bis.read(buf);
					if (len == -1) break;
					bos.write(buf, 0, len);
				}
				bis.close();
				bos.close();
				remove(temp1);
				remove(new File("./"+filename));
			}
			System.out.println("[BetterLogging] 初期化が完了しました");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Langs.load();
		Runner.SERVER = SERVER_NAME;
		System.out.println("[BetterLogging] 読み込みは正常に終了しました");
	}

	protected static void compress(File parent, File dest) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(dest)));
		for (File file : parent.listFiles()) {
			compress0(zos, file, file.getName());
		}
		zos.close();
	}
	protected static void compress0(ZipOutputStream zos, File file, String base) throws IOException {
		if (file.isDirectory()) {
			ZipEntry e = new ZipEntry(base+"/");
			zos.putNextEntry(e);
			for (File file2 : file.listFiles()) {
				compress0(zos, file2, base+"/"+file2.getName());
			}
		} else {
			ZipEntry e = new ZipEntry(base);
			zos.putNextEntry(e);
			byte buffer[] = new byte[8192];
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			while (true) {
				int len = bis.read(buffer);
				if (len==-1) break;
				zos.write(buffer, 0, len);
			}
			zos.closeEntry();
			bis.close();
		}
	}

	static void remove(File file) {
		if (file.isDirectory()) {
			for (File file2 : file.listFiles()) {
				remove(file2);
			}
		}
		file.delete();
	}

	protected static void format1(File file) throws IOException {
		File buffer = new File(file.toString()+".tmp");
		BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(file))));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(buffer))));
		while (true) {
			String str = br.readLine();
			if (str == null) break;
			if (str == "") {
				bw.write("\n");
			} else {
				String array[] = str.split("=", 2);
				if (array.length != 2) continue;
				String key=array[0];
				if (key.equals("command.context.here")) {
					bw.write(str+"\n");
				} else {
					int count = count(array[1]);
					StringBuilder sb = new StringBuilder();
					sb.append(key).append('=').append(key);
					for (int i=0;i<count;i++) {
						sb.append(REGEX).append("%s");
					}
					sb.append('\n');
					bw.write(sb.toString());
				}
			}
		}
		br.close();
		bw.close();
		file.delete();
		Files.move(buffer, file);

	}
	protected static void format2(File file) throws IOException {
		File buffer = new File(file.toString()+".tmp");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(file))));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(buffer))));
		JsonObject jo = gson.fromJson(br, JsonObject.class);
		JsonObject jo2 = new JsonObject();
		for (Entry<String, JsonElement> entry : jo.entrySet()) {
			String key=entry.getKey();
			if (key.equals("command.context.here")) {
				jo2.add(key, entry.getValue());
			} else {
				int count = count(entry.getValue().getAsString());
				StringBuilder sb = new StringBuilder();
				sb.append(key);
				for (int i=0;i<count;i++) {
					sb.append(REGEX).append("%s");
				}
				jo2.addProperty(key, sb.toString());
			}
		}
		gson.toJson(jo2, bw);
		br.close();
		bw.close();
		file.delete();
		Files.move(buffer, file);
	}

	protected static void convert1(File file) throws IOException {
		File buffer = new File(file.toString().substring(0, file.toString().lastIndexOf('.'))+".json");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(file))));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(buffer))));
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
		BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(file))));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(buffer))));
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

	protected static int count(String value) {
		char chs[] = value.toCharArray();
		int max=0;
		int cur=0;
		boolean percent=false;
		for (char ch : chs) {
			if (percent) {
				if ('0'<ch & ch<='9') max=ch-'0';
				if (ch=='s'|ch=='S'|ch=='d'|ch=='D') cur++;
				percent=false;
			} else {
				if (ch == '%') {
					percent=true;
				}
			}
		}
		return (max>cur)?max:cur;
	}

	public static void main(String args[]) throws IOException {
		String filename = "temp0_BtLog";
		{
			File lang = new File("./lang");
			lang.mkdir();
			for (File file : new File("./"+filename+"/lang/").listFiles()) {
				File target = new File(lang, file.getName().substring(0, 5).toLowerCase()+"/"+file.getName());
				target.getParentFile().mkdirs();
				Files.copy(file, target);
				convert1(target);
				format1(file);
			}
		}
	}

}
