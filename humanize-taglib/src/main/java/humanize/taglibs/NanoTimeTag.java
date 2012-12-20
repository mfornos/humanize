package humanize.taglibs;

import humanize.Humanize;

import java.util.Locale;

import javax.servlet.jsp.JspException;

import org.apache.taglibs.standard.tag.common.fmt.NumberCallSupport;

public class NanoTimeTag extends NumberCallSupport {

    private static final long serialVersionUID = -3090050722249192237L;

	@Override
    protected String render() throws JspException {

		return Humanize.nanoTime(input);
		
    }

	@Override
    protected String render(Locale locale) throws JspException {

		return Humanize.nanoTime(input, locale);
		
    }

}
