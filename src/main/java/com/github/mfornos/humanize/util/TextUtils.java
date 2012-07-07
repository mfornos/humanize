package com.github.mfornos.humanize.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.icu.lang.UCharacter;

public class TextUtils {

	public static String replaceSupplementary(String text) {

		return interpolate(text, Constants.NOT_IN_BMP, new Replacer() {
			public String replace(String in) {

				return UCharacter.getName(in, ", ");

			}
		});

	}

	public static String interpolate(String text, Pattern pattern, Replacer replacer) {

		Matcher matcher = pattern.matcher(text);
		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			matcher.appendReplacement(sb, replacer.replace(matcher.group(1)));
		}

		matcher.appendTail(sb);

		return (sb.length() > 0) ? sb.toString() : text;
	}

}
