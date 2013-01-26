package humanize.faces.convert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.faces.component.PartialStateHolder;
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

	protected Double asDouble(Object value) {

		if (Number.class.isAssignableFrom(value.getClass())) {
			return ((Number) value).doubleValue();
		} else {
			return Double.parseDouble(value.toString());
		}

	}

	protected Long asLong(Object value) {

		if (Number.class.isAssignableFrom(value.getClass())) {
			return ((Number) value).longValue();
		} else {
			return Long.parseLong(value.toString());
		}

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
