package humanize.taglibs;

import humanize.Humanize;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.apache.taglibs.standard.tag.common.fmt.HumanizeSupport;

public class NaturalDayTag extends HumanizeSupport {

	private static final long serialVersionUID = -5236222588491148996L;

	private Date value;

	private int style;

	private static final Map<String, Integer> styleMap = new HashMap<String, Integer>();
	static {
		styleMap.put("short", DateFormat.SHORT);
		styleMap.put("medium", DateFormat.MEDIUM);
		styleMap.put("long", DateFormat.LONG);
		styleMap.put("full", DateFormat.FULL);
	}

	public void setStyle(String style) {

		String key = style.toLowerCase();
		this.style = styleMap.containsKey(key) ? styleMap.get(key) : DateFormat.SHORT;

	}

	public void setValue(Object value) {

		this.value = (Date) value;

	}

	@Override
	protected void begin() throws JspException {

		//

	}

	protected void clean() {

		this.value = null;
		this.style = DateFormat.SHORT;

	}

	@Override
	protected boolean isContextRemoveNeeded() {

		return value == null;

	}

	@Override
	protected String render() throws JspException {

		return Humanize.naturalDay(style, value);

	}

	@Override
	protected String render(Locale locale) throws JspException {

		return Humanize.naturalDay(style, value, locale);

	}

}
