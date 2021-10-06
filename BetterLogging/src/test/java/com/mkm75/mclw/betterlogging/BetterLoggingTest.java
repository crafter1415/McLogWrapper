package com.mkm75.mclw.betterlogging;

import org.junit.Assert;
import org.junit.Test;

public class BetterLoggingTest {

	@Test
	public void CheckRegexCorrect() {
		check();
		Assert.assertTrue(true);
	}

	boolean check() {
		if (BetterLogging.REGEX.length() != 16) throw new RuntimeException();
		if (!BetterLogging.REGEX.startsWith("<")) throw new RuntimeException();
		if (!BetterLogging.REGEX.endsWith(">")) throw new RuntimeException();
		for (char ch : ".*+?^$~|\\[](){}".toCharArray()) {
			if (BetterLogging.REGEX.contains(ch+"")) throw new RuntimeException();
		}
		for (char ch : BetterLogging.REGEX.toCharArray()) {
			if (ch<0x20|0x7f<=ch) throw new RuntimeException();
		}
		return true;
	}

}
