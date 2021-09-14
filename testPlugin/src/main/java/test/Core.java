package test;

import com.mkm75.mclw.mclogwrapper.extensions.interfaces.LogWrapperExtension;

@LogWrapperExtension(name="test", major_version=1.0, minor_version=0.0, requirements_name= {"test2"}, requirements_version= {1.0})
public class Core {
	public Core() {
		System.out.println("[TestPlugin] test");
	}
}

