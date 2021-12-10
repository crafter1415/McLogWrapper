package com.mkm75.mclw.mclogwrapper.base;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * プロセスに文字列を出力します。
 * <br><br>
 * 内部的にはインスタンスへの書き込みとプロセスへの伝達が非同期であり最高で100ms程度遅れることに注意してください。<br>
 * スレッドによる継続実行が必要です。
 */
public class ProcessWriter {

	// write JAR input

	Charset ch;
	OutputStream stream;
	List<String> lines;

	/**
	 * {@code ProcessWriter}の新しいインスタンスを作成します。
	 * <br><br>
	 * 作成時に 初期容量10 の空のリストがバッファとして用意されます。<br>
	 * 利用する前に{@link OutputStream}を指定する必要があります。
	 */
	public ProcessWriter() {
		lines=new ArrayList<>();
		ch=StandardCharsets.UTF_8;
	}

	/**
	 * 出力ストリームを初期化します。
	 * <br>
	 * @param stream 出力先
	 */
	public void setStream(OutputStream stream) {
		this.stream = stream;
	}

	/**
	 * 入力に対する処理を開始します。
	 * <br><br>
	 * 行単位で追加された入力は最大で100msの遅延を経てプロセスに出力されます。<br>
	 * スレッドによる継続実行が必要です。
	 * @throws IllegalStateException {@code stream}が未設定の場合
	 */
	public void run() {
		Objects.requireNonNull(stream, "Stream is null");
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(stream, ch));
			while (true) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					break;
				}
				while (0 < lines.size()) {
					bw.write(lines.get(0)+"\n");
					bw.flush();
					lines.remove(0);
				}
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 行出力を追加します。
	 * <br><br>
	 * 一般的な{@code PrintWriter}における{@code println}に該当します。
	 *
	 * @param line 追加する行
	 * @throws NullPointerException 引数がnullだった場合
	 */
	public void append(String line) {
		Objects.requireNonNull(line);
		lines.add(line);
	}
	/**
	 * 行出力を追加します。
	 * <br><br>
	 * 一般的な{@code PrintWriter}における{@code println}に該当します。<br>
	 * 内部的には {@code lines.add(Byte.toString())} に一致します。
	 *
	 * @param line 追加する行
	 * @throws NullPointerException 引数がnullだった場合
	 * @deprecated 本来ならプログラム内で数値行が必要になることはありません
	 */
	public void append(byte b) {
		lines.add(Byte.toString(b));
	}
	/**
	 * 行出力を追加します。
	 * <br><br>
	 * 一般的な{@code PrintWriter}における{@code println}に該当します。<br>
	 * 内部的には {@code lines.add(Short.toString())} に一致します。
	 *
	 * @param line 追加する行
	 * @throws NullPointerException 引数がnullだった場合
	 * @deprecated 本来ならプログラム内で数値行が必要になることはありません
	 */
	public void append(short s) {
		lines.add(Short.toString(s));
	}
	/**
	 * 行出力を追加します。
	 * <br><br>
	 * 一般的な{@code PrintWriter}における{@code println}に該当します。<br>
	 * 内部的には {@code lines.add(Integer.toString())} に一致します。
	 *
	 * @param line 追加する行
	 * @throws NullPointerException 引数がnullだった場合
	 * @deprecated 本来ならプログラム内で数値行が必要になることはありません
	 */
	public void append(int i) {
		lines.add(Integer.toString(i));
	}
	/**
	 * 行出力を追加します。
	 * <br><br>
	 * 一般的な{@code PrintWriter}における{@code println}に該当します。<br>
	 * 内部的には {@code lines.add(Long.toString())} に一致します。
	 *
	 * @param line 追加する行
	 * @throws NullPointerException 引数がnullだった場合
	 * @deprecated 本来ならプログラム内で数値行が必要になることはありません
	 */
	public void append(long l) {
		lines.add(Long.toString(l));
	}
	/**
	 * 行出力を追加します。
	 * <br><br>
	 * 一般的な{@code PrintWriter}における{@code println}に該当します。<br>
	 * 内部的には {@code lines.add(Float.toString())} に一致します。
	 *
	 * @param line 追加する行
	 * @throws NullPointerException 引数がnullだった場合
	 * @deprecated 本来ならプログラム内で数値行が必要になることはありません
	 */
	public void append(float f) {
		lines.add(Float.toString(f));
	}
	/**
	 * 行出力を追加します。
	 * <br><br>
	 * 一般的な{@code PrintWriter}における{@code println}に該当します。<br>
	 * 内部的には {@code lines.add(Double.toString())} に一致します。
	 *
	 * @param line 追加する行
	 * @throws NullPointerException 引数がnullだった場合
	 * @deprecated 本来ならプログラム内で数値行が必要になることはありません
	 */
	public void append(double d) {
		lines.add(Double.toString(d));
	}
	/**
	 * 行出力を追加します。
	 * <br><br>
	 * 一般的な{@code PrintWriter}における{@code println}に該当します。<br>
	 * 内部的には {@code lines.add(Boolean.toString())} に一致します。
	 *
	 * @param line 追加する行
	 * @throws NullPointerException 引数がnullだった場合
	 * @deprecated 本来ならプログラム内で真偽行が必要になることはありません
	 */
	public void append(boolean b) {
		lines.add(Boolean.toString(b));
	}
	/**
	 * 行出力を追加します。
	 * <br><br>
	 * 一般的な{@code PrintWriter}における{@code println}に該当します。<br>
	 * 内部的には {@code lines.add(Objects.toString())} に一致します。
	 *
	 * @param line 追加する行
	 * @throws NullPointerException 引数がnullだった場合
	 */
	public void append(Object o) {
		lines.add(Objects.toString(o));
	}

	/**
	 * 複数行出力を追加します。
	 * <br><br>
	 * {@code append(element)} を繰り返し呼び出します。
	 *
	 * @param lines 追加する行
	 * @throws NullPointerException 引数がnullだった場合
	 */
	public void append(List<String> lines) {
		Objects.requireNonNull(lines);
		for (String line : lines) append(line);
	}

}
