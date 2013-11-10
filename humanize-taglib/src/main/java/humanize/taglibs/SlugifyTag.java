package humanize.taglibs;

import humanize.Humanize;

import java.util.Locale;

import javax.servlet.jsp.JspException;

import org.apache.taglibs.standard.tag.common.fmt.HumanizeSupport;

public class SlugifyTag extends HumanizeSupport
{

    private static final long serialVersionUID = 1259741260968346374L;

    private String value;

    private String input;

    public void setValue(String value)
    {

        this.value = value;

    }

    @Override
    protected void begin() throws JspException
    {

        this.input = value == null || value.length() < 1 ? inputFromBody() : value;

    }

    protected void clean()
    {

        this.value = null;
        this.input = null;
        this.resolveLocale = false;

    }

    @Override
    protected boolean isContextRemoveNeeded()
    {

        return input == null || input.length() < 1;

    }

    @Override
    protected String render() throws JspException
    {

        return Humanize.slugify(input);

    }

    @Override
    protected String render(Locale locale) throws JspException
    {

        return render();

    }

}
