package humanize.text.util;

import humanize.util.Constants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

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
            String replacement = replacer.replace(matcher.group(1));
            matcher.appendReplacement(sb, Strings.nullToEmpty(replacement));
        }

        matcher.appendTail(sb);

        return (sb.length() > 0) ? sb.toString() : text;
    }

    public static String replaceSupplementary(String text, Replacer replacer)
    {
        return interpolate(text, Constants.NOT_IN_BMP, replacer);
    }
}
