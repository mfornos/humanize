package humanize.taglibs;

import humanize.Humanize;
import humanize.util.Constants.TimeStyle;

import java.util.Locale;

import javax.servlet.jsp.JspException;

import org.apache.taglibs.standard.tag.common.fmt.NumberCallSupport;

public class DurationTag extends NumberCallSupport
{

    private static final long serialVersionUID = 1003775990876323736L;

    private String style;

    private TimeStyle timeStyle;

    public void setStyle(String style)
    {

        this.style = style;

    }

    @Override
    protected void begin() throws JspException
    {

        super.begin();

        timeStyle = style == null ? TimeStyle.STANDARD : TimeStyle.valueOf(style.toUpperCase());

    }

    @Override
    protected void clean()
    {

        super.clean();

        this.style = null;
        this.timeStyle = null;

    }

    @Override
    protected String render() throws JspException
    {

        return Humanize.duration(input, timeStyle);

    }

    @Override
    protected String render(Locale locale) throws JspException
    {

        return Humanize.duration(input, timeStyle, locale);

    }

}
