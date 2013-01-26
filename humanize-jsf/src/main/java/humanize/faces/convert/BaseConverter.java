package humanize.faces.convert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.faces.component.PartialStateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import com.google.common.base.Preconditions;

public abstract class BaseConverter implements Converter, Serializable, PartialStateHolder {

	private static final long serialVersionUID = -7333204612599724969L;

	private Locale locale;

	private boolean initialState;

	private boolean transientFlag = false;

	@Override
	public void clearInitialState() {

		initialState = false;

	}

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {

		return value;

	}

	public Locale getLocale() {

		if (locale == null) {
			locale = getLocale(FacesContext.getCurrentInstance());
		}

		return locale;

	}

	@Override
	public boolean initialStateMarked() {

		return initialState;

	}

	@Override
	public boolean isTransient() {

		return transientFlag;

	}

	@Override
	public void markInitialState() {

		initialState = true;

	}

	@Override
	public void restoreState(FacesContext context, Object state) {

		Preconditions.checkNotNull(context);

		if (state != null) {
			@SuppressWarnings("unchecked")
			List<Object> values = (List<Object>) state;
			Iterator<Object> iterator = values.iterator();
			locale = (Locale) iterator.next();
			restore(iterator);
		}

	}

	@Override
	public Object saveState(FacesContext context) {

		Preconditions.checkNotNull(context);

		if (!initialStateMarked()) {
			List<Object> states = new ArrayList<Object>();
			states.add(locale);
			save(states);
			return states;
		}

		return null;
	}

	public void setLocale(Locale locale) {

		clearInitialState();
		this.locale = locale;

	}

	@Override
	public void setTransient(boolean transientFlag) {

		this.transientFlag = transientFlag;

	}

	protected Date asDate(Object value) {

		if (Date.class.isAssignableFrom(value.getClass())) {
			return (Date) value;
		} else {
			return new Date(Long.parseLong(value.toString()));
		}

	}

	protected Number asNumber(Object input) {

		if (input instanceof String) {
			try {
				String istr = (String) input;
				return (istr.indexOf('.') != -1) ? Double.valueOf(istr) : Long.valueOf(istr);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException(nfe);
			}
		}

		return (Number) input;

	}

	protected Locale getLocale(FacesContext context) {

		return locale == null ? context.getViewRoot().getLocale() : locale;

	}

	protected void restore(Iterator<Object> iterator) {

		//

	}

	protected void save(List<Object> states) {

		//

	}
}
