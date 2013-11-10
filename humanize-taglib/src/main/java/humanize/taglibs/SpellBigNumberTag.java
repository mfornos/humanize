package humanize.taglibs;

import humanize.Humanize;

import java.util.Locale;

import javax.servlet.jsp.JspException;

import org.apache.taglibs.standard.tag.common.fmt.NumberCallSupport;

public class SpellBigNumberTag extends NumberCallSupport
{

    private static final long serialVersionUID = 7175958375643210374L;

    @Override
    protected String render() throws JspException
    {

        return Humanize.spellBigNumber(input);

    }

    @Override
    protected String render(Locale locale) throws JspException
    {

        return Humanize.spellBigNumber(input, locale);

    }

}
