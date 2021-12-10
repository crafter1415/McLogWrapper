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
			boolean log4jprotect=false;
			if (System.getenv("log4j2.formatMsgNoLookups") != null) {
				arg2.add("-Dlog4j2.formatMsgNoLookups="+System.getenv("log4j2.formatMsgNoLookups"));
				log4jprotect=true;
			}
			if (System.getenv("log4j.configurationFile") != null) {
				arg2.add("-Dlog4j.configurationFile="+System.getenv("log4j.configurationFile"));
				log4jprotect=true;
			}
			arg2.add("-Dlog4j2.formatMsgNoLookups=true");
			arg2.add("-jar");
			arg2.add(SERVER);
			arg2.add("nogui");
			if (!log4jprotect) {
				System.out.println("[MCLW/WARN] **Is your server patched?** Log4j exploit could easily damage your device, take care and DO NOT LAUNCH SERVER IF YOUR SERVER IS UNPATCHED!!");
				System.out.println("[MCLW/警告] **サーバーはパッチ済ですか？** Log4jの脆弱性はとても危険なものであり、貴方のデバイスが簡単に傷つけられる危険性があります。パッチの適用が済んでいない限りサーバーを実行しないでください！");
				System.out.println("[MCLW/INFO] 脆弱性対策の為10秒後に起動を開始します / Server will boot 10 sec later");
				System.out.println("[MCLW/INFO] http://redsto.ne/java");
				Thread.sleep(10000);
			} else {
				System.out.println("[MCLW] このサーバーにはLog4j脆弱性対策が導入されています");
			}

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
				Extensions.save_configs();
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
			Extensions.save_configs();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
		}
	}
}
