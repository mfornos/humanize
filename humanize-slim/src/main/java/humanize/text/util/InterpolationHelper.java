package humanize.text.util;

import humanize.util.Constants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper for text interpolation.
 * 
 */
public class InterpolationHelper
{

	public static String interpolate(String text, Pattern pattern, Replacer replacer)
	{

		Matcher matcher = pattern.matcher(text);
		StringBuffer sb = new StringBuffer();

		while (matcher.find())
		{
			matcher.appendReplacement(sb, replacer.replace(matcher.group(1)));
		}

		matcher.appendTail(sb);

		return (sb.length() > 0) ? sb.toString() : text;
	}

	public static String replaceSupplementary(String text, Replacer replacer)
	{

		return interpolate(text, Constants.NOT_IN_BMP, replacer);

	}

}
