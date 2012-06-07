package org.nikko.humanize.spi.number;

/**
 * <p>
 * Provider interface for number to text conversion.
 * </p>
 * 
 */
public interface NumberText {

	/**
	 * <p>
	 * Converts the given number to words.
	 * </p>
	 * 
	 * @param value
	 *            Number to be converted
	 * @return the number converted to words
	 */
	String toText(Number value);

}
