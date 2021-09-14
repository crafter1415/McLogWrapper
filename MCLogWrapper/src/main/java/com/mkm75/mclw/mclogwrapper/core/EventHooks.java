package com.mkm75.mclw.mclogwrapper.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class EventHooks {

	private static HashMap<Class<?>, Set<Consumer<?>>> hooks = new HashMap<>();

	public static <T> void add(Class<T> clazz, Consumer<T> hook) {
		if (!hooks.containsKey(clazz)) hooks.put(clazz, new HashSet<>());
		hooks.get(clazz).add(hook);
	}

	public static <T> void remove(Class<T> clazz, Consumer<T> hook) {
		if (!hooks.containsKey(clazz)) return;
		hooks.get(clazz).remove(hook);
	}

	@SuppressWarnings("unchecked")
	public static <T> void call(T event) {
		if (!hooks.containsKey(event.getClass())) return;
		hooks.get(event.getClass()).forEach(r->new Thread(()->((Consumer<T>)r).accept(event)).start());
	}

}
