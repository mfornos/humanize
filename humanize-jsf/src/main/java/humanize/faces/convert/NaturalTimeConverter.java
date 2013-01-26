package humanize.faces.convert;

import humanize.Humanize;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

@FacesConverter(value = "humanize.NaturalDay")
public class NaturalTimeConverter extends BaseConverter {

	private static final long serialVersionUID = 2321489006654386163L;

	private Object reference;

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {

		return asDate(value);

	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {

		return Humanize.naturalTime(getReference(), asDate(value), getLocale(context));

	}

	public Object getFrom() {

		return reference;

	}

	public void setFrom(Object reference) {

		clearInitialState();
		this.reference = reference;

	}

	@Override
	protected void restore(Iterator<Object> iterator) {

		this.reference = iterator.next();

	}

	@Override
	protected void save(List<Object> states) {

		states.add(reference);

	}

	private Date getReference() {

		return reference == null ? new Date() : asDate(reference);

	}

}
