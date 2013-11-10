package humanize.faces.convert;

import humanize.Humanize;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "humanize.NaturalDay")
public class NaturalDayConverter extends BaseConverter
{

	private static final long serialVersionUID = -6615733185378981191L;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value)
	{

		return asDate(value);

	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value)
	{

		return Humanize.naturalDay(asDate(value), getLocale(context));

	}

}
