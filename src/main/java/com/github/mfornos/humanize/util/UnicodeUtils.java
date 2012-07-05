package com.github.mfornos.humanize.util;

import java.util.regex.Matcher;

import com.ibm.icu.lang.UCharacter;

public class UnicodeUtils {

	public static String replaceSupplementary(String source) {

		Matcher matcher = Constants.NOT_IN_BMP.matcher(source);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sb, UCharacter.getName(matcher.group(1), ", "));
		}
		return (sb.length() > 0) ? sb.toString() : source;

	}

	// TODO Easy Emoji replacement?

}
