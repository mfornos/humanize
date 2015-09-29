package humanize.text.util;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.base.Strings;
import com.google.common.collect.Range;
import com.google.common.escape.UnicodeEscaper;

public class UnicodeInterpolator extends UnicodeEscaper
{
    private final Replacer replacer;
    private final Collection<Range<Integer>> ranges;

    public UnicodeInterpolator(Replacer replacer)
    {
        this.replacer = replacer;
        this.ranges = new ArrayList<Range<Integer>>();
    }

    public void addRange(int lower, int upper)
    {
        addRange(Range.closed(lower, upper));
    }

    public void addRange(Range<Integer> range)
    {
        ranges.add(range);
    }

    @Override
    protected char[] escape(int codePoint)
    {
        for (Range<Integer> range : ranges)
        {
            if (range.contains(codePoint))
            {
                return Strings.nullToEmpty(replacer.replace(Integer.toHexString(codePoint))).toCharArray();
            }
        }

        return Character.toChars(codePoint);
    }
}
