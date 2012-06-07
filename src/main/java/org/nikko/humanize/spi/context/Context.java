package org.nikko.humanize.spi.context;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import org.nikko.humanize.spi.Message;
import org.nikko.humanize.spi.number.NumberText;
import org.nikko.humanize.util.RelativeDate;

public interface Context {

	String digitStrings(int index);

	String formatDate(int style, Date value);

	String formatDateTime(Date date);

	String formatDateTime(int dateStyle, int timeStyle, Date date);

	String formatDecimal(Number value);

	String formatMessage(String key, Object... args);

	String formatRelativeDate(Date duration);

	String formatRelativeDate(Date reference, Date duration);

	ResourceBundle getBundle();

	DecimalFormat getCurrencyFormat();

	DateFormat getDateFormat(int style);

	DateFormat getDateTimeFormat();

	DateFormat getDateTimeFormat(int dateStyle, int timeStyle);

	DecimalFormat getDecimalFormat();

	Locale getLocale();

	String getMessage(String key);

	Message getMessageFormat();

	NumberFormat getNumberFormat();

	NumberText getNumberText();

	DecimalFormat getPercentFormat();

	RelativeDate getRelativeDate();

	String ordinalSuffix(int index);

	void setLocale(Locale locale);

	String toText(Number value);

}
