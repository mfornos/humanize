package humanize.time;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ocpsoft.prettytime.Duration;
import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.TimeUnit;
import org.ocpsoft.prettytime.impl.DurationImpl;

import com.google.common.base.Preconditions;

/**
 * Helper to create {@link Duration}s outside {@link PrettyTime}.
 * 
 */
public class DurationHelper
{

    public static Duration calculateDuration(Date ref, Date then, List<TimeUnit> timeUnits)
    {
        return calculateDuration(then.getTime() - ref.getTime(), timeUnits);
    }

    public static Duration calculateDuration(long difference, List<TimeUnit> timeUnits)
    {
        long absoluteDifference = Math.abs(difference);

        List<TimeUnit> units = new ArrayList<TimeUnit>(timeUnits);

        DurationImpl result = new DurationImpl();

        for (int i = 0; i < units.size(); ++i)
        {
            TimeUnit unit = (TimeUnit) units.get(i);
            long millisPerUnit = Math.abs(unit.getMillisPerUnit());
            long quantity = Math.abs(unit.getMaxQuantity());

            boolean isLastUnit = i == units.size() - 1;

            if ((0L == quantity) && (!(isLastUnit)))
            {
                quantity = ((TimeUnit) units.get(i + 1)).getMillisPerUnit() / unit.getMillisPerUnit();
            }

            if ((millisPerUnit * quantity <= absoluteDifference) && (!(isLastUnit)))
                continue;
            result.setUnit(unit);
            if (millisPerUnit > absoluteDifference)
            {
                result.setQuantity(0L > difference ? -1L : 1L);
            } else
            {
                result.setQuantity(difference / millisPerUnit);
            }
            result.setDelta(difference - (result.getQuantity() * millisPerUnit));
            break;
        }

        return result;
    }

    public static List<Duration> calculatePreciseDuration(Date ref, Date then, List<TimeUnit> units)
    {
        Preconditions.checkNotNull(then, "Date to calculate must not be null.");

        if (null == ref)
        {
            ref = new Date();
        }

        List<Duration> result = new ArrayList<Duration>();
        long difference = then.getTime() - ref.getTime();
        Duration duration = calculateDuration(difference, units);
        result.add(duration);
        while (0L != duration.getDelta())
        {
            duration = calculateDuration(duration.getDelta(), units);
            result.add(duration);
        }
        return result;
    }

}
