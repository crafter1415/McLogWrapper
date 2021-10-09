package com.mkm75.mclw.mclogwrapper.extensions;

import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Config {

	JsonObject jo;
	boolean isLoaded;
	boolean isModified;
	JsonObject modified;

	Gson gson;

	public Config() {
		gson = new Gson();
		isLoaded=false;
		isModified=false;
		jo = new JsonObject();
		modified = new JsonObject();
	}

	public <T> void reserve(String key, T def) {
		if (isLoaded) throw new IllegalStateException("Config already loaded");
		modified.add(key, gson.toJsonTree(def));
	}

	public <T> T get(String key, Class<T> clazz) {
		if (!isLoaded) throw new IllegalStateException("Config not loaded");
		Objects.requireNonNull(key);
		Objects.requireNonNull(clazz);
		if (!jo.has(key)) return null;
		return gson.fromJson(jo.get(key), clazz);
	}

	public <T> void set(String key, T value) {
		if (!isLoaded) {
			reserve(key, value);
			return;
		}
		isModified=true;
		modified.add(key, gson.toJsonTree(value));
	}

	public void load(JsonObject object) {
		isLoaded=true;
		jo=object;
	}
	public JsonObject save() {
		JsonObject merged = new JsonObject();
		for (String key : jo.keySet()) {
			merged.add(key, jo.get(key));
		}
		for (String key : modified.keySet()) {
			merged.add(key, modified.get(key));
		}
		return merged;
	}
}
