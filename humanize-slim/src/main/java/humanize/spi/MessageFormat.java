package humanize.spi;

import java.util.Locale;

/**
 * Adds syntactic sugar methods to avoid explicit array creation for arguments.
 * 
 * @author mfornos
 * 
 */
public class MessageFormat extends java.text.MessageFormat {

	private static final long serialVersionUID = -5384364921909539710L;

	public MessageFormat(String pattern) {

		super(pattern);

	}

	public MessageFormat(String pattern, Locale locale) {

		super(pattern, locale);

	}

	public String render(Object... arguments) {

		return format(arguments);

	}

	public StringBuffer render(StringBuffer buffer, Object... arguments) {

		return format(arguments, buffer, null);

	}

}
