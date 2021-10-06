package com.mkm75.mclw.mclogwrapper.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * このクラスは任意のインスタンスを保持します。
 * <br><br>
 * オーバーライド可能な実装を用いたいときに利用してください。<br>
 * クラスは初期化時にのみ変更され、その後は一定である必要があります。
 */
public class ClassHolder {

	private static Map<Integer, Object> map = new HashMap<>();

	/**
	 * インスタンスを登録します。
	 * <br>
	 * @param instance 登録するインスタンス
	 */
	public static <T> void put(T instance) {
		put(instance.getClass().getName().hashCode(), instance);
	}

	/**
	 * インスタンスを登録、あるいは既存の物をオーバーライドしたインスタンスで上書きします。
	 * <br><br>
	 * インスタンスが継承関係にあることが保証されている必要があります。
	 *
	 * @param clazz 継承元あるいは自身のクラス
	 * @param instance 登録/上書きするインスタンス
	 */
	public static <T> void put(Class<? super T> clazz, T instance) {
		put(clazz.getName().hashCode(), instance);
	}

	private static <T> void put(int clazz_hash, T instance) {
		Objects.requireNonNull(instance);
		map.put(clazz_hash, instance);
	}

	/**
	 * インスタンスを取得します。
	 *
	 * @param clazz 取得するインスタンスのクラス
	 * @return インスタンス
	 */
	public static <T> T get(Class<T> clazz) {
		return get(clazz.getName().hashCode());
	}

	@SuppressWarnings("unchecked")
	private static <T> T get(int clazz_hash) {
		return (T)map.get(clazz_hash);
	}

}
