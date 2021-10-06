package com.mkm75.mclw.mclogwrapper.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class EventHooks {

	private static HashMap<Integer, Set<Consumer<?>>> hooks = new HashMap<>();

	private EventHooks() {}

	/**
	 * イベントフックを追加します。
	 * <br><br>
	 * 同一な({@code Objects.equals}で一致する)イベントフックを複数登録することはできません。<br>
	 * イベントの発生と同時に呼び出された場合、イベントの終了まで待機します。
	 * @param clazz
	 * @param hook
	 */
	public static <T> void add(Class<? extends T> clazz, Consumer<T> hook) {
		add(clazz.hashCode(), hook);
	}
	synchronized private static <T> void add(int clazz_hash, Consumer<T> hook) {
		if (!hooks.containsKey(clazz_hash)) hooks.put(clazz_hash, new HashSet<>());
		hooks.get(clazz_hash).add(hook);
	}

	/**
	 * イベントフックを削除します。
	 * <br><br>
	 * イベントの発生と同時に呼び出された場合、イベントの終了まで待機します。
	 * @param clazz
	 * @param hook
	 */
	public static <T> void remove(Class<T> clazz, Consumer<T> hook) {
		remove(clazz.hashCode(), hook);
	}
	synchronized private static <T> void remove(int clazz_hash, Consumer<T> hook) {
		if (!hooks.containsKey(clazz_hash)) return;
		hooks.get(clazz_hash).remove(hook);
	}

	/**
	 * イベントを呼び出します。
	 * <br><br>
	 * 内部的にはインスタンスのクラスを取得してそれを基準にイベントを呼び出します。<br>
	 * {@code getClass()}メソッドは高負荷なため、呼び出しが頻発する場合このメソッドの利用は推奨されません。
	 * 呼び出しが頻発する場合、もう一方のメソッドを利用することを強く推奨します。
	 * @param event
	 */
	public static <T> void call(T event) {
		call(event.getClass().hashCode(), event);
	}
	/**
	 * イベントを呼び出します。
	 * <br><br>
	 * 呼び出しに使用するクラスを直接指定あるいはキャッシュする事により、負荷の軽減につながります。
	 * @param clazz
	 * @param event
	 */
	public static <T> void call(Class<? extends T> clazz, T event) {
		call(clazz.hashCode(), event);
	}
	@SuppressWarnings("unchecked")
	synchronized private static <T> void call(int hash, T event) {
		if (!hooks.containsKey(hash)) return;
		hooks.get(hash).parallelStream().forEach(r->((Consumer<T>)r).accept(event));
	}

}
