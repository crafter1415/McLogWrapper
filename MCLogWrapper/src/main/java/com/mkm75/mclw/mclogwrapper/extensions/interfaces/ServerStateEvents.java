package com.mkm75.mclw.mclogwrapper.extensions.interfaces;

/**
 * 起動完了時・サーバー停止時のイベントを指定します。<br><br>
 *
 * <b>重要: サーバー停止イベントが呼ばれた時点でコマンドを実行することはできません!</b>
 */
public interface ServerStateEvents {

	default void onDone() {}
	default void onStop() {}

}
