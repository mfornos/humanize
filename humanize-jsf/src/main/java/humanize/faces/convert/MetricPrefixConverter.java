package humanize.faces.convert;

import humanize.Humanize;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "humanize.MetricPrefix")
public class MetricPrefixConverter extends BaseConverter {

	private static final long serialVersionUID = 789707697926582865L;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {

		return asDouble(value);

	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {

		return Humanize.metricPrefix(asDouble(value), getLocale(context));

	}
}
