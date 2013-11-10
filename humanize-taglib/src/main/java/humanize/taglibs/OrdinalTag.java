package humanize.taglibs;

import humanize.Humanize;

import java.util.Locale;

import javax.servlet.jsp.JspException;

import org.apache.taglibs.standard.tag.common.fmt.NumberCallSupport;

public class OrdinalTag extends NumberCallSupport
{

    private static final long serialVersionUID = -808918041951271999L;

    @Override
    protected String render() throws JspException
    {

        return Humanize.ordinal(input);

    }

    @Override
    protected String render(Locale locale) throws JspException
    {

        return Humanize.ordinal(input, locale);

    }

}
