package org.apache.taglibs.standard.tag.common.fmt;

import javax.servlet.jsp.JspException;

import static humanize.taglibs.util.Convert.asNumber;

public abstract class NumberCallSupport extends HumanizeSupport {

	private static final long serialVersionUID = 4926940370084394075L;

	private Object value;

	protected Number input;

	public void setValue(Object value) {

		this.value = value;

	}

	@Override
	protected void begin() throws JspException {

		Object tmp = value == null ? inputFromBody() : value;
		this.input = asNumber(tmp);

	}

	@Override
	protected void clean() {

		this.value = null;
		this.input = null;

	}

	@Override
	protected boolean isContextRemoveNeeded() {

		return input == null;

	}

}
