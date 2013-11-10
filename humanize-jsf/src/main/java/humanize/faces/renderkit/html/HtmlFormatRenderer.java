package humanize.faces.renderkit.html;

import humanize.Humanize;
import humanize.spi.MessageFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.UIParameter;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;

@FacesRenderer(renderKitId = "HTML_BASIC", componentFamily = "javax.faces.Output", rendererType = "javax.faces.Format")
public class HtmlFormatRenderer extends Renderer
{

    private static final Logger log = Logger.getLogger(HtmlFormatRenderer.class.getName());

    private static final String ESCAPE_ATTR = "escape";
    private static final String VALUE_ATTR = "value";

    private static final Object[] EMPTY_ARGS = new Object[0];

    @Override
    public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException
    {

        //
    }

    @Override
    public void encodeChildren(FacesContext facescontext, UIComponent uicomponent) throws IOException
    {

        //
    }

    @Override
    public void encodeEnd(FacesContext facesContext, UIComponent component) throws IOException
    {

        Utils.checkParamValidity(facesContext, component,
                UIOutput.class);

        String text = getOutputFormatText(facesContext, component);

        boolean escape = Utils.getBooleanAttribute(component, ESCAPE_ATTR, true);

        if (text != null)
        {
            ResponseWriter writer = facesContext.getResponseWriter();
            boolean span = false;

            if (!(component.getId() == null || component.getId().startsWith(UIViewRoot.UNIQUE_ID_PREFIX)))
            {
                span = true;

                writer.startElement(HTML.SPAN_ELEM, component);

                Utils.writeIdIfNecessary(writer, component, facesContext);

                Utils.renderHTMLAttributes(writer, component, HTML.COMMON_PASSTROUGH_ATTRIBUTES);

            } else
            {
                span = Utils.renderHTMLAttributesWithOptionalStartElement(writer, component,
                        HTML.SPAN_ELEM, HTML.COMMON_PASSTROUGH_ATTRIBUTES);
            }

            if (escape)
            {
                writer.writeText(text, VALUE_ATTR);
            } else
            {
                writer.write(text);
            }

            if (span)
            {
                writer.endElement(HTML.SPAN_ELEM);
            }
        }
    }

    private String getOutputFormatText(FacesContext facesContext, UIComponent htmlOutputFormat)
    {

        String pattern = Utils.getStringValue(facesContext, htmlOutputFormat);

        Object[] args;
        if (htmlOutputFormat.getChildCount() == 0)
        {
            args = EMPTY_ARGS;
        }
        else
        {
            List<Object> argsList = null;

            if (htmlOutputFormat.getChildCount() > 0)
            {
                List<UIParameter> validParams = Utils.getValidUIParameterChildren(
                        facesContext, htmlOutputFormat.getChildren(), false, false, false);
                for (UIParameter param : validParams)
                {
                    if (argsList == null)
                    {
                        argsList = new ArrayList<Object>();
                    }
                    argsList.add(param.getValue());
                }
            }

            if (argsList != null)
            {
                args = argsList.toArray(new Object[argsList.size()]);
            }
            else
            {
                args = EMPTY_ARGS;
            }
        }

        MessageFormat format = Humanize.messageFormat(pattern, facesContext.getViewRoot().getLocale());

        try
        {
            return format.format(args);
        } catch (Exception e)
        {
            log.severe("Error formatting message of component " + htmlOutputFormat.getClientId(facesContext));
            return "";
        }
    }

}
