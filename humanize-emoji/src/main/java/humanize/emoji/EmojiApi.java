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
    private static class LazyHolder
    {
        private static final EmojiApi INSTANCE = new EmojiApi();
    }

    private static final String DEFAULT_IMG_FMT = "<img class=\"emoji\" src=\"%s/{0}.%s\" alt=\"{1}\" />";
    private static final String DEFAULT_EXT = "png";

    private static final Joiner SPACE_JOINER = Joiner.on(' ');

    /**
     * Finds an Emoji character by its hexadecimal code.
     * 
     * @param hex
     *            The hexadecimal code. E.g.: "1f536"
     * @return an instance of EmojiChar for the given hex code, or null if not
     *         found
     * @see Emoji#findByHexCode(String)
     */
    public static EmojiChar byHexCode(String hex)
    {
        return Emoji.findByHexCode(hex);
    }

    /**
     * Finds an Emoji character by its name.
     * 
     * @param name
     *            The Emoji character name
     * @return an instance of EmojiChar for the given name, or null if not found
     * @see Emoji#singleByAnnotations(String)
     */
    public static EmojiChar byName(String name)
    {
        return Emoji.singleByAnnotations(name);
    }

    /**
     * Finds an Emoji character by its code point.
     * 
     * @param codePoint
     *            The Unicode code point. E.g.: "\uD83D\uDD36"
     * @return an instance of EmojiChar for the given code point, or null if not
     *         found
     * @see Emoji#findByCodePoint(String)
     */
    public static EmojiChar byUnicode(String codePoint)
    {
        return Emoji.findByCodePoint(codePoint);
    }

    /**
     * Returns the underlying EmojiApi instance. Intended to chain
     * configurations.
     * 
     * @return the underlying EmojiApi instance
     */
    public static EmojiApi configure()
    {
        return getInstance();
    }

    /**
     * Interpolates a code point to build an image tag representation suitable
     * to be included in an hypertext document.
     * 
     * @param codePoint
     *            The Unicode code point
     * @return an interpolated image tag for the given code point
     */
    public static String imageTagByUnicode(String codePoint)
    {
        return getInstance().interpolate(codePoint);
    }

    /**
     * Replaces all Emoji characters by its HTML image tag, in the text
     * provided.
     * 
     * @param text
     *            The text to be interpolated
     * @return the text with all the Emoji characters replaced by its HTML image
     *         tag
     */
    public static String replaceUnicodeWithImages(String text)
    {
        return getInstance().interpolate(text);
    }

    /**
     * Finds matching Emoji character by its annotated metadata.
     * 
     * @param annotations
     *            The annotation tags to be matched
     * @return A list containing the instances of the marching characters
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
     * Configures the URL for image tag interpolation methods.
     * 
     * @param assetsURL
     *            The base url to be used in the image tag. E.g.
     *            "https://127.0.0.1/img"
     * @return the underlying EmojiApi instance for further chainning
     */
    public EmojiApi assetsURL(String assetsURL)
    {
        this.assetsURL = assetsURL;
        return reset();
    }

    /**
     * Configures the URL for image tag interpolation methods.
     * 
     * @param assetsURL
     *            The base url to be used in the image tag. E.g.
     *            "https://127.0.0.1/img"
     * @return the underlying EmojiApi instance for further chainning
     */
    public EmojiApi assetsURL(URL assetsURL)
    {
        return assetsURL(assetsURL.toExternalForm());
    }

    /**
     * Configures the format to be used in the image interpolation methods.
     * 
     * @param format
     *            The format to be used while building the image tag. Defaults:
     *            "&lt;img class=\"emoji\" src=\"%s/{0}.%s\" alt=\"{1}\" /&gt;"
     * @return the underlying EmojiApi instance for further chainning
     */
    public EmojiApi format(String format)
    {
        this.tagFormat = format;
        return reset();
    }

    /**
     * Gets the configured assets URL to build image tags.
     * 
     * @return the current assets URL for image tags
     */
    public String getAssetsURL()
    {
        return assetsURL;
    }

    /**
     * Gets the configured format to build image tags.
     * 
     * @return the current format for image tags
     */
    public String getFormat()
    {
        return tagFormat;
    }

    /**
     * Gets the configured image extension to build image tags.
     * 
     * @return the current image extension
     */
    public String getImageExtension()
    {
        return imageExt;
    }

    /**
     * Configures the extension for image interpolation methods.
     * 
     * @param imageExt
     *            The image extension. Defaults: "png"
     * @return the underlying EmojiApi instance for further chainning
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

}
