package humanize.taglibs;

import static humanize.taglibs.util.Convert.asNumber;
import humanize.Humanize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.servlet.jsp.JspException;

import org.apache.taglibs.standard.tag.common.fmt.HumanizeSupport;

public class PluralizeTag extends HumanizeSupport {

	private static final long serialVersionUID = -3406725990318696579L;

	private String template;

	private Object value;

	private String args;

	private String input;

	private Object[] argsArray;

	public void setTemplate(String template) {

		this.template = template;

	}

	public void setValue(Object value) {

		this.value = value;

	}

	public void setArgs(String args) {

		this.args = args;

	}

	@Override
	protected void begin() throws JspException {

		this.input = template == null || template.length() < 1 ? inputFromBody() : template;

		List<Object> tmpArr = new ArrayList<Object>();
		tmpArr.add(asNumber(value));
		if (args != null) {
			tmpArr.addAll(Arrays.asList(args.split("\\s*,\\s*")));
		}

		this.argsArray = tmpArr.toArray(new Object[tmpArr.size()]);

	}

	protected void clean() {

		this.template = null;
		this.value = null;
		this.input = null;
		this.args = null;
		this.argsArray = null;

	}

	@Override
	protected boolean isContextRemoveNeeded() {

		return (input == null || input.length() < 1) || (argsArray == null || argsArray.length == 0);

	}

	@Override
	protected String render() throws JspException {

		return Humanize.pluralize(input).render(argsArray);

	}

	@Override
	protected String render(Locale locale) throws JspException {

		return Humanize.pluralize(input, locale).render(argsArray);

	}

}
