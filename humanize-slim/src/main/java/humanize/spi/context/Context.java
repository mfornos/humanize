package humanize.spi.context;

import humanize.text.MaskFormat;

import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

public interface Context
{

    String digitStrings(int index);

    String formatDate(int style, Date value);

    String formatDateTime(Date date);

    String formatDateTime(int dateStyle, int timeStyle, Date date);

    String formatDecimal(Number value);

    String formatMessage(String key, Object... args);

    ResourceBundle getBundle();

    Locale getLocale();

    MaskFormat getMaskFormat();

    String getMessage(String key);

    void setLocale(Locale locale);

}
