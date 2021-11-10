package com.mkm75.mclw.betterlogging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import com.mkm75.mclw.mclogwrapper.base.ProcessReader;
import com.mkm75.mclw.mclogwrapper.core.EventHooks;
import com.mkm75.mclw.mclogwrapper.extensions.Extensions;

public class BetterProcessReader extends ProcessReader {
	public void run() {
		Objects.requireNonNull(stream, "Stream is null");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			String str;
			while ((str = br.readLine()) != null) {
				check:
				{
					// ここから自分で見ても引くほど分からんのでバリバリコメントしていくわよ

					// 本文の前後で切る
					String buf0[] = str.split("]: ", 2);
					// 本文無しで抜ける
					if (buf0.length == 1) break check;
					// 本文を翻訳名と引数に分ける
					String buf1[] = buf0[1].split(BetterLogging.REGEX, 2);
					// 翻訳できないなら抜ける
					if (!Langs.isLocalizable(buf1[0])) break check;
					// 引数のみの配列
					String buf2[];
					if (buf1.length == 1) {
						buf2=new String[0];
					} else {
						buf2=buf1[1].split(BetterLogging.REGEX);
					}
					// 時刻とログ情報に分ける
					String buf3[] = buf0[0].split("\\] \\[", 2);
					if (buf3.length == 1) break check;
					// スレッドとレベルに分ける
					String buf4[] = buf3[1].split("/", 2);
					if (buf4.length == 1) break check;
					LogEvent event = new LogEvent(buf3[0].substring(1), buf4[0], buf4[1], buf1[0], buf2);
					EventHooks.call(LogEvent.class, event);
					String fin=event.toString();
					Extensions.extensions.values().parallelStream().forEach(e->e.consumeLog(fin));
					continue;
				}
				String fin=str;
				Extensions.extensions.values().parallelStream().forEach(e->e.consumeLog(fin));
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
