package com.github.mfornos.humanize.spi.context;

import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import com.github.mfornos.humanize.spi.MessageFormat;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.DurationFormat;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.util.ULocale;

public interface Context {

	String digitStrings(int index);

	String formatDate(int style, Date value);

	String formatDateTime(Date date);

	String formatDateTime(int dateStyle, int timeStyle, Date date);

	String formatDecimal(Number value);

	String formatMessage(String key, Object... args);

	String getBestPattern(String skeleton);

	ResourceBundle getBundle();

	DecimalFormat getCurrencyFormat();

	DateFormat getDateFormat(int style);

	DateFormat getDateTimeFormat();

	DateFormat getDateTimeFormat(int dateStyle, int timeStyle);

	DecimalFormat getDecimalFormat();

	DurationFormat getDurationFormat();

	Locale getLocale();

	String getMessage(String key);

	MessageFormat getMessageFormat();

	NumberFormat getNumberFormat();

	DecimalFormat getPercentFormat();

	DecimalFormat getPluralCurrencyFormat();

	NumberFormat getRuleBasedNumberFormat(int type);

	ULocale getULocale();

	void setLocale(Locale locale);

}
