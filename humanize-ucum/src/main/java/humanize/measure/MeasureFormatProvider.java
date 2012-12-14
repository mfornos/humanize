package humanize.measure;

import humanize.spi.FormatProvider;
import humanize.text.FormatFactory;

import java.text.Format;
import java.text.NumberFormat;
import java.util.Locale;

import javax.measure.MeasureFormat;
import javax.measure.unit.UnitFormat;

/**
 * <p>
 * Provides a JSR-275 measure formatter. Supports "standard" sub-format.
 * </p>
 * 
 * Examples:
 * 
 * <pre>
 * 
 * // == &quot;100 kg&quot;
 * Humanize.format(&quot;{0, measure}&quot;, Measure.valueOf(100, SI.GRAM.times(1000)));
 * 
 * Humanize.format(&quot;{0, measure, standard}&quot;, measure);
 * 
 * // With a locale
 * 
 * MessageFormat esFormat = Humanize.messageFormatInstance(&quot;{0, measure}&quot;, new Locale(&quot;es&quot;));
 * esFormat.render(Measure.valueOf(1000, SI.GRAM.times(1000)));
 * 
 * </pre>
 * 
 */
public class MeasureFormatProvider implements FormatProvider {

	@Override
	public FormatFactory getFactory() {

		return new FormatFactory() {
			@Override
			public Format getFormat(String name, String args, Locale locale) {

				if (args != null && "standard".equalsIgnoreCase(args)) {
					return MeasureFormat.getStandard();
				} else {
					return MeasureFormat.getInstance(NumberFormat.getInstance(locale), UnitFormat.getInstance(locale));
				}

			}
		};

	}

	@Override
	public String getFormatName() {

		return "measure";

	}

}
