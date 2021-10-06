package com.mkm75.mclw.mclogwrapper.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mkm75.mclw.mclogwrapper.base.ConsoleReader;
import com.mkm75.mclw.mclogwrapper.base.ProcessReader;
import com.mkm75.mclw.mclogwrapper.base.ProcessWriter;
import com.mkm75.mclw.mclogwrapper.extensions.Extensions;

public class Runner {

	public static String SERVER = "./server.jar";

	public static void main(String args[]) {
		try {
			Extensions.load();
			Runtime rt = Runtime.getRuntime();
			long Xmx=rt.maxMemory()/1048576;
			long Xms=rt.totalMemory()/1048576;
			List<String> arg2 = new ArrayList<>();
			arg2.add(System.getProperty("java.home").replace('\\', '/') + "/bin/java.exe");
			arg2.add("-Xms"+Xms+"m");
			arg2.add("-Xmx"+Xmx+"m");
			arg2.add("-jar");
			arg2.add(SERVER);
			arg2.add("nogui");

			ProcessBuilder builder = new ProcessBuilder(arg2);
			builder.redirectErrorStream(true);
			Process process;
			ConsoleReader ic = ClassHolder.get(ConsoleReader.class);
			ProcessReader oc = ClassHolder.get(ProcessReader.class);
			ProcessWriter pw = ClassHolder.get(ProcessWriter.class);
			System.out.println("[MCLogWrapper] プロセスを以下の条件でバイパスします: -Xms" + Xms + "m -Xmx" + Xmx + "m\n");
			process = builder.start();
			Thread hook = new Thread(()->{
				System.out.println("[MCLogWrapper] 親プロセスの強制終了を検出しました。子プロセスの終了を試みます...");
				process.destroyForcibly();
				System.out.println("[MCLogWrapper] 成功");
			});
			rt.addShutdownHook(hook);
			ic.setStream(System.in);
			ic.setWriter(pw);
			oc.setStream(process.getInputStream());
			pw.setStream(process.getOutputStream());
			Thread inThread = new Thread(ic::run);
			Thread outThread = new Thread(oc::run);
			Thread writeThread = new Thread(pw::run);
			inThread.setDaemon(true);
			inThread.start();
			outThread.start();
			writeThread.start();

			process.waitFor();

			rt.removeShutdownHook(hook);
			Extensions.extensions.values().forEach(e->new Thread(e::onStop).start());
			outThread.interrupt();
			writeThread.interrupt();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
		}
	}
}
