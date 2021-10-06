package com.mkm75.mclw.mclogwrapper.extensions.interfaces;

/**
 * LoggerWrapperExtensionの注釈を持つクラスが本インターフェースを実装することにより、初期化処理を行うことができます。
 * <br><br>
 * 呼び出し順は以下の通りです: {@code setInstances->override->preInitialize->postInitialize}<br>
 * 各関数について呼び出し順の仕様を確認することを推奨します。
 */
public interface Initializable {

	/**
	 * {@link com.mkm75.mclw.mclogwrapper.core.ClassHolder ClassHolder} に任意のインスタンスを登録するためのクラスです。
	 * <br><br>
	 * 呼び出し順に関する規定はありません。
	 */
	public void setInstances();

	/**
	 * {@link com.mkm75.mclw.mclogwrapper.core.ClassHolder ClassHolder} に登録された任意のインスタンスをオーバーライドするためのクラスです。
	 * <br><br>
	 * 依存関係が設定されている場合、先に実装する拡張機能が依存する拡張機能に対して呼び出しを行います。
	 */
	public void override();

	/**
	 * 初期化を行います。
	 * <br><br>
	 * {@code postInitialize}に対して、こちらは実装する拡張機能に依存する拡張機能から処理されます。<br>
	 * 初期化の際の細かな条件指定に最適です。
	 */
	public void preInitialize();

	/**
	 * 初期化を行います。
	 * <br><br>
	 * 依存関係が設定されている場合、先に実装する拡張機能が依存する拡張機能に対して呼び出しを行います。<br>
	 * プラグインの初期化タイミングとして最適です。
	 */
	public void postInitialize();
}
