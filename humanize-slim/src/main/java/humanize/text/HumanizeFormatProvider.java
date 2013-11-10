package humanize.text;

import humanize.Humanize;
import humanize.spi.Expose;
import humanize.spi.FormatProvider;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.google.common.base.Preconditions;

/**
 * <p>
 * Exposes as {@link Format}s all the methods annotated with {@link Expose} of
 * {@link Humanize} class.
 * </p>
 * 
 * Example:
 * 
 * <pre>
 * Humanize.format(&quot;size {0, humanize, binary.prefix}&quot;, 8);
 * // == &quot;size 8 bytes&quot;
 * </pre>
 */
public class HumanizeFormatProvider implements FormatProvider
{

	public static class HumanizeFormat extends Format
	{

		private static final long serialVersionUID = -3261072590121741805L;

		private final Locale locale;

		private final Method method;

		public HumanizeFormat(Method method, Locale locale)
		{

			this.method = method;
			this.locale = locale;

		}

		@Override
		public StringBuffer format(Object paramObject, StringBuffer toAppendTo, FieldPosition position)
		{

			Preconditions.checkNotNull(method);

			Class<?>[] paramTypes = method.getParameterTypes();
			boolean withLocale = false;
			Object retval = null;

			for (Class<?> type : paramTypes)
			{
				if (Locale.class.equals(type))
				{
					withLocale = true;
					break;
				}
			}

			try
			{

				retval = withLocale ? method.invoke(null, paramObject, locale) : method.invoke(null, paramObject);

			} catch (Exception e)
			{

				retval = String.format("[invalid call: '%s']", e.getMessage());

			}

			return toAppendTo.append(retval);
		}

		@Override
		public Object parseObject(String paramString, ParsePosition paramParsePosition)
		{

			throw new UnsupportedOperationException();

		}

	}

	private static final Map<String, Method> humanizeMethods = getStaticMethods(Humanize.class);

	public static FormatFactory factory()
	{

		return new FormatFactory()
		{
			@Override
			public Format getFormat(String name, String args, Locale locale)
			{

				String camelized = Humanize.camelize(args);
				if (humanizeMethods.containsKey(camelized))
				{
					Method method = humanizeMethods.get(camelized);
					return new HumanizeFormat(method, locale);
				}

				// not found
				return null;
			}
		};

	}

	private static Map<String, Method> getStaticMethods(Class<?> clazz)
	{

		Map<String, Method> methods = new HashMap<String, Method>();

		for (Method method : clazz.getMethods())
		{
			if (Modifier.isStatic(method.getModifiers()) && method.getAnnotation(Expose.class) != null)
			{
				methods.put(method.getName(), method);
			}
		}

		return Collections.unmodifiableMap(methods);

	}

	@Override
	public FormatFactory getFactory()
	{

		return factory();

	}

	@Override
	public String getFormatName()
	{

		return "humanize";

	}

}
