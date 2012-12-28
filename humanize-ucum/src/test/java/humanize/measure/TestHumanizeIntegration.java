package humanize.measure;

import java.text.Format;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.measure.Measure;
import javax.measure.MeasureFormat;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import humanize.Humanize;
import humanize.spi.MessageFormat;
import humanize.text.FormatFactory;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestHumanizeIntegration {

	@Test
	public void testExplicitRegistration() {

		Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
		registry.put("measure", new FormatFactory() {

			@Override
			public Format getFormat(String name, String args, Locale locale) {

				return MeasureFormat.getInstance();

			}
		});

		MessageFormat mf = new MessageFormat("measure: {0, measure}", Locale.ENGLISH, registry);
		Assert.assertEquals(mf.render(Measure.valueOf(100, SI.GRAM.times(1000))), "measure: 100 kg");
		Assert.assertEquals(mf.render(Measure.valueOf(100, SI.KILOGRAM.times(1000))), "measure: 100 t");
		Assert.assertEquals(mf.render(Measure.valueOf(100, NonSI.MILES_PER_HOUR)), "measure: 100 mph");
		
	}

	@Test
	public void testRegistration() {

		MessageFormat mf = new MessageFormat("{1,number} weight: {0, measure}", Locale.ENGLISH);
		Assert.assertEquals(mf.render(Measure.valueOf(1000, SI.GRAM.times(1000)), 1), "1 weight: 1,000 kg");
		Assert.assertEquals(mf.render(Measure.valueOf(100, SI.KILOGRAM.times(1000)), 1), "1 weight: 100 t");

		Assert.assertEquals(Humanize.format("{0, measure}", Measure.valueOf(100, SI.GRAM.times(1000))), "100 kg");

		Assert.assertEquals(Humanize.format("{0, measure, standard}", Measure.valueOf(100, SI.GRAM.times(1000))), "100 kg");
		
		MessageFormat esFormat = Humanize.messageFormat("{0, measure}", new Locale("es"));
		Assert.assertEquals(esFormat.render(Measure.valueOf(1000, SI.GRAM.times(1000))), "1.000 kg");
		Assert.assertEquals(esFormat.render(Measure.valueOf(1000, NonSI.DAY_SIDEREAL)), "1.000 day_sidereal");

	}

}
