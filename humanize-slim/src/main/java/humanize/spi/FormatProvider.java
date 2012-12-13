package humanize.spi;

import humanize.text.ExtendedMessageFormat;
import humanize.text.FormatFactory;

/**
 * Contract for contributing a {@link Format} implementation to the
 * {@link ExtendedMessageFormat} registry.
 * 
 */
public interface FormatProvider {

	String getFormatName();

	FormatFactory getFactory();

}
