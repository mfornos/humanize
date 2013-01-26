package humanize.faces.convert;

import humanize.Humanize;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "humanize.NanoTime")
public class NanoTimeConverter extends NumberConverter {

    private static final long serialVersionUID = 5010346061889560233L;

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {

		return Humanize.nanoTime(asNumber(value), getLocale(context));

	}
}
