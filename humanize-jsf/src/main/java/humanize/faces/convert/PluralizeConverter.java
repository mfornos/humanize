package humanize.faces.convert;

import humanize.Humanize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "humanize.Pluralize")
public class PluralizeConverter extends BaseConverter
{

    private static final long serialVersionUID = 9005174100459127996L;

    private Object value;

    private String args;

    public String getArgs()
    {

        return args;

    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value)
    {

        return Humanize.pluralizeFormat(value.toString(), getLocale()).render(getArgsArray());

    }

    public Object getValue()
    {

        return value;

    }

    public void setArgs(String args)
    {

        clearInitialState();
        this.args = args;

    }

    public void setValue(Object value)
    {

        clearInitialState();
        this.value = value;

    }

    @Override
    protected void restore(Iterator<Object> iterator)
    {

        this.value = iterator.next();
        this.args = (String) iterator.next();

    }

    @Override
    protected void save(List<Object> states)
    {

        states.add(value);
        states.add(args);

    }

    private Object[] getArgsArray()
    {

        List<Object> tmpArr = new ArrayList<Object>();
        tmpArr.add(asNumber(value));
        if (args != null)
        {
            tmpArr.addAll(Arrays.asList(args.split("\\s*,\\s*")));
        }

        return tmpArr.toArray(new Object[tmpArr.size()]);

    }

}
