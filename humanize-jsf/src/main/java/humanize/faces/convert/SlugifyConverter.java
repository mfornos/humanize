package humanize.faces.convert;

import humanize.Humanize;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "humanize.Slugify")
public class SlugifyConverter extends BaseConverter
{

	private static final long serialVersionUID = -446143597515448224L;

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value)
	{

		return Humanize.slugify(value.toString());

	}
}
