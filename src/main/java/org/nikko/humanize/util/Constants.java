package org.nikko.humanize.util;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Constants {

	public static final Pattern SPLIT_CAMEL_REGEX = Pattern
	        .compile("(?<=[A-Z])(?=[A-Z][a-z])|(?<=[^A-Z])(?=[A-Z])|(?<=[A-Za-z])(?=[^A-Za-z])");

	public static final String ORDINAL_FMT = "%d%s";

	public static final int ND_FACTOR = 1000 * 60 * 60 * 23;

	public static final String SPACE_STRING = " ";

	public static final String EMPTY_STRING = "";

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

	public static final Map<Long, String> binPrexies = new LinkedHashMap<Long, String>();

	static {
		binPrexies.put(1125899906842624L, "%.2f PB");
		binPrexies.put(1099511627776L, "%.2f TB");
		binPrexies.put(1073741824L, "%.2f GB");
		binPrexies.put(1048576L, "%.2f MB");
		binPrexies.put(1024L, "%.1f kB");
		binPrexies.put(0L, "%.0f bytes");
	}

}
