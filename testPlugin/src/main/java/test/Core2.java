package test;

import com.mkm75.mclw.mclogwrapper.extensions.interfaces.LogWrapperExtension;

@LogWrapperExtension(name="test2", major_version=1.0, minor_version=0.0, requirements_name= {}, requirements_version= {})
public class Core2 {
	public Core2() {
		System.out.println("[TestPlugin2] test");
	}
}
