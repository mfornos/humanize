package humanize.text;

import humanize.Humanize;
import humanize.spi.Export;
import humanize.spi.FormatProvider;

import java.lang.reflect.InvocationTargetException;
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

public class HumanizeFormat extends Format implements FormatProvider {

	private static final long serialVersionUID = 4144220416351621568L;

	private static final Map<String, Method> humanizeMethods = getStaticMethods(Humanize.class);

	private Locale locale;

	private Method method;

	public HumanizeFormat(Method method, Locale locale) {

		this.method = method;
		this.locale = locale;

	}

	/**
	 * Returns the public static methods of a class or interface, including
	 * those declared in super classes and interfaces.
	 */
	private static Map<String, Method> getStaticMethods(Class<?> clazz) {

		Map<String, Method> methods = new HashMap<String, Method>();

		for (Method method : clazz.getMethods()) {
			if (Modifier.isStatic(method.getModifiers()) && method.getAnnotation(Export.class) != null) {
				methods.put(method.getName(), method);
			}
		}

		return Collections.unmodifiableMap(methods);

	}

	@Override
	public String getFormatName() {

		return "humanize";

	}

	@Override
	public FormatFactory getFactory() {

		return new FormatFactory() {
			@Override
			public Format getFormat(String name, String args, Locale locale) {

				if (humanizeMethods.containsKey(args)) {
					Method method = humanizeMethods.get(args);
					return new HumanizeFormat(method, locale);
				}

				// not found
				return null;
			}
		};

	}

	public HumanizeFormat() {

		//
	}

	@Override
	public StringBuffer format(Object paramObject, StringBuffer toAppendTo, FieldPosition position) {

		Preconditions.checkNotNull(method);

		Class<?>[] paramTypes = method.getParameterTypes();
		boolean withLocale = false;
		Object retval = null;

		for (Class<?> type : paramTypes) {
			if (Locale.class.equals(type)) {
				withLocale = true;
				break;
			}
		}
		
		try {
			
			retval = withLocale ? method.invoke(null, paramObject, locale) : method.invoke(null, paramObject);

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return toAppendTo.append(retval);
	}

	@Override
	public Object parseObject(String paramString, ParsePosition paramParsePosition) {

		throw new UnsupportedOperationException();

	}

}
