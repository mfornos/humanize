package humanize.emoji;

import static humanize.emoji.EmojiInterpolator.createEmojiInterpolator;
import humanize.text.util.UnicodeInterpolator;

import java.net.URL;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Exposes a concise Emoji API for common use cases.
 * 
 */
public final class EmojiApi
{
    private static final String DEFAULT_IMG_FMT = "<img class=\"emoji\" src=\"%s/{0}.%s\" alt=\"{1}\" />";
    private static final String DEFAULT_EXT = "png";
    private static final Joiner SPACE_JOINER = Joiner.on(' ');

    /**
     * @param name
     * @return
     */
    public static EmojiChar byName(String name)
    {
        return Emoji.singleByAnnotations(name);
    }

    /**
     * @return
     */
    public static EmojiApi configure()
    {
        return getInstance();
    }

    /**
     * @param codePoint
     * @return
     */
    public static String imageTagByUnicode(String codePoint)
    {
        return getInstance().interpolate(codePoint);
    }

    /**
     * @param text
     * @return
     */
    public static String replaceUnicodeWithImages(String text)
    {
        return getInstance().interpolate(text);
    }

    /**
     * @param annotations
     * @return
     */
    public static List<EmojiChar> search(String... annotations)
    {
        return Emoji.findByAnnotations(SPACE_JOINER.join(annotations));
    }

    private static EmojiApi getInstance()
    {
        return LazyHolder.INSTANCE;
    }

    private String tagFormat;

    private UnicodeInterpolator interpolator;
    private String imageExt;
    private String assetsURL;

    private EmojiApi()
    {
        this.imageExt = DEFAULT_EXT;
        this.tagFormat = DEFAULT_IMG_FMT;
    }

    /**
     * @param assetsURL
     * @return
     */
    public EmojiApi assetsURL(String assetsURL)
    {
        this.assetsURL = assetsURL;
        return reset();
    }

    /**
     * @param assetsURL
     * @return
     */
    public EmojiApi assetsURL(URL assetsURL)
    {
        return assetsURL(assetsURL.toExternalForm());
    }

    /**
     * @param format
     * @return
     */
    public EmojiApi format(String format)
    {
        this.tagFormat = format;
        return reset();
    }

    /**
     * @return
     */
    public String getAssetsURL()
    {
        return assetsURL;
    }

    /**
     * @return
     */
    public String getFormat()
    {
        return tagFormat;
    }

    /**
     * @return
     */
    public String getImageExtension()
    {
        return imageExt;
    }

    /**
     * @param imageExt
     * @return
     */
    public EmojiApi imageExtension(String imageExt)
    {
        this.imageExt = imageExt;
        return reset();
    }

    private String getImageFormat(String assetsURL)
    {
        String base = removeTrailingSlashes(assetsURL);
        return String.format(tagFormat, base, imageExt);
    }

    private UnicodeInterpolator getInterpolator()
    {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(assetsURL),
                "Please, specify an assets URL using" +
                        " EmojiApi.getConfiguration().assetsURL() method." +
                        "Example: api.assetsURL(\"http://localhost:8000/imgs\")");

        if (interpolator == null)
        {
            interpolator = createEmojiInterpolator(getImageFormat(assetsURL));
        }

        return interpolator;
    }

    private String interpolate(String text)
    {
        if (Strings.isNullOrEmpty(text))
        {
            return text;
        }
        return getInterpolator().escape(text);
    }

    private String removeTrailingSlashes(String str)
    {
        int i = str.length() - 1;
        if (str.charAt(i) == '/')
        {
            return removeTrailingSlashes(str.substring(0, i));
        } else
        {
            return str;
        }
    }

    private EmojiApi reset()
    {
        this.interpolator = null;
        return this;
    }

    private static class LazyHolder
    {
        private static final EmojiApi INSTANCE = new EmojiApi();
    }

    /**
     * @param codePoint
     * @return
     */
    public static EmojiChar byUnicode(String codePoint)
    {
        return Emoji.findByCodePoint(codePoint);
    }
    
    /**
     * @param hex
     * @return
     */
    public static EmojiChar byHexCode(String hex)
    {
        return Emoji.findByHexCode(hex);
    }

}
