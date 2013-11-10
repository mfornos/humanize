package humanize.faces.convert;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

public abstract class NumberConverter extends BaseConverter
{

	private static final long serialVersionUID = -1609171975745646864L;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value)
	{

		return asNumber(value);

	}

}
