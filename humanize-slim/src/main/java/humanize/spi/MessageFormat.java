package humanize.spi;

import humanize.text.ExtendedMessageFormat;
import humanize.text.FormatFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Convenience methods to avoid explicit array creation for arguments.
 * 
 * @author mfornos
 * 
 */
public class MessageFormat extends ExtendedMessageFormat {

	private static final long serialVersionUID = -5384364921909539710L;

	private final static Map<String, FormatFactory> formatFactories = loadFormatFactories();

	private static Map<String, FormatFactory> loadFormatFactories() {

		Map<String, FormatFactory> factories = new HashMap<String, FormatFactory>();
		ServiceLoader<FormatProvider> ldr = ServiceLoader.load(FormatProvider.class);

		for (FormatProvider provider : ldr) {
			factories.put(provider.getFormatName(), provider.getFactory());
		}

		return factories;

	}

	public MessageFormat(String pattern) {

		super(pattern, formatFactories);

	}

	public MessageFormat(String pattern, Locale locale) {

		super(pattern, locale, formatFactories);

	}

	public MessageFormat(String pattern, Locale locale, Map<String, ? extends FormatFactory> registry) {

		super(pattern, locale, registry);

	}

	public MessageFormat(String pattern, Map<String, ? extends FormatFactory> registry) {

		super(pattern, registry);

	}

	public String render(Object... arguments) {

		return format(arguments);

	}

	public StringBuffer render(StringBuffer buffer, Object... arguments) {

		return format(arguments, buffer, null);

	}

}
