package humanize.taglibs.util;

import javax.servlet.jsp.JspException;

import org.apache.taglibs.standard.resources.Resources;

public final class Convert
{
	public static Number asNumber(Object input) throws JspException
	{

		if (input instanceof String)
		{
			try
			{
				String istr = (String) input;
				return (istr.indexOf('.') != -1) ? Double.valueOf(istr) : Long.valueOf(istr);
			} catch (NumberFormatException nfe)
			{
				throw new JspException(Resources.getMessage("FORMAT_NUMBER_PARSE_ERROR", input), nfe);
			}
		}

		return (Number) input;

	}
}
