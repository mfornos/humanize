package humanize.taglibs;

import humanize.Humanize;

import java.util.Locale;

import javax.servlet.jsp.JspException;

import org.apache.taglibs.standard.tag.common.fmt.NumberCallSupport;

public class SpellDigitTag extends NumberCallSupport
{

	private static final long serialVersionUID = -3650935972940093910L;

	@Override
	protected String render() throws JspException
	{

		return Humanize.spellDigit(input);

	}

	@Override
	protected String render(Locale locale) throws JspException
	{

		return Humanize.spellDigit(input, locale);

	}

}
