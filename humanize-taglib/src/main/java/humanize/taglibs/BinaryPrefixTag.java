package humanize.taglibs;

import humanize.Humanize;

import java.util.Locale;

import javax.servlet.jsp.JspException;

import org.apache.taglibs.standard.tag.common.fmt.NumberCallSupport;

public class BinaryPrefixTag extends NumberCallSupport
{

    private static final long serialVersionUID = -6504638950550719198L;

    @Override
    protected String render() throws JspException
    {

        return Humanize.binaryPrefix(input);

    }

    @Override
    protected String render(Locale locale) throws JspException
    {

        return Humanize.binaryPrefix(input, locale);

    }

}
