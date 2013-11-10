/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package humanize.faces.renderkit.html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.ProjectStage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIParameter;
import javax.faces.component.UIViewRoot;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;

/**
 * @author Manfred Geiler (latest modification by $Author: bommel $)
 * @version $Revision: 1187700 $ $Date: 2011-10-22 07:19:37 -0500 (Sat, 22 Oct
 *          2011) $
 */
public final class Utils
{
	// private static final Log log = LogFactory.getLog(RendererUtils.class);
	private static final Logger log = Logger.getLogger(Utils.class.getName());

	public static final String SELECT_ITEM_LIST_ATTR = Utils.class.getName() + ".LIST";

	public static final String EMPTY_STRING = "";
	public static final String SEQUENCE_PARAM = "jsf_sequence";

	// This nice constant is "specified" 13.1.1.2 The Resource API Approach in
	// Spec as an example
	public static final String RES_NOT_FOUND = "RES_NOT_FOUND";

	public static void checkParamValidity(FacesContext facesContext, UIComponent uiComponent, Class<?> compClass)
	{

		if (facesContext == null)
			throw new NullPointerException("facesContext may not be null");
		if (uiComponent == null)
			throw new NullPointerException("uiComponent may not be null");

		// if (compClass != null &&
		// !(compClass.isAssignableFrom(uiComponent.getClass())))
		// why isAssignableFrom with additional getClass method call if
		// isInstance does the same?
		if (compClass != null && !(compClass.isInstance(uiComponent)))
		{
			throw new IllegalArgumentException("uiComponent : " + getPathToComponent(uiComponent) +
			        " is not instance of " + compClass.getName() + " as it should be");
		}
	}

	public static boolean getBooleanAttribute(UIComponent component, String attrName, boolean defaultValue)
	{

		Boolean b = (Boolean) component.getAttributes().get(attrName);
		return b != null ? b.booleanValue() : defaultValue;
	}

	public static Boolean getBooleanValue(UIComponent component)
	{

		Object value = getObjectValue(component);
		// Try to convert to Boolean if it is a String
		if (value instanceof String)
		{
			value = Boolean.valueOf((String) value);
		}

		if (value == null || value instanceof Boolean)
		{
			return (Boolean) value;
		}

		throw new IllegalArgumentException("Expected submitted value of type Boolean for Component : " +
		        getPathToComponent(component));

	}

	public static String getClientId(FacesContext facesContext, UIComponent uiComponent, String forAttr)
	{

		UIComponent forComponent = uiComponent.findComponent(forAttr);
		if (forComponent == null)
		{
			final char separatorChar = UINamingContainer.getSeparatorChar(facesContext);
			if (log.isLoggable(Level.INFO))
			{
				log
				        .info("Unable to find component '"
				                + forAttr
				                + "' (calling findComponent on component '"
				                + uiComponent.getClientId(facesContext)
				                + "')."
				                + " We'll try to return a guessed client-id anyways -"
				                + " this will be a problem if you put the referenced component"
				                + " into a different naming-container. If this is the case, you can always use the full client-id.");
			}
			if (forAttr.length() > 0 && forAttr.charAt(0) == separatorChar)
			{
				// absolute id path
				return forAttr.substring(1);
			}

			// relative id path, we assume a component on the same level as the
			// label component
			String labelClientId = uiComponent.getClientId(facesContext);
			int colon = labelClientId.lastIndexOf(separatorChar);

			return colon == -1 ? forAttr : labelClientId.substring(0, colon + 1) + forAttr;

		}

		return forComponent.getClientId(facesContext);

	}

	public static Date getDateValue(UIComponent component)
	{

		Object value = getObjectValue(component);
		if (value == null || value instanceof Date)
		{
			return (Date) value;
		}

		throw new IllegalArgumentException("Expected submitted value of type Date for component : "
		        + getPathToComponent(component));
	}

	public static int getIntegerAttribute(UIComponent component, String attrName, int defaultValue)
	{

		Integer i = (Integer) component.getAttributes().get(attrName);
		return i != null ? i.intValue() : defaultValue;
	}

	public static Object getObjectValue(UIComponent component)
	{

		if (!(component instanceof ValueHolder))
		{
			throw new IllegalArgumentException("Component : " +
			        getPathToComponent(component) + "is not a ValueHolder");
		}

		if (component instanceof EditableValueHolder)
		{
			Object value = ((EditableValueHolder) component).getSubmittedValue();
			if (value != null)
			{
				return value;
			}
		}

		return ((ValueHolder) component).getValue();
	}

	public static String getPathToComponent(UIComponent component)
	{

		StringBuffer buf = new StringBuffer();

		if (component == null)
		{
			buf.append("{Component-Path : ");
			buf.append("[null]}");
			return buf.toString();
		}

		getPathToComponent(component, buf);

		buf.insert(0, "{Component-Path : ");
		Object location = component.getAttributes().get(UIComponent.VIEW_LOCATION_KEY);
		if (location != null)
		{
			buf.append(" Location: ").append(location);
		}
		buf.append("}");

		return buf.toString();
	}

	public static String getStringValue(FacesContext facesContext,
	        UIComponent component)
	{

		if (!(component instanceof ValueHolder))
		{
			throw new IllegalArgumentException("Component : " + getPathToComponent(component) + "is not a ValueHolder");
		}

		if (component instanceof EditableValueHolder)
		{
			Object submittedValue = ((EditableValueHolder) component).getSubmittedValue();
			if (submittedValue != null)
			{
				if (log.isLoggable(Level.FINE))
					log.fine("returning 1 '" + submittedValue + "'");
				return submittedValue.toString();
			}
		}

		Object value;

		if (component instanceof EditableValueHolder)
		{

			EditableValueHolder holder = (EditableValueHolder) component;

			if (holder.isLocalValueSet())
			{
				value = holder.getLocalValue();
			} else
			{
				value = getValue(component);
			}
		}
		else
		{
			value = getValue(component);
		}

		Converter converter = ((ValueHolder) component).getConverter();
		if (converter == null && value != null)
		{

			try
			{
				converter = facesContext.getApplication().createConverter(value.getClass());
				if (log.isLoggable(Level.FINE))
					log.fine("the created converter is " + converter);
			} catch (FacesException e)
			{
				log.log(Level.SEVERE, "No converter for class " + value.getClass().getName() + " found (component id="
				        + component.getId() + ").", e);
				// converter stays null
			}
		}

		if (converter == null)
		{
			if (value == null)
			{
				if (log.isLoggable(Level.FINE))
					log.fine("returning an empty string");
				return "";
			}

			if (log.isLoggable(Level.FINE))
				log.fine("returning an .toString");
			return value.toString();

		}

		if (log.isLoggable(Level.FINE))
			log.fine("returning converter get as string " + converter);
		return converter.getAsString(facesContext, component, value);

	}

	public static String getStringValue(FacesContext context, ValueExpression ve)
	{

		Object value = ve.getValue(context.getELContext());
		if (value != null)
		{
			return value.toString();
		}
		return null;
	}

	/**
	 * Calls getValidUIParameterChildren(facesContext, children, skipNullValue,
	 * skipUnrendered, true);
	 * 
	 * @param facesContext
	 * @param children
	 * @param skipNullValue
	 * @param skipUnrendered
	 * @return
	 */
	public static List<UIParameter> getValidUIParameterChildren(
	        FacesContext facesContext, List<UIComponent> children,
	        boolean skipNullValue, boolean skipUnrendered)
	{

		return getValidUIParameterChildren(facesContext, children,
		        skipNullValue, skipUnrendered, true);
	}

	/**
	 * Returns a List of all valid UIParameter children from the given children.
	 * Valid means that the UIParameter is not disabled, its name is not null
	 * (if skipNullName is true), its value is not null (if skipNullValue is
	 * true) and it is rendered (if skipUnrendered is true). This method also
	 * creates a warning for every UIParameter with a null-name (again, if
	 * skipNullName is true) and, if ProjectStage is Development and
	 * skipNullValue is true, it informs the user about every null-value.
	 * 
	 * @param facesContext
	 * @param children
	 * @param skipNullValue
	 *            should UIParameters with a null value be skipped
	 * @param skipUnrendered
	 *            should UIParameters with isRendered() returning false be
	 *            skipped
	 * @param skipNullName
	 *            should UIParameters with a null name be skipped (normally
	 *            true, but in the case of h:outputFormat false)
	 * @return
	 */
	public static List<UIParameter> getValidUIParameterChildren(
	        FacesContext facesContext, List<UIComponent> children,
	        boolean skipNullValue, boolean skipUnrendered, boolean skipNullName)
	{

		List<UIParameter> params = null;
		for (int i = 0, size = children.size(); i < size; i++)
		{
			UIComponent child = children.get(i);
			if (child instanceof UIParameter)
			{
				UIParameter param = (UIParameter) child;
				// check for the disable attribute (since 2.0)
				// and the render attribute (only if skipUnrendered is true)
				if (param.isDisable()
				        || (skipUnrendered && !param.isRendered()))
				{
					// ignore this UIParameter and continue
					continue;
				}
				// check the name
				String name = param.getName();
				if (skipNullName && (name == null || EMPTY_STRING.equals(name)))
				{
					// warn for a null-name
					log.log(Level.WARNING,
					        "The UIParameter "
					                + Utils.getPathToComponent(param)
					                + " has a name of null or empty string and thus will not be added to the URL.");
					// and skip it
					continue;
				}
				// check the value
				if (skipNullValue && param.getValue() == null)
				{
					if (facesContext.isProjectStage(ProjectStage.Development))
					{
						// inform the user about the null value when in
						// Development stage
						log.log(Level.INFO,
						        "The UIParameter "
						                + Utils
						                        .getPathToComponent(param)
						                + " has a value of null and thus will not be added to the URL.");
					}
					// skip a null-value
					continue;
				}
				// add the param
				if (params == null)
				{
					params = new ArrayList<UIParameter>();
				}
				params.add(param);
			}
		}
		if (params == null)
		{
			params = Collections.emptyList();
		}
		return params;
	}

	/**
	 * See JSF Spec. 8.5 Table 8-1
	 * 
	 * @param value
	 * @return boolean
	 */
	public static boolean isDefaultAttributeValue(Object value)
	{

		if (value == null)
		{
			return true;
		}
		else if (value instanceof Boolean)
		{
			return !((Boolean) value).booleanValue();
		}
		else if (value instanceof Number)
		{
			if (value instanceof Integer)
			{
				return ((Number) value).intValue() == Integer.MIN_VALUE;
			}
			else if (value instanceof Double)
			{
				return ((Number) value).doubleValue() == Double.MIN_VALUE;
			}
			else if (value instanceof Long)
			{
				return ((Number) value).longValue() == Long.MIN_VALUE;
			}
			else if (value instanceof Byte)
			{
				return ((Number) value).byteValue() == Byte.MIN_VALUE;
			}
			else if (value instanceof Float)
			{
				return ((Number) value).floatValue() == Float.MIN_VALUE;
			}
			else if (value instanceof Short)
			{
				return ((Number) value).shortValue() == Short.MIN_VALUE;
			}
		}
		return false;
	}

	/**
	 * @return true, if the attribute was written
	 * @throws java.io.IOException
	 */
	public static boolean renderHTMLAttribute(ResponseWriter writer,
	        String componentProperty, String attrName, Object value)
	        throws IOException
	{

		if (!isDefaultAttributeValue(value))
		{
			// render JSF "styleClass" and "itemStyleClass" attributes as
			// "class"
			String htmlAttrName = attrName.equals(HTML.STYLE_CLASS_ATTR) ? HTML.CLASS_ATTR
			        : attrName;
			writer.writeAttribute(htmlAttrName, value, componentProperty);
			return true;
		}

		return false;
	}

	/**
	 * @return true, if the attribute was written
	 * @throws java.io.IOException
	 */
	public static boolean renderHTMLAttribute(ResponseWriter writer,
	        UIComponent component, String componentProperty, String htmlAttrName)
	        throws IOException
	{

		Object value = component.getAttributes().get(componentProperty);
		return renderHTMLAttribute(writer, componentProperty, htmlAttrName,
		        value);
	}

	/**
	 * @return true, if an attribute was written
	 * @throws java.io.IOException
	 */
	public static boolean renderHTMLAttributes(ResponseWriter writer,
	        UIComponent component, String[] attributes) throws IOException
	{

		boolean somethingDone = false;
		for (int i = 0, len = attributes.length; i < len; i++)
		{
			String attrName = attributes[i];
			if (renderHTMLAttribute(writer, component, attrName, attrName))
			{
				somethingDone = true;
			}
		}
		return somethingDone;
	}

	public static boolean renderHTMLAttributesWithOptionalStartElement(
	        ResponseWriter writer, UIComponent component, String elementName,
	        String[] attributes) throws IOException
	{
		boolean startElementWritten = false;
		for (int i = 0, len = attributes.length; i < len; i++)
		{
			String attrName = attributes[i];
			Object value = component.getAttributes().get(attrName);
			if (!isDefaultAttributeValue(value))
			{
				if (!startElementWritten)
				{
					writer.startElement(elementName, component);
					startElementWritten = true;
				}
				renderHTMLAttribute(writer, attrName, attrName, value);
			}
		}
		return startElementWritten;
	}

	public static void writeIdIfNecessary(ResponseWriter writer, UIComponent component, FacesContext facesContext)
	        throws IOException
	{

		if (component.getId() != null
		        && !component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX))
		{
			writer.writeAttribute(HTML.ID_ATTR,
			        component.getClientId(facesContext), null);
		}
	}

	private static void getPathToComponent(UIComponent component, StringBuffer buf)
	{

		if (component == null)
			return;

		StringBuffer intBuf = new StringBuffer();

		intBuf.append("[Class: ");
		intBuf.append(component.getClass().getName());
		if (component instanceof UIViewRoot)
		{
			intBuf.append(",ViewId: ");
			intBuf.append(((UIViewRoot) component).getViewId());
		}
		else
		{
			intBuf.append(",Id: ");
			intBuf.append(component.getId());
		}
		intBuf.append("]");

		buf.insert(0, intBuf.toString());

		getPathToComponent(component.getParent(), buf);
	}

	private static Object getValue(UIComponent component)
	{

		Object value;
		try
		{
			value = ((ValueHolder) component).getValue();
		} catch (Exception ex)
		{
			throw new FacesException("Could not retrieve value of component with path : " +
			        getPathToComponent(component), ex);
		}
		return value;
	}

	private Utils()
	{

		// nope
	}

}
