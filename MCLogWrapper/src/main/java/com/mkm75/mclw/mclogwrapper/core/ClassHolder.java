package com.mkm75.mclw.mclogwrapper.core;

import java.util.HashMap;
import java.util.Map;

/**
 * オーバーライド可能な実装を用いたいときに利用してください。
 * <br><br>
 * このクラスは任意のコンストラクタを保持します。
 */
public class ClassHolder {

	private static Map<Class<? extends Object>, Object> map = new HashMap<>();

	public static <T> void put(T instance) {
		map.put(instance.getClass(), instance);
	}

	// Override existing one
	public static <T> void put(Class<? super T> clazz, T instance) {
		map.put(clazz, instance);
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(Class<T> clazz) {
		return (T)map.getOrDefault(clazz, null);
	}

}
