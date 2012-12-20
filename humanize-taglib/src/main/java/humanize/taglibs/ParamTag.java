package humanize.taglibs;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

import org.apache.taglibs.standard.resources.Resources;
import org.apache.taglibs.standard.tag.common.fmt.HumanizeMessageSupport;

/**
 * <p>
 * A handler for &lt;param&gt; that supports rtexprvalue-based message
 * arguments.
 * </p>
 * 
 * @author Jan Luehe
 */

public class ParamTag extends BodyTagSupport {

	private static final long serialVersionUID = -4456345915979081598L;

	// for tag attribute
	public void setValue(Object value) throws JspTagException {

		this.value = value;
		this.valueSpecified = true;

	}

	// XXX ParamSupport
	protected Object value;
	/*    */protected boolean valueSpecified;

	/*    */
	/*    */public ParamTag()
	/*    */{

		/* 49 */init();
		/*    */}

	/*    */
	/*    */private void init() {

		/* 53 */this.value = null;
		/* 54 */this.valueSpecified = false;
		/*    */}

	/*    */
	/*    */public int doEndTag()
	/*    */throws JspException
	/*    */{

		/* 63 */Tag t = findAncestorWithClass(this, HumanizeMessageSupport.class);
		/* 64 */if (t == null) {
			/* 65 */throw new JspTagException(Resources.getMessage("PARAM_OUTSIDE_MESSAGE"));
			/*    */}
		/*    */
		/* 68 */HumanizeMessageSupport parent = (HumanizeMessageSupport) t;
		/*    */
		/* 75 */Object input = null;
		/*    */
		/* 77 */if (this.valueSpecified)
		/*    */{
			/* 79 */input = this.value;
			/*    */}
		/*    */else
		/*    */{
			/* 83 */input = this.bodyContent.getString().trim();
			/*    */}
		/* 85 */parent.addParam(input);
		/*    */
		/* 87 */return EVAL_PAGE;
		/*    */}

	/*    */
	/*    */public void release()
	/*    */{

		/* 92 */init();
		/*    */}

}
