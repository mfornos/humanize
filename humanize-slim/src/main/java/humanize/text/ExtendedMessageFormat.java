/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package humanize.text;

import java.text.Format;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import com.google.common.base.Preconditions;

/**
 * Extends <code>java.text.MessageFormat</code> to allow pluggable/additional
 * formatting options for embedded format elements. Client code should specify a
 * registry of <code>FormatFactory</code> instances associated with
 * <code>String</code> format names. This registry will be consulted when the
 * format elements are parsed from the message pattern. In this way custom
 * patterns can be specified, and the formats supported by
 * <code>java.text.MessageFormat</code> can be overridden at the format and/or
 * format style level (see MessageFormat). A "format element" embedded in the
 * message pattern is specified (<b>()?</b> signifies optionality):<br />
 * <code>{</code><i>argument-number</i><b>(</b><code>,</code>
 * <i>format-name</i><b> (</b><code>,</code><i>format-style</i><b>)?)?</b>
 * <code>}</code>
 * 
 * <p>
 * <i>format-name</i> and <i>format-style</i> values are trimmed of surrounding
 * whitespace in the manner of <code>java.text.MessageFormat</code>. If
 * <i>format-name</i> denotes <code>FormatFactory formatFactoryInstance</code>
 * in <code>registry</code>, a <code>Format</code> matching <i>format-name</i>
 * and <i>format-style</i> is requested from <code>formatFactoryInstance</code>.
 * If this is successful, the <code>Format</code> found is used for this format
 * element.
 * </p>
 * 
 * <p>
 * Limitations inherited from <code>java.text.MessageFormat</code>:
 * <ul>
 * <li>When using "choice" subformats, support for nested formatting
 * instructions is limited to that provided by the base class.</li>
 * <li>Thread-safety of <code>Format</code>s, including
 * <code>MessageFormat</code> and thus <code>ExtendedMessageFormat</code>, is
 * not guaranteed.</li>
 * </ul>
 * </p>
 * 
 * @version $Id: ExtendedMessageFormat.java 1144929 2011-07-10 18:26:16Z
 *          ggregory $
 */
public class ExtendedMessageFormat extends MessageFormat
{

	private static final long serialVersionUID = -2362048321261811743L;

	private static final String DUMMY_PATTERN = "";
	private static final String ESCAPED_QUOTE = "''";
	private static final char START_FMT = ',';
	private static final char END_FE = '}';
	private static final char START_FE = '{';
	private static final char QUOTE = '\'';

	private String toPattern;
	private final Map<String, ? extends FormatFactory> registry;

	private static final char[] SPLIT_CHARS = " \t\n\r\f".toCharArray();

	static
	{
		Arrays.sort(SPLIT_CHARS);
	}

	/**
	 * Create a new ExtendedMessageFormat for the default locale.
	 * 
	 * @param pattern
	 *            the pattern to use, not null
	 * @throws IllegalArgumentException
	 *             in case of a bad pattern.
	 */
	public ExtendedMessageFormat(String pattern)
	{

		this(pattern, Locale.getDefault());

	}

	/**
	 * Create a new ExtendedMessageFormat.
	 * 
	 * @param pattern
	 *            the pattern to use, not null
	 * @param locale
	 *            the locale to use, not null
	 * @throws IllegalArgumentException
	 *             in case of a bad pattern.
	 */
	public ExtendedMessageFormat(String pattern, Locale locale)
	{

		this(pattern, locale, null);

	}

	/**
	 * Create a new ExtendedMessageFormat.
	 * 
	 * @param pattern
	 *            the pattern to use, not null
	 * @param locale
	 *            the locale to use, not null
	 * @param registry
	 *            the registry of format factories, may be null
	 * @throws IllegalArgumentException
	 *             in case of a bad pattern.
	 */
	public ExtendedMessageFormat(String pattern, Locale locale, Map<String, ? extends FormatFactory> registry)
	{

		super(DUMMY_PATTERN);
		setLocale(locale);
		this.registry = registry;
		applyPattern(pattern);

	}

	/**
	 * Create a new ExtendedMessageFormat for the default locale.
	 * 
	 * @param pattern
	 *            the pattern to use, not null
	 * @param registry
	 *            the registry of format factories, may be null
	 * @throws IllegalArgumentException
	 *             in case of a bad pattern.
	 */
	public ExtendedMessageFormat(String pattern, Map<String, ? extends FormatFactory> registry)
	{

		this(pattern, Locale.getDefault(), registry);

	}

	/**
	 * Apply the specified pattern.
	 * 
	 * @param pattern
	 *            String
	 */
	@Override
	public final void applyPattern(String pattern)
	{

		if (hasRegistry())
		{
			super.applyPattern(pattern);
			toPattern = super.toPattern();
			return;
		}

		ArrayList<Format> foundFormats = new ArrayList<Format>();
		ArrayList<String> foundDescriptions = new ArrayList<String>();
		StringBuilder stripCustom = new StringBuilder(pattern.length());

		ParsePosition pos = new ParsePosition(0);
		char[] c = pattern.toCharArray();
		int fmtCount = 0;

		while (pos.getIndex() < pattern.length())
		{

			char charType = c[pos.getIndex()];

			if (QUOTE == charType)
			{

				appendQuotedString(pattern, pos, stripCustom, true);
				continue;

			}

			if (START_FE == charType)
			{

				fmtCount++;
				seekNonWs(pattern, pos);
				int start = pos.getIndex();
				int index = readArgumentIndex(pattern, next(pos));
				stripCustom.append(START_FE).append(index);
				seekNonWs(pattern, pos);
				Format format = null;
				String formatDescription = null;
				if (c[pos.getIndex()] == START_FMT)
				{
					formatDescription = parseFormatDescription(pattern, next(pos));
					format = getFormat(formatDescription);
					if (format == null)
					{
						stripCustom.append(START_FMT).append(formatDescription);
					}
				}
				foundFormats.add(format);
				foundDescriptions.add(format == null ? null : formatDescription);

				Preconditions.checkState(foundFormats.size() == fmtCount);
				Preconditions.checkState(foundDescriptions.size() == fmtCount);

				if (c[pos.getIndex()] != END_FE)
				{
					throw new IllegalArgumentException("Unreadable format element at position " + start);
				}

			}

			//$FALL-THROUGH$
			stripCustom.append(c[pos.getIndex()]);
			next(pos);

		}

		super.applyPattern(stripCustom.toString());

		toPattern = insertFormats(super.toPattern(), foundDescriptions);

		if (containsElements(foundFormats))
		{
			Format[] origFormats = getFormats();
			// only loop over what we know we have, as MessageFormat on Java 1.3
			// seems to provide an extra format element:
			Iterator<Format> it = foundFormats.iterator();
			for (int i = 0; it.hasNext(); i++)
			{
				Format f = it.next();
				if (f != null)
				{
					origFormats[i] = f;
				}
			}
			super.setFormats(origFormats);
		}

	}

	@Override
	public boolean equals(Object obj)
	{

		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExtendedMessageFormat other = (ExtendedMessageFormat) obj;
		if (registry == null)
		{
			if (other.registry != null)
				return false;
		} else if (!registry.equals(other.registry))
			return false;
		if (toPattern == null)
		{
			if (other.toPattern != null)
				return false;
		} else if (!toPattern.equals(other.toPattern))
			return false;

		return true;

	}

	/**
	 * Return the hashcode.
	 * 
	 * @return the hashcode
	 */
	@Override
	public int hashCode()
	{

		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((registry == null) ? 0 : registry.hashCode());
		result = prime * result + ((toPattern == null) ? 0 : toPattern.hashCode());
		return result;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFormat(int formatElementIndex, Format newFormat)
	{

		super.setFormat(formatElementIndex, newFormat);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFormatByArgumentIndex(int argumentIndex, Format newFormat)
	{

		super.setFormatByArgumentIndex(argumentIndex, newFormat);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFormats(Format[] newFormats)
	{

		super.setFormats(newFormats);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFormatsByArgumentIndex(Format[] newFormats)
	{

		super.setFormatsByArgumentIndex(newFormats);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toPattern()
	{

		return toPattern;

	}

	/**
	 * Consume a quoted string, adding it to <code>appendTo</code> if specified.
	 * 
	 * @param pattern
	 *            pattern to parse
	 * @param pos
	 *            current parse position
	 * @param appendTo
	 *            optional StringBuffer to append
	 * @param escapingOn
	 *            whether to process escaped quotes
	 * @return <code>appendTo</code>
	 */
	private StringBuilder appendQuotedString(String pattern, ParsePosition pos, StringBuilder appendTo,
	        boolean escapingOn)
	{

		int start = pos.getIndex();
		char[] c = pattern.toCharArray();
		if (escapingOn && c[start] == QUOTE)
		{
			next(pos);
			return appendTo == null ? null : appendTo.append(QUOTE);
		}
		int lastHold = start;
		for (int i = pos.getIndex(); i < pattern.length(); i++)
		{
			if (escapingOn && pattern.substring(i).startsWith(ESCAPED_QUOTE))
			{
				appendTo.append(c, lastHold, pos.getIndex() - lastHold).append(QUOTE);
				pos.setIndex(i + ESCAPED_QUOTE.length());
				lastHold = pos.getIndex();
				continue;
			}
			switch (c[pos.getIndex()])
			{
			case QUOTE:
				next(pos);
				return appendTo == null ? null : appendTo.append(c, lastHold, pos.getIndex() - lastHold);
			default:
				next(pos);
			}
		}

		throw new IllegalArgumentException("Unterminated quoted string at position " + start);

	}

	/**
	 * Learn whether the specified Collection contains non-null elements.
	 * 
	 * @param coll
	 *            to check
	 * @return <code>true</code> if some Object was found, <code>false</code>
	 *         otherwise.
	 */
	private boolean containsElements(Collection<?> coll)
	{

		return coll != null && !coll.isEmpty();

		// if (coll == null || coll.size() == 0) {
		// return false;
		// }
		// for (Object name : coll) {
		// if (name != null) {
		// return true;
		// }
		// }
		// return false;

	}

	/**
	 * Get a custom format from a format description.
	 * 
	 * @param desc
	 *            String
	 * @return Format
	 */
	private Format getFormat(String desc)
	{

		if (registry != null)
		{
			String name = desc;
			String args = null;
			int i = desc.indexOf(START_FMT);
			if (i > 0)
			{
				name = desc.substring(0, i).trim();
				args = desc.substring(i + 1).trim();
			}
			FormatFactory factory = registry.get(name);
			if (factory != null)
			{
				return factory.getFormat(name, args, getLocale());
			}
		}
		return null;

	}

	/**
	 * Consume quoted string only
	 * 
	 * @param pattern
	 *            pattern to parse
	 * @param pos
	 *            current parse position
	 * @param escapingOn
	 *            whether to process escaped quotes
	 */
	private void getQuotedString(String pattern, ParsePosition pos, boolean escapingOn)
	{

		appendQuotedString(pattern, pos, null, escapingOn);
	}

	private boolean hasRegistry()
	{

		return registry == null || registry.isEmpty();

	}

	/**
	 * Insert formats back into the pattern for toPattern() support.
	 * 
	 * @param pattern
	 *            source
	 * @param customPatterns
	 *            The custom patterns to re-insert, if any
	 * @return full pattern
	 */
	private String insertFormats(String pattern, ArrayList<String> customPatterns)
	{

		if (!containsElements(customPatterns))
		{
			return pattern;
		}

		StringBuilder sb = new StringBuilder(pattern.length() * 2);
		ParsePosition pos = new ParsePosition(0);
		int fe = -1;
		int depth = 0;
		do
		{
			char c = pattern.charAt(pos.getIndex());

			if (QUOTE == c)
			{
				appendQuotedString(pattern, pos, sb, false);
				continue;
			}

			if (START_FE == c)
			{
				depth++;
				if (depth == 1)
				{
					fe++;
					sb.append(START_FE).append(readArgumentIndex(pattern, next(pos)));
					String customPattern = customPatterns.get(fe);
					if (customPattern != null)
					{
						sb.append(START_FMT).append(customPattern);
					}
				}
				continue;
			}

			if (END_FE == c)
			{
				depth--;
			}

			//$FALL-THROUGH$
			sb.append(c);
			next(pos);
		} while (pos.getIndex() < pattern.length());

		return sb.toString();

	}

	/**
	 * Convenience method to advance parse position by 1
	 * 
	 * @param pos
	 *            ParsePosition
	 * @return <code>pos</code>
	 */
	private ParsePosition next(ParsePosition pos)
	{

		pos.setIndex(pos.getIndex() + 1);
		return pos;

	}

	/**
	 * Parse the format component of a format element.
	 * 
	 * @param pattern
	 *            string to parse
	 * @param pos
	 *            current parse position
	 * @return Format description String
	 */
	private String parseFormatDescription(String pattern, ParsePosition pos)
	{

		int start = pos.getIndex();
		seekNonWs(pattern, pos);
		int text = pos.getIndex();
		int depth = 1;
		for (; pos.getIndex() < pattern.length(); next(pos))
		{
			char charAt = pattern.charAt(pos.getIndex());

			if (START_FE == charAt)
			{
				depth++;
				continue;
			}
			if (END_FE == charAt)
			{
				depth--;
				if (depth == 0)
				{
					return pattern.substring(text, pos.getIndex());
				}
				continue;
			}
			if (QUOTE == charAt)
			{
				getQuotedString(pattern, pos, false);
			}
		}

		throw new IllegalArgumentException("Unterminated format element at position " + start);

	}

	/**
	 * Read the argument index from the current format element
	 * 
	 * @param pattern
	 *            pattern to parse
	 * @param pos
	 *            current parse position
	 * @return argument index
	 */
	private int readArgumentIndex(String pattern, ParsePosition pos)
	{

		int start = pos.getIndex();
		seekNonWs(pattern, pos);
		StringBuffer result = new StringBuffer();
		boolean error = false;
		for (; !error && pos.getIndex() < pattern.length(); next(pos))
		{
			char c = pattern.charAt(pos.getIndex());
			if (Character.isWhitespace(c))
			{
				seekNonWs(pattern, pos);
				c = pattern.charAt(pos.getIndex());
				if (c != START_FMT && c != END_FE)
				{
					error = true;
					continue;
				}
			}
			if ((c == START_FMT || c == END_FE) && result.length() > 0)
			{
				try
				{
					return Integer.parseInt(result.toString());
				} catch (NumberFormatException e)
				{ // NOPMD
					// we've already ensured only digits, so unless something
					// outlandishly large was specified we should be okay.
				}
			}
			error = !Character.isDigit(c);
			result.append(c);
		}
		if (error)
		{
			throw new IllegalArgumentException("Invalid format argument index at position " + start + ": "
			        + pattern.substring(start, pos.getIndex()));
		}

		throw new IllegalArgumentException("Unterminated format element at position " + start);

	}

	/**
	 * Consume whitespace from the current parse position.
	 * 
	 * @param pattern
	 *            String to read
	 * @param pos
	 *            current position
	 */
	private void seekNonWs(String pattern, ParsePosition pos)
	{

		int len = 0;
		char[] buffer = pattern.toCharArray();

		do
		{
			len = Arrays.binarySearch(SPLIT_CHARS, buffer[pos.getIndex()]) >= 0 ? 1 : 0;
			pos.setIndex(pos.getIndex() + len);
		} while (len > 0 && pos.getIndex() < pattern.length());

	}

}
