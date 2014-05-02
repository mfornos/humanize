package humanize.time;

import com.google.common.base.Objects;

/**
 * Holds the properties required to produce human friendly textual
 * representations of pace quantities.
 * 
 */
public class Pace
{

    public enum Accuracy
    {
        NONE, APROX, LESS_THAN
    }

    private final long value;
    private final String accuracy;
    private final String timeUnit;

    public static final Pace EMPTY = new Pace(0, Accuracy.NONE, TimeMillis.SECOND);

    public Pace(long value, Accuracy accuracy, TimeMillis timeUnit)
    {
        this.value = value;
        this.accuracy = accuracy.name().toLowerCase();
        this.timeUnit = timeUnit.key();
    }

    public String getAccuracy()
    {
        return accuracy;
    }

    public String getTimeUnit()
    {
        return timeUnit;
    }

    public long getValue()
    {
        return value;
    }

    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("value", value)
                .add("accuracy", accuracy)
                .add("timeUnit", timeUnit)
                .toString();
    }

}
