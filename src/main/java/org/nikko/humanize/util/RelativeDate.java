package org.nikko.humanize.util;

import static org.nikko.humanize.util.Constants.EMPTY_STRING;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.ResourceBundle;

import org.nikko.humanize.spi.Message;
import org.nikko.humanize.spi.context.Context;

public class RelativeDate {

	public static RelativeDate getInstance(Context context) {
		return new RelativeDate(context);
	}

	private final Locale locale;

	private final ResourceBundle messages;

	private final Message format;

	private static final String[] units = new String[] { "year", "month", "week", "day", "hour", "minute", "second" };

	private RelativeDate(Context context) {
		this.locale = context.getLocale();
		this.messages = context.getBundle();
		this.format = new Message(EMPTY_STRING, locale);
	}

	/**
	 * This method returns a String representing the relative date by comparing
	 * duration date to reference date.
	 * 
	 * @param Date
	 *            reference date
	 * @param Date
	 *            duration from reference date
	 * @return String representing the relative date
	 */
	public String format(Calendar reference, Calendar duration) {
		int[] deltas = new int[units.length];
		int i = 0;

		deltas[i++] = duration.get(Calendar.YEAR) - reference.get(Calendar.YEAR);
		deltas[i++] = duration.get(Calendar.MONTH) - reference.get(Calendar.MONTH);
		deltas[i++] = duration.get(Calendar.WEEK_OF_MONTH) - reference.get(Calendar.WEEK_OF_MONTH);
		deltas[i++] = duration.get(Calendar.DAY_OF_MONTH) - reference.get(Calendar.DAY_OF_MONTH);
		deltas[i++] = duration.get(Calendar.HOUR_OF_DAY) - reference.get(Calendar.HOUR_OF_DAY);
		deltas[i++] = duration.get(Calendar.MINUTE) - reference.get(Calendar.MINUTE);
		deltas[i++] = duration.get(Calendar.SECOND) - reference.get(Calendar.SECOND);

		return computeRelativeDate(deltas);
	}

	/**
	 * This method returns a String representing the relative date by comparing
	 * the given Date to the current date.
	 * 
	 * @param Date
	 *            duration from now
	 * @return String representing the relative date
	 */
	public String format(Date duration) {
		return format(GregorianCalendar.getInstance(locale), getCalendar(duration));
	}

	public String format(Date reference, Date duration) {
		return format(getCalendar(reference), getCalendar(duration));
	}

	/**
	 * Computes both past and future relative dates. E.g. "one day ago" and
	 * "one day from now".
	 * 
	 * @param []int ordered array of deltas
	 * @return String representing the relative date
	 */
	private String computeRelativeDate(int[] deltas) {

		StringBuilder buffer = new StringBuilder();

		for (int i = 0; i < units.length; i++) {
			if (matchUnit(units[i], deltas[i], buffer))
				return buffer.toString();
		}

		return messages.getString("now");
	}

	private String formatMessage(String key, Object... args) {
		format.applyPattern(messages.getString(key));
		return format.render(args);
	}

	private Calendar getCalendar(Date date) {
		Calendar cal = GregorianCalendar.getInstance(locale);
		cal.setTime(date);
		return cal;
	}

	private boolean matchUnit(String unit, int delta, StringBuilder buffer) {
		if (delta == 1)
			buffer.append(messages.getString(unit + ".one.from"));
		else if (delta == -1)
			buffer.append(messages.getString(unit + ".one.ago"));
		else if (delta > 0)
			buffer.append(formatMessage(unit + ".many.from", delta));
		else if (delta < 0)
			buffer.append(formatMessage(unit + ".many.ago", Math.abs(delta)));

		return buffer.length() > 0;
	}
}
