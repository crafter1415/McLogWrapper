package com.mkm75.mclw.mclogwrapper.extensions.interfaces;

import com.mkm75.mclw.mclogwrapper.extensions.Config;

public interface UseConfig {
	public Config reserveConfigs();
	default public void onConfigLoaded() {};
}
