package com.github.mfornos.humanize.text;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;

/**
 * <p>
 * MaskFormat is used to format and parse strings by the mean of a mask.
 * Underscore '_' is used as default place holder for the next message
 * character. The other characters within the mask are inserted between the
 * message symbols. The backslash '\' is escape symbol. Combination of '\?' will
 * include '?' in the human-readable message, where '?' can be any character
 * including underscore '_' and backslash '\'. Hash '#' can be used to skip
 * (delete) a character from the original message.
 * </p>
 * 
 * <h5>Examples:</h5>
 * <table border="0" cellspacing="0" cellpadding="3" width="100%">
 * <tr>
 * <th align="left">Input</th>
 * <th align="left">Mask</th>
 * <th align="left">Output</th>
 * </tr>
 * <tr>
 * <td>313378444416</td>
 * <td>_ _____ _____ _</td>
 * <td>3 13378 44441 6</td>
 * </tr>
 * <tr>
 * <td>A58818501</td>
 * <td>_-__-_____/_</td>
 * <td>A-58-81850/1</td>
 * </tr>
 * <tr>
 * <td>A/5881850 1</td>
 * <td>_# __ _____#-_</td>
 * <td>A 58 81850-1</td>
 * </tr>
 * </table>
 * 
 */
public class MaskFormat extends Format {

	private static final long serialVersionUID = -2072270263539296713L;

	private static final char DEFAULT_PLACEHOLDER = '_';

	public static String format(String mask, String str) {

		return format(mask, str, DEFAULT_PLACEHOLDER);

	}

	public static String format(String mask, String str, char placeholder) {

		return new MaskFormat(mask, placeholder).format(str);

	}

	public static String parse(String mask, String source) throws ParseException {

		return parse(mask, source, DEFAULT_PLACEHOLDER);

	}

	public static String parse(String mask, String source, char placeholder) throws ParseException {

		return new MaskFormat(mask, placeholder).parse(source);

	}

	private String mask;

	private char placeholder;

	public MaskFormat(String mask) {

		this(mask, DEFAULT_PLACEHOLDER);

	}

	public MaskFormat(String mask, char placeholder) {

		this.mask = mask;
		this.placeholder = placeholder;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer,
	 * java.text.FieldPosition)
	 */
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {

		if (obj == null)
			return null;

		return toAppendTo.append(this.format(obj.toString()));

	}

	public String format(String str) {

		if (isEmptyInput(mask, str)) {
			return str;
		}

		StringBuilder result = new StringBuilder();

		int msgIndex = 0;
		char maskChar;
		boolean isPlaceHolder;

		int i = 0;
		for (; i < mask.length(); i++) {

			maskChar = mask.charAt(i);
			isPlaceHolder = isPlaceholder(maskChar);

			if (!(isPlaceHolder || isDeleteholder(maskChar))) {
				result.append(isEscapeChar(maskChar) ? mask.charAt(++i) : maskChar);
			} else {
				if (isPlaceHolder) {
					result.append(str.charAt(msgIndex));
				}
				if (++msgIndex == str.length()) {
					break;
				}
			}

		}

		// Append tail
		while (++i < mask.length()) {
			result.append(mask.charAt(i));
		}

		return result.toString();

	}

	public String getMask() {

		return mask;
	}

	public char getPlaceholder() {

		return placeholder;
	}

	public String parse(String source) throws ParseException {

		return parse(source, (ParsePosition) null);

	}

	public String parse(String source, ParsePosition pos) throws ParseException {

		if (isEmptyInput(mask, source)) {
			return source;
		}

		StringBuilder sb = new StringBuilder(mask.length());

		for (int i = 0; i < mask.length() && i < source.length(); i++) {

			char maskChar = mask.charAt(i);
			if (isPlaceholder(maskChar)) {
				sb.append(source.charAt(i));
			} else if (!isEscapeChar(maskChar) && maskChar != source.charAt(i)) {
				throw new ParseException(String.format("Error parsing String: '%s' at %d", source, i), i);
			}

		}

		return sb.toString();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.text.Format#parseObject(java.lang.String)
	 */
	public Object parseObject(String source) throws ParseException {

		ParsePosition pos = new ParsePosition(0);
		Object result = parseObject(source, pos);
		if (pos.getErrorIndex() >= 0) {
			throw new ParseException("Format.parseObject(String) failed", pos.getErrorIndex());
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.text.Format#parseObject(java.lang.String,
	 * java.text.ParsePosition)
	 */
	public Object parseObject(String source, ParsePosition pos) {

		try {
			return parse(source, pos);
		} catch (ParseException e) {
			pos.setIndex(0);
			pos.setErrorIndex(e.getErrorOffset());
		}
		return null;

	}

	public void setMask(String mask) {

		this.mask = mask;

	}

	public void setPlaceholder(char placeholder) {

		this.placeholder = placeholder;
	}

	private boolean isDeleteholder(char c) {

		return c == '#';

	}

	private boolean isEmptyInput(String mask, String str) {

		return (mask == null || mask.length() == 0 || str == null || str.length() == 0);

	}

	private boolean isEscapeChar(char c) {

		return c == '\\';

	}

	private boolean isPlaceholder(char c) {

		return c == this.placeholder;

	}

}
