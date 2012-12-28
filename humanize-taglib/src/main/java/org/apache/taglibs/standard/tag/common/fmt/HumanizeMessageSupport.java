package org.apache.taglibs.standard.tag.common.fmt;

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * glassfish/bootstrap/legal/CDDLv1.0.txt or
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Portions Copyright Apache Software Foundation.
 */

import humanize.Humanize;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

import org.apache.taglibs.standard.tag.common.core.Util;
import org.apache.taglibs.standard.tag.common.fmt.BundleSupport;
import org.apache.taglibs.standard.tag.common.fmt.SetLocaleSupport;

/**
 * Support for tag handlers for &lt;message&gt;, the message formatting tag in
 * JSTL 1.0.
 * 
 * @author Jan Luehe
 */

public abstract class HumanizeMessageSupport extends BodyTagSupport {

	private static final long serialVersionUID = -8279839743465404094L;

	public static final String UNDEFINED_KEY = "???";

	// *********************************************************************
	// Protected state

	protected String keyAttrValue; // 'key' attribute value
	protected boolean keySpecified; // 'key' attribute specified
	protected LocalizationContext bundleAttrValue; // 'bundle' attribute value
	protected boolean bundleSpecified; // 'bundle' attribute specified?

	// *********************************************************************
	// Private state

	private String var; // 'var' attribute
	private int scope; // 'scope' attribute
	private List<Object> params;

	// *********************************************************************
	// Constructor and initialization

	public HumanizeMessageSupport() {

		super();
		params = new ArrayList<Object>();
		init();
	}

	private void init() {

		var = null;
		scope = PageContext.PAGE_SCOPE;
		keyAttrValue = null;
		keySpecified = false;
		bundleAttrValue = null;
		bundleSpecified = false;
	}

	// *********************************************************************
	// Tag attributes known at translation time

	public void setVar(String var) {

		this.var = var;
	}

	public void setScope(String scope) {

		this.scope = Util.getScope(scope);
	}

	// *********************************************************************
	// Collaboration with subtags

	/**
	 * Adds an argument (for parametric replacement) to this tag's message.
	 * 
	 * @see ParamSupport
	 */
	public void addParam(Object arg) {

		params.add(arg);

	}

	// *********************************************************************
	// Tag logic

	public int doStartTag() throws JspException {

		params.clear();
		return EVAL_BODY_BUFFERED;
	}

	public int doEndTag() throws JspException {

		String key = null;
		LocalizationContext locCtxt = null;

		// determine the message key by...
		if (keySpecified) {
			// ... reading 'key' attribute
			key = keyAttrValue;
		} else {
			// ... retrieving and trimming our body
			if (bodyContent != null && bodyContent.getString() != null)
				key = bodyContent.getString().trim();
		}

		if ((key == null) || key.equals("")) {
			try {
				pageContext.getOut().print("??????");
			} catch (IOException ioe) {
				throw new JspTagException(ioe.toString(), ioe);
			}
			return EVAL_PAGE;
		}

		String prefix = null;
		if (!bundleSpecified) {
			Tag t = findAncestorWithClass(this, BundleSupport.class);
			if (t != null) {
				// use resource bundle from parent <bundle> tag
				BundleSupport parent = (BundleSupport) t;
				locCtxt = parent.getLocalizationContext();
				prefix = parent.getPrefix();
			} else {
				locCtxt = BundleSupport.getLocalizationContext(pageContext);
			}
		} else {
			// localization context taken from 'bundle' attribute
			locCtxt = bundleAttrValue;
			if (locCtxt.getLocale() != null) {
				SetLocaleSupport.setResponseLocale(pageContext, locCtxt.getLocale());
			}
		}

		String message = UNDEFINED_KEY + key + UNDEFINED_KEY;
		if (locCtxt != null) {
			ResourceBundle bundle = locCtxt.getResourceBundle();
			if (bundle != null) {
				try {
					// prepend 'prefix' attribute from parent bundle
					if (prefix != null)
						key = prefix + key;
					message = bundle.getString(key);
					// Perform parametric replacement if required
					if (!params.isEmpty()) {
						Object[] messageArgs = params.toArray();
						Locale locale;
						if (locCtxt.getLocale() != null) {
							locale = locCtxt.getLocale();
						} else {
							// For consistency with the <fmt:formatXXX> actions,
							// we try to get a locale that matches the user's
							// preferences
							// as well as the locales supported by 'date' and
							// 'number'.
							// System.out.println("LOCALE-LESS LOCCTXT: GETTING FORMATTING LOCALE");
							locale = SetLocaleSupport.getFormattingLocale(pageContext);
							// System.out.println("LOCALE: " + locale);
						}
						MessageFormat formatter = (locale != null) ? Humanize.messageFormat(message, locale)
						        : Humanize.messageFormat(message);
						message = formatter.format(messageArgs);
					}
				} catch (MissingResourceException mre) {
					message = UNDEFINED_KEY + key + UNDEFINED_KEY;
				}
			}
		}

		if (var != null) {
			pageContext.setAttribute(var, message, scope);
		} else {
			try {
				pageContext.getOut().print(message);
			} catch (IOException ioe) {
				throw new JspTagException(ioe.toString(), ioe);
			}
		}

		return EVAL_PAGE;
	}

	// Releases any resources we may have (or inherit)
	public void release() {

		init();
		
	}
}
