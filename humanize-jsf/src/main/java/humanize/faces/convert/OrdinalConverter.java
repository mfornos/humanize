package humanize.faces.convert;

import humanize.Humanize;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "humanize.Ordinal")
public class OrdinalConverter extends NumberConverter
{

    private static final long serialVersionUID = -5459230181583940999L;

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value)
    {

        return Humanize.ordinal(asNumber(value), getLocale(context));

    }
}
