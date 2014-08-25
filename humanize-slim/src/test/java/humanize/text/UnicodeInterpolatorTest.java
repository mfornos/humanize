package humanize.text;

import static org.testng.Assert.assertEquals;
import humanize.text.util.Replacer;
import humanize.text.util.UnicodeInterpolator;

import org.testng.annotations.Test;

public class UnicodeInterpolatorTest
{

    @Test
    public void replaceTest()
    {
        UnicodeInterpolator interpol = new UnicodeInterpolator(createTestReplacer());
        interpol.addRange(0x20a0, 0x32ff);
        assertEquals(interpol.escape("♦♦ Alakazam 123 ♦♦"), "xx Alakazam 123 xx");
    }

    private Replacer createTestReplacer()
    {
        return new Replacer()
        {
            @Override
            public String replace(String replacement)
            {
                return "x";
            }
        };
    }

}
