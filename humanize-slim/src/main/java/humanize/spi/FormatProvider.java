package humanize.spi;

import humanize.text.ExtendedMessageFormat;
import humanize.text.FormatFactory;

import java.text.Format;

/**
 * Contract for contributing a {@link Format} implementation to the
 * {@link ExtendedMessageFormat} registry.
 * 
 */
public interface FormatProvider
{

    /**
     * Gets the format factory.
     * 
     * @return a {@link FormatFactory} instance
     */
    FormatFactory getFactory();

    /**
     * Gets the format name that will be registered. If you want to register
     * multiple names for a format then return a String with the names
     * concatenated by a vertical bar character '|'.
     * 
     * @return the format name
     */
    String getFormatName();

}
