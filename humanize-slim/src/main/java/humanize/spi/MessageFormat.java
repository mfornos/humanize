package humanize.spi;

import humanize.text.ExtendedMessageFormat;
import humanize.text.FormatFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * {@link ExtendedMessageFormat} wrapper.
 * 
 */
public class MessageFormat extends ExtendedMessageFormat
{

    private static final long serialVersionUID = -5384364921909539710L;

    private final static Map<String, FormatFactory> formatFactories = loadFormatFactories();

    private static Map<String, FormatFactory> loadFormatFactories()
    {

        Map<String, FormatFactory> factories = new HashMap<String, FormatFactory>();
        ServiceLoader<FormatProvider> ldr = ServiceLoader.load(FormatProvider.class);

        for (FormatProvider provider : ldr)
        {
            registerProvider(factories, provider);
        }

        return factories;

    }

    private static void registerProvider(Map<String, FormatFactory> factories, FormatProvider provider)
    {

        String formatName = provider.getFormatName();
        FormatFactory factory = provider.getFactory();

        if (formatName.indexOf('|') > -1)
        {
            String[] names = formatName.split("\\|");
            for (String name : names)
            {
                factories.put(name, factory);
            }
        } else
        {
            factories.put(formatName, factory);
        }

    }

    public MessageFormat(String pattern)
    {

        super(pattern, formatFactories);

    }

    public MessageFormat(String pattern, Locale locale)
    {

        super(pattern, locale, formatFactories);

    }

    public MessageFormat(String pattern, Locale locale, Map<String, ? extends FormatFactory> registry)
    {

        super(pattern, locale, registry);

    }

    public MessageFormat(String pattern, Map<String, ? extends FormatFactory> registry)
    {

        super(pattern, registry);

    }

    /**
     * Formats the current pattern with the given arguments.
     * 
     * @param arguments
     *            The formatting arguments
     * @return Formatted message
     */
    public String render(Object... arguments)
    {

        return format(arguments);

    }

    /**
     * Formats the current pattern with the given arguments.
     * 
     * @param buffer
     *            The StringBuffer
     * @param arguments
     *            The formatting arguments
     * @return StringBuffer with the formatted message
     */
    public StringBuffer render(StringBuffer buffer, Object... arguments)
    {

        return format(arguments, buffer, null);

    }

}
