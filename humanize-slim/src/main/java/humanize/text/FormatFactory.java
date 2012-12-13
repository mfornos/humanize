package humanize.text;

import java.text.Format;
import java.util.Locale;

public interface FormatFactory {

	Format getFormat(String name, String args, Locale locale);
	
}
