package com.github.mfornos.humanize.util;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Constants {

	public static final Pattern SPLIT_CAMEL = Pattern
	        .compile("(?<=[A-Z])(?=[A-Z][a-z])|(?<=[^A-Z])(?=[A-Z])|(?<=[A-Za-z])(?=[^A-Za-z])");

	// See http://en.wikipedia.org/wiki/UTF-16
	public static final Pattern NOT_IN_BMP = Pattern.compile("([^\u0000-\uD7FF\uE000-\uFFFF])");

	public static final String SPACE = " ";

	public static final String EMPTY = "";

	public static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);

	public static final Map<BigDecimal, String> bigDecExponents = new LinkedHashMap<BigDecimal, String>();

	static {
		bigDecExponents.put(BigDecimal.TEN.pow(3), "thousand");
		bigDecExponents.put(BigDecimal.TEN.pow(6), "million");
		bigDecExponents.put(BigDecimal.TEN.pow(9), "billion");
		bigDecExponents.put(BigDecimal.TEN.pow(12), "trillion");
		bigDecExponents.put(BigDecimal.TEN.pow(15), "quadrillion");
		bigDecExponents.put(BigDecimal.TEN.pow(18), "quintillion");
		bigDecExponents.put(BigDecimal.TEN.pow(21), "sextillion");
		bigDecExponents.put(BigDecimal.TEN.pow(24), "septillion");
		bigDecExponents.put(BigDecimal.TEN.pow(27), "octillion");
		bigDecExponents.put(BigDecimal.TEN.pow(30), "nonillion");
		bigDecExponents.put(BigDecimal.TEN.pow(33), "decillion");
		bigDecExponents.put(BigDecimal.TEN.pow(36), "undecillion");
		bigDecExponents.put(BigDecimal.TEN.pow(39), "duodecillion");
		bigDecExponents.put(BigDecimal.TEN.pow(100), "googol");
	}

	public static final Map<Long, String> binPrefixes = new LinkedHashMap<Long, String>();

	static {
		binPrefixes.put(1125899906842624L, "#.## PB");
		binPrefixes.put(1099511627776L, "#.## TB");
		binPrefixes.put(1073741824L, "#.## GB");
		binPrefixes.put(1048576L, "#.## MB");
		binPrefixes.put(1024L, "#.# kB");
		binPrefixes.put(0L, "# bytes");
	}

	public static final Map<Long, String> metricPrefixes = new LinkedHashMap<Long, String>();

	static {
		metricPrefixes.put(1000000000000000L, "#.##P");
		metricPrefixes.put(1000000000000L, "#.##T");
		metricPrefixes.put(1000000000L, "#.##G");
		metricPrefixes.put(1000000L, "#.##M");
		metricPrefixes.put(1000L, "#.#k");
		metricPrefixes.put(0L, "#.#");
	}

}
