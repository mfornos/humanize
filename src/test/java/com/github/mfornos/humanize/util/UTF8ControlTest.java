package com.github.mfornos.humanize.util;

import java.util.Locale;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.mfornos.humanize.util.UTF8Control;

public class UTF8ControlTest {

	// TODO implement test
	@Test
	public void newBundle() {
		UTF8Control control = new UTF8Control();
		try {
			control.newBundle("", Locale.getDefault(), "", UTF8ControlTest.class.getClassLoader(), true);
		} catch (Exception e) {
			Assert.fail(e.getMessage(), e);
		}
	}
}
