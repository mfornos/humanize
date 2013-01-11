package org.apache.taglibs.standard.tag.common.fmt;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.taglibs.standard.tag.common.core.Util;

public abstract class HumanizeSupport extends BodyTagSupport {

	private static final long serialVersionUID = 106589551951164935L;

	private String var; // 'var' attribute
	private int scope; // 'scope' attribute
	protected boolean resolveLocale;

	public HumanizeSupport() {

		super();
		init();

	}

	@Override
	public int doEndTag() throws JspException {

		begin();

		if (isContextRemoveNeeded()) {
			// Spec says:
			// If value is null or empty, remove the scoped variable
			// if it is specified (see attributes var and scope).
			if (var != null) {
				pageContext.removeAttribute(var, scope);
			}
			return EVAL_PAGE;
		}

		// Determine formatting locale
		Locale loc = resolveLocale ? SetLocaleSupport.getFormattingLocale(pageContext) : null;

		// Render
		String formatted = loc == null ? render() : render(loc);

		if (var != null) {
			pageContext.setAttribute(var, formatted, scope);
		} else {
			try {
				pageContext.getOut().print(formatted);
			} catch (IOException ioe) {
				throw new JspTagException(ioe.toString(), ioe);
			}
		}

		return end();
	}

	@Override
	public void release() {

		init();

	}

	public void setScope(String scope) {

		this.scope = Util.getScope(scope);
	}

	public void setVar(String var) {

		this.var = var;
	}

	abstract protected void begin() throws JspException;

	abstract protected void clean();

	protected int end() {

		return EVAL_PAGE;

	}

	protected void init() {

		this.resolveLocale = true;
		this.var = null;
		this.scope = PageContext.REQUEST_SCOPE;

		clean();

	}

	protected String inputFromBody() {

		return (bodyContent != null && bodyContent.getString() != null) ? bodyContent.getString().trim() : null;

	}

	protected boolean isEmpty(String str) {

		return str == null || str.length() < 1;
		
	}

	abstract protected boolean isContextRemoveNeeded();

	abstract protected String render() throws JspException;

	abstract protected String render(Locale locale) throws JspException;

}
