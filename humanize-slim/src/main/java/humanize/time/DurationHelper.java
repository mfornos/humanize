package humanize.time;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.ocpsoft.prettytime.Duration;
import org.ocpsoft.prettytime.LocaleAware;
import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.TimeFormat;
import org.ocpsoft.prettytime.TimeUnit;
import org.ocpsoft.prettytime.impl.DurationImpl;
import org.ocpsoft.prettytime.impl.ResourcesTimeFormat;
import org.ocpsoft.prettytime.impl.ResourcesTimeUnit;
import org.ocpsoft.prettytime.units.Century;
import org.ocpsoft.prettytime.units.Day;
import org.ocpsoft.prettytime.units.Decade;
import org.ocpsoft.prettytime.units.Hour;
import org.ocpsoft.prettytime.units.JustNow;
import org.ocpsoft.prettytime.units.Millennium;
import org.ocpsoft.prettytime.units.Millisecond;
import org.ocpsoft.prettytime.units.Minute;
import org.ocpsoft.prettytime.units.Month;
import org.ocpsoft.prettytime.units.Second;
import org.ocpsoft.prettytime.units.TimeUnitComparator;
import org.ocpsoft.prettytime.units.Week;
import org.ocpsoft.prettytime.units.Year;

import com.google.common.base.Preconditions;

/**
 * Helper to create {@link Duration}s outside {@link PrettyTime}.
 * 
 */
public class DurationHelper
{

	public static class DurationDB
	{

		// TODO reuse PrettyTime cached instances from DefaultContext?
		private final Map<TimeUnit, TimeFormat> units = new LinkedHashMap<TimeUnit, TimeFormat>();

		private final Locale locale;

		public DurationDB(Locale locale)
		{

			this.locale = locale;
			addUnit(new JustNow());
			addUnit(new Millisecond());
			addUnit(new Second());
			addUnit(new Minute());
			addUnit(new Hour());
			addUnit(new Day());
			addUnit(new Week());
			addUnit(new Month());
			addUnit(new Year());
			addUnit(new Decade());
			addUnit(new Century());
			addUnit(new Millennium());

		}

		public void addUnit(ResourcesTimeUnit unit)
		{

			registerUnit(unit, new ResourcesTimeFormat(unit));

		}

		public List<TimeUnit> clearUnits()
		{

			List<TimeUnit> result = getUnits();
			this.units.clear();
			return result;
		}

		public List<TimeUnit> getUnits()
		{

			List<TimeUnit> result = new ArrayList<TimeUnit>(this.units.keySet());
			Collections.sort(result, new TimeUnitComparator());
			return Collections.unmodifiableList(result);

		}

		public void registerUnit(TimeUnit unit, TimeFormat format)
		{

			Preconditions.checkArgument(unit != null, "Unit to register must not be null.");
			Preconditions.checkArgument(format != null, "Format to register must not be null.");

			units.put(unit, format);

			if (unit instanceof LocaleAware)
				((LocaleAware<?>) unit).setLocale(this.locale);
			if (format instanceof LocaleAware)
				((LocaleAware<?>) format).setLocale(this.locale);

		}

	}

	private static final Map<Locale, DurationDB> dbs = new HashMap<Locale, DurationDB>();

	public static Duration calculateDurantion(Date ref, Date then)
	{

		return calculateDurantion(ref, then, Locale.getDefault());

	}

	public static Duration calculateDurantion(Date ref, Date then, List<TimeUnit> timeUnits)
	{

		long difference = then.getTime() - ref.getTime();
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

	public static Duration calculateDurantion(Date ref, Date then, Locale locale)
	{

		return calculateDurantion(ref, then, getDB(locale).getUnits());

	}

	public static DurationDB getDB(Locale locale)
	{

		if (!dbs.containsKey(locale))
		{
			synchronized (dbs)
			{
				dbs.put(locale, new DurationDB(locale));
			}
		}
		return dbs.get(locale);

	}

}
