package humanize.config;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import com.google.common.base.Preconditions;

public class ConfigLoader
{

    private static final Properties DEFAULTS = new Properties();
    public static final String CACHE_BUILDER_SPEC = "cache.builder.spec";

    static
    {
        DEFAULTS.setProperty(CACHE_BUILDER_SPEC, "expireAfterAccess=1h");
    }

    public static Properties loadProperties()
    {

        String path = System.getProperty("humanize.config");
        return loadProperties(path == null ? "humanize.properties" : path);

    }

    public static Properties loadProperties(final String path)
    {

        Properties properties = new Properties(DEFAULTS);

        URL url = locateConfig(path);

        if (url != null)
        {
            try
            {
                URLConnection connection = url.openConnection();
                properties.load(connection.getInputStream());
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        return properties;

    }

    public static URL locateConfig(final String path)
    {

        URL url = asFile(path);
        if (url == null)
        {
            url = asURL(path);
        }
        if (url == null)
        {
            url = asResource(path);
        }
        return url;

    }

    private static URL asFile(final String path)
    {
        URL url = null;

        File file = new File(path);

        if (file.exists())
        {
            try
            {
                url = file.toURI().toURL();
            } catch (MalformedURLException e)
            {
                //
            }
        }

        return url;

    }

    private static final URL asResource(final String path)
    {

        URL url = null;

        try
        {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if (contextClassLoader != null)
            {
                url = contextClassLoader.getResource(path);
            }
            if (url == null)
            {
                ClassLoader classLoader = ConfigLoader.class.getClassLoader();
                Preconditions.checkNotNull(classLoader, "Class Loader Not Found! :(");
                url = classLoader.getResource(path);
            }
        } catch (Exception e)
        {
            // assume not found
        }

        return url;

    }

    private static URL asURL(final String path)
    {

        URL url = null;

        try
        {
            url = new URL(path);
        } catch (MalformedURLException e)
        {
            //
        }

        return url;

    }

}
