package humanize.spi;

import humanize.text.ExtendedMessageFormat;
import humanize.text.FormatFactory;

/**
 * Contract for contributing a {@link Format} implementation to the
 * {@link ExtendedMessageFormat} registry.
 * 
 */
public interface FormatProvider {

	/**
	 * @return a {@link FormatFactory} instance
	 */
	FormatFactory getFactory();

	/**
	 * @return the format name
	 */
	String getFormatName();

}
