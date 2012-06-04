package org.nikko.humanize.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

/**
 * Custom ResourceBundle.Control with UTF8 support.
 * 
 * <br />
 * 
 * Slightly modified from:
 * http://stackoverflow.com/questions/4659929/how-to-use-utf-8-in-resource-
 * properties-with-resourcebundle
 * 
 * @author balusc / balusc.blogspot.com
 * 
 */
public class UTF8Control extends Control {
	
	public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
	        throws IllegalAccessException, InstantiationException, IOException {
		// The below is a copy of the default implementation.
		String bundleName = toBundleName(baseName, locale);
		String resourceName = toResourceName(bundleName, "properties");
		ResourceBundle bundle = null;
		InputStream stream = (reload) ? reload(loader.getResource(resourceName)) : loader
		        .getResourceAsStream(resourceName);

		if (stream != null) {
			try {
				// Only this line is changed to make it to read properties files
				// as UTF-8.
				bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
			} finally {
				stream.close();
			}
		}
		return bundle;
	}

	private InputStream reload(URL url) throws IOException {
		if (url != null) {
			URLConnection connection = url.openConnection();
			if (connection != null) {
				connection.setUseCaches(false);
				return connection.getInputStream();
			}
		}
		return null;
	}
	
}
