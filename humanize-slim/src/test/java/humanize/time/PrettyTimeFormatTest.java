package humanize.time;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Date;
import java.util.Locale;

import org.ocpsoft.prettytime.Duration;
import org.testng.annotations.Test;

public class PrettyTimeFormatTest
{
    @Test
    public void basicTest()
    {
        PrettyTimeFormat fmt = new PrettyTimeFormat(Locale.ENGLISH);
        assertEquals(fmt.getFormatName(), "prettytime");

        assertEquals(fmt.format(new Date(1000), new Date(10000)), "moments from now");

        assertNotNull(fmt.format(10000));

        Duration duration = DurationHelper.calculateDuration(new Date(0), new Date(10000), fmt.getUnits());
        assertEquals(fmt.format(duration), "moments from now");
    }
}
