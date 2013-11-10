package humanize.text;

import humanize.spi.MessageFormat;

import java.text.Format;
import java.util.Locale;

/**
 * Format factory interface.
 * 
 */
public interface FormatFactory
{

	/**
	 * Creates a {@link Format} instance for the given parameters. The format
	 * will be accessible as '{0, name, args}'.
	 * 
	 * @param name
	 *            The format name
	 * @param args
	 *            The format arguments
	 * @param locale
	 *            Target locale
	 * @return a new {@link Format} instance
	 * @see MessageFormat
	 */
	Format getFormat(String name, String args, Locale locale);

}
