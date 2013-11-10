package humanize.taglibs;

import static humanize.taglibs.util.Convert.asNumber;
import humanize.Humanize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.servlet.jsp.JspException;

import org.apache.taglibs.standard.tag.common.fmt.HumanizeSupport;

public class PluralizeTag extends HumanizeSupport
{

    private static final long serialVersionUID = -8952205534935694612L;

    private String none;

    private String one;

    private String many;

    private Object value;

    private String args;

    private Number num;

    private Object[] argsArray;

    public void setArgs(String args)
    {

        this.args = args;
    }

    public void setMany(String many)
    {

        this.many = many;
    }

    public void setNone(String none)
    {

        this.none = none;
    }

    public void setOne(String one)
    {

        this.one = one;
    }

    public void setValue(String value)
    {

        this.value = value;

    }

    @Override
    protected void begin() throws JspException
    {

        num = asNumber(value);

        List<Object> tmpArr = new ArrayList<Object>();
        if (args != null)
        {
            tmpArr.addAll(Arrays.asList(args.split("\\s*,\\s*")));
        }

        this.argsArray = tmpArr.toArray(new Object[tmpArr.size()]);

    }

    protected void clean()
    {

        this.value = null;
        this.none = null;
        this.one = null;
        this.many = null;
        this.num = null;

    }

    @Override
    protected boolean isContextRemoveNeeded()
    {

        return value == null;

    }

    @Override
    protected String render() throws JspException
    {

        return isEmpty(none) ? Humanize.pluralize(one, many, num, argsArray) : Humanize.pluralize(one, many, none, num,
                argsArray);

    }

    @Override
    protected String render(Locale locale) throws JspException
    {

        return isEmpty(none) ? Humanize.pluralize(locale, one, many, num, argsArray) : Humanize.pluralize(locale, one,
                many, none, num, argsArray);

    }

}
