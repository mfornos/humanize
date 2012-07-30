package humanize.spi.cache;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

/**
 * Facade to access resource caches. Includes string arrays, bundles and formats
 * by locale.
 * 
 * @author mfornos
 * 
 */
public interface CacheProvider {

	boolean containsBundle(Locale locale);

	boolean containsFormat(String cache, Locale locale);

	boolean containsStrings(String cache, Locale locale);

	ResourceBundle getBundle(Locale locale, Callable<ResourceBundle> getCall);

	<T> T getFormat(String cache, Locale locale, Callable<T> getCall);

	String[] getStrings(String cache, Locale locale, Callable<String[]> getCall);

	ResourceBundle putBundle(Locale locale, ResourceBundle bundle);

	<T> T putFormat(String cache, Locale locale, T format);

	String[] putStrings(String cache, Locale locale, String[] split);

}
