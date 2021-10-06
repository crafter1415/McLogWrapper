package com.mkm75.mclw.mclogwrapper.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import com.mkm75.mclw.mclogwrapper.extensions.Extensions;

/**
 * プロセスからの出力を取得します。
 * <br><br>
 * {@code InputStream}から取得した文字列を行単位でプラグインに繋げる簡易的なストリームです。
 */
public class ProcessReader {

	// check JAR output

	protected InputStream stream;

	/**
	 * {@code ProcessReader}の新しいインスタンスを作成します。
	 * <br><br>
	 * 作成された時点では初期化は行われません。<br>
	 * 利用する前に{@link InputStream}を指定する必要があります。
	 */
	public ProcessReader() {

	}

	/**
	 * 入力ストリームを初期化します。
	 * <br>
	 * @param stream 検出する入力ストリーム (ProcessBuilderにてredirectErrorStreamを有効にすることを推奨します)
	 */
	public void setStream(InputStream stream) {
		this.stream = stream;
	}

	/**
	 * ストリームの処理を開始します。
	 * <br><br>
	 * 入力ストリームからの入力は行単位で読み込まれ、各プラグインで処理されます。<br>
	 * 継続的な処理の為、新しいスレッドで実行することを強く推奨します。
	 * @throws IllegalStateException {@code stream}が未設定の場合
	 */
	public void run() {
		Objects.requireNonNull(stream, "Stream is null");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			String str;
			while ((str = br.readLine()) != null) {
				final String buffer = str;
				Extensions.extensions.values().parallelStream().forEach(e->e.consumeLog(buffer));
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
