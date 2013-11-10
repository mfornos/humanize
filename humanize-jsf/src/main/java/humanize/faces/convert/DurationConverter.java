package humanize.faces.convert;

import humanize.Humanize;
import humanize.util.Constants.TimeStyle;

import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "humanize.Duration")
public class DurationConverter extends NumberConverter
{

	private static final long serialVersionUID = -6598199651948023319L;

	private String style;

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value)
	{

		return Humanize.duration(asNumber(value), getTimeStyle(), getLocale(context));

	}

	public String getStyle()
	{

		return style;

	}

	public void setStyle(String style)
	{

		clearInitialState();
		this.style = style;

	}

	@Override
	protected void restore(Iterator<Object> iterator)
	{

		this.style = (String) iterator.next();

	}

	@Override
	protected void save(List<Object> states)
	{

		states.add(style);

	}

	private TimeStyle getTimeStyle()
	{

		return style == null ? TimeStyle.STANDARD : TimeStyle.valueOf(style.toUpperCase());

	}

}
