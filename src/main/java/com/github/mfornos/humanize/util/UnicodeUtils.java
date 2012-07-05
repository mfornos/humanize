package com.github.mfornos.humanize.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.icu.lang.UCharacter;

public class UnicodeUtils {

	// See http://en.wikipedia.org/wiki/UTF-16
	private static final Pattern NOT_IN_BMP = Pattern.compile("([^\u0000-\uD7FF\uE000-\uFFFF])");

	public static String replaceSupplementary(String source) {

		Matcher matcher = NOT_IN_BMP.matcher(source);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sb, UCharacter.getName(matcher.group(1), ", "));
		}
		return (sb.length() > 0) ? sb.toString() : source;

	}

}
