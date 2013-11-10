package humanize.taglibs;

import humanize.Humanize;

import java.util.Date;
import java.util.Locale;

import javax.servlet.jsp.JspException;

import org.apache.taglibs.standard.tag.common.fmt.HumanizeSupport;

public class NaturalTimeTag extends HumanizeSupport
{

	private static final long serialVersionUID = -6504638950550719198L;

	private Date from;
	private Date to;

	public Date getDefaultedFrom()
	{

		return from == null ? new Date() : from;

	}

	public void setFrom(Object from)
	{

		this.from = (Date) from;

	}

	public void setTo(Object to)
	{

		this.to = (Date) to;

	}

	@Override
	protected void begin() throws JspException
	{

		//

	}

	protected void clean()
	{

		this.from = null;
		this.to = null;

	}

	@Override
	protected boolean isContextRemoveNeeded()
	{

		return from == null && to == null;

	}

	@Override
	protected String render() throws JspException
	{

		return Humanize.naturalTime(getDefaultedFrom(), to);

	}

	@Override
	protected String render(Locale locale) throws JspException
	{

		return Humanize.naturalTime(getDefaultedFrom(), to, locale);

	}

}
