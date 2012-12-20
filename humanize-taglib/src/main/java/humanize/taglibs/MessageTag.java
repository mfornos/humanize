package humanize.taglibs;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.apache.taglibs.standard.tag.common.fmt.HumanizeMessageSupport;

/**
 * <p>
 * A handler for &lt;message&gt; that supports rtexprvalue-based attributes.
 * </p>
 * 
 * @author Jan Luehe
 */
public class MessageTag extends HumanizeMessageSupport {

	private static final long serialVersionUID = -7333452590563574360L;

	// for tag attribute
	public void setKey(String key) throws JspTagException {

		this.keyAttrValue = key;
		this.keySpecified = true;
		
	}

	// for tag attribute
	public void setBundle(LocalizationContext locCtxt) throws JspTagException {

		this.bundleAttrValue = locCtxt;
		this.bundleSpecified = true;
		
	}
}
