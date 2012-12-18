package humanize.time;

import humanize.spi.FormatProvider;
import humanize.text.FormatFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.ocpsoft.prettytime.Duration;
import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.TimeFormat;
import org.ocpsoft.prettytime.TimeUnit;

/**
 * {@link Format} implementation for {@link PrettyTime}.
 * 
 */
public class PrettyTimeFormat extends Format implements FormatProvider {

	private static final long serialVersionUID = -1398312177396430967L;

	public static FormatFactory factory() {

		return new FormatFactory() {
			@Override
			public Format getFormat(String name, String args, Locale locale) {

				// TODO support unrounded in args?
				return new PrettyTimeFormat(locale);

			}
		};

	}

	public static PrettyTimeFormat getInstance() {

		return getInstance(Locale.getDefault());

	}

	public static PrettyTimeFormat getInstance(Locale locale) {

		return new PrettyTimeFormat(locale);

	}

	private transient PrettyTime prettyTime;

	private final Locale locale;

	public PrettyTimeFormat() {

		this(Locale.getDefault());

	}

	public PrettyTimeFormat(Locale locale) {

		this.prettyTime = new PrettyTime(locale);
		this.locale = locale;

	}

	public Duration approximateDuration(Date then) {

		return prettyTime.approximateDuration(then);
	}

	public List<Duration> calculatePreciseDuration(Date then) {

		return prettyTime.calculatePreciseDuration(then);
	}

	public List<TimeUnit> clearUnits() {

		return prettyTime.clearUnits();
	}

	/**
	 * Convenience format method.
	 * 
	 * @param then
	 *            The future date.
	 * @return a relative format date as text representation
	 */
	public String format(Date then) {

		return prettyTime.format(DurationHelper.calculateDurantion(new Date(), then, prettyTime.getUnits()));

	}

	/**
	 * Convenience format method.
	 * 
	 * @param ref
	 *            The date of reference.
	 * @param then
	 *            The future date.
	 * @return a relative format date as text representation
	 */
	public String format(Date ref, Date then) {

		return prettyTime.format(DurationHelper.calculateDurantion(ref, then, prettyTime.getUnits()));

	}

	public String format(Duration duration) {

		return prettyTime.format(duration);
	}

	public String format(List<Duration> durations) {

		return prettyTime.format(durations);
	}

	@Override
	@SuppressWarnings("unchecked")
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {

		if (Duration.class.isAssignableFrom(obj.getClass())) {
			return toAppendTo.append(prettyTime.format((Duration) obj));
		}

		if (Date.class.isAssignableFrom(obj.getClass())) {
			return toAppendTo.append(prettyTime.format((Date) obj));
		}

		if (List.class.isAssignableFrom(obj.getClass())) {
			return toAppendTo.append(prettyTime.format((List<Duration>) obj));
		}

		throw new IllegalArgumentException(String.format("Class %s is not suitable for PrettyTimeFormat",
		        obj.getClass()));

	}

	public String formatUnrounded(Date then) {

		return prettyTime.formatUnrounded(then);
	}

	public String formatUnrounded(Duration duration) {

		return prettyTime.formatUnrounded(duration);
	}

	@Override
	public FormatFactory getFactory() {

		return factory();

	}

	public TimeFormat getFormat(TimeUnit unit) {

		return prettyTime.getFormat(unit);
	}

	@Override
	public String getFormatName() {

		return "prettytime";

	}

	/**
	 * Gets the underlying {@link PrettyTime} instance.
	 * 
	 * @return the underlying {@link PrettyTime} instance.
	 */
	public PrettyTime getPrettyTime() {

		return prettyTime;

	}

	public List<TimeUnit> getUnits() {

		return prettyTime.getUnits();
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {

		throw new UnsupportedOperationException();

	}

	public PrettyTime registerUnit(TimeUnit unit, TimeFormat format) {

		return prettyTime.registerUnit(unit, format);
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {

		ois.defaultReadObject();
		this.prettyTime = new PrettyTime(locale);

	}

}
