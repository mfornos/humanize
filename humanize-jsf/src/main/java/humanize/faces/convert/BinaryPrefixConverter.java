package humanize.faces.convert;

import humanize.Humanize;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "humanize.BinaryPrefix")
public class BinaryPrefixConverter extends NumberConverter
{

    private static final long serialVersionUID = 1942567170903039630L;

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value)
    {

        return Humanize.binaryPrefix(asNumber(value), getLocale(context));

    }

}
