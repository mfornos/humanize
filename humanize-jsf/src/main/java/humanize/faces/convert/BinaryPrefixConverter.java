package humanize.faces.convert;

import humanize.Humanize;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "humanize.BinaryPrefix")
public class BinaryPrefixConverter extends BaseConverter {

	private static final long serialVersionUID = 1942567170903039630L;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {

		return asDouble(value);

	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {

		return Humanize.binaryPrefix(asDouble(value), getLocale(context));

	}

}
