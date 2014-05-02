package humanize.time;

public enum TimeMillis
{
    SECOND(1000L),
    MINUTE(60000L),
    HOUR(3600000L),
    DAY(86400000L),
    WEEK(604800000L),
    // ideal months
    MONTH(2628000000L);

    private long millis;

    TimeMillis(long millis)
    {
        this.millis = millis;
    }

    public String key()
    {
        return this.name().toLowerCase();
    }

    public long millis()
    {
        return this.millis;
    }
}