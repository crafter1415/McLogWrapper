package com.mkm75.mclw.mclogwrapper.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import com.mkm75.mclw.mclogwrapper.extensions.Extensions;

/**
 * コンソールからのユーザー入力を取得します。
 * <br><br>
 * 内部的には入力の取得と内部サーバーへの伝達のみを行い、その他の処理はプラグインに委ねます。
 */
public class ConsoleReader {

	// check CONSOLE input

	protected InputStream stream;
	protected ProcessWriter writer;

	/**
	 * {@code ConsoleReader}の新しいインスタンスを作成します。
	 * <br><br>
	 * 作成された時点では初期化は行われません。<br>
	 * 利用する前に{@link InputStream}と{@link ProcessWriter}を指定する必要があります。
	 */
	public ConsoleReader() {

	}

	/**
	 * 入力ストリームを初期化します。
	 * <br>
	 * @param stream 検出する入力ストリーム - 一般的には{@code System.in}
	 */
	public void setStream(InputStream stream) {
		this.stream = stream;
	}
	/**
	 * 出力ストリームを初期化します。
	 * <br>
	 * @param writer 出力先のプロセスに書き込むインスタンス
	 */
	public void setWriter(ProcessWriter writer) {
		this.writer = writer;
	}

	/**
	 * ストリームの処理を開始します。
	 * <br><br>
	 * 入力ストリームからの入力は行単位で読み込まれ、出力ストリームでの出力が予約されたのちに各プラグインで処理されます。<br>
	 * 継続的な処理のため、新しいスレッドで実行することを強く推奨します。
	 * @throws IllegalStateException {@code stream}や{@code writer}が未設定の場合
	 */
	public void run() {
		Objects.requireNonNull(stream, "Stream is null");
		Objects.requireNonNull(writer, "Writer is null");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			while (!Thread.interrupted()) {
				String str;
				try {
					str = br.readLine();
				} catch (IOException e1) {
					continue;
				}
				if (str == null) break;
				writer.append(str);
				Extensions.doAllParallel(e->e.consumeConsoleIn(str));
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
