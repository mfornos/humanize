package humanize.spi;

import humanize.text.FormatFactory;

public interface FormatProvider {

	String getFormatName();

	FormatFactory getFactory();

}
