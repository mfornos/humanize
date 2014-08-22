package humanize.emoji;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestEmojiApi
{

    private static final String SIMPLE_MOJI_TXT = "Lorem ipsum \u2639 dolorem\uD83D\uDD36 and dolorem sit amet";

    @BeforeMethod
    public void setUp()
    {
        EmojiApi.configure().assetsURL("http://cdn.emoji.com/img/");
    }

    @Test
    public void testByHexCode()
    {
        EmojiChar ec = EmojiApi.byHexCode("1f536");
        assertNotNull(ec);
    }

    @Test
    public void testByName()
    {
        EmojiChar ec = EmojiApi.byName("green heart");
        assertNotNull(ec);
    }

    @Test
    public void testByUnicode()
    {
        EmojiChar ec = EmojiApi.byUnicode("\uD83D\uDD36");
        assertNotNull(ec);
    }

    @Test
    public void testImageTagByUnicode()
    {
        String img = EmojiApi.imageTagByUnicode("\uD83D\uDD36");
        assertEquals(img,
                "<img class=\"emoji\" src=\"http://cdn.emoji.com/img/1f536.png\" " +
                        "alt=\"large orange diamond\" />");
    }

    @Test
    public void testImageTagByUnicodeRaw()
    {
        String img = EmojiApi.imageTagByUnicode("❤");
        assertEquals(img,
                "<img class=\"emoji\" src=\"http://cdn.emoji.com/img/2764.png\" " +
                        "alt=\"heavy black heart\" />");
    }

    @Test
    public void testReplaceUnicodeWithImagesAssetsURL()
    {
        String replaced = EmojiApi.replaceUnicodeWithImages(SIMPLE_MOJI_TXT);

        assertEquals(replaced,
                "Lorem ipsum <img class=\"emoji\" src=\"http://cdn.emoji.com/img/2639.png\" " +
                        "alt=\"white frowning face\" /> dolorem<img class=\"emoji\" " +
                        "src=\"http://cdn.emoji.com/img/1f536.png\" alt=\"large orange diamond\" /> " +
                        "and dolorem sit amet");

        replaced = EmojiApi.replaceUnicodeWithImages("I ❤ Emoji");

        assertEquals(replaced,
                "I <img class=\"emoji\" src=\"http://cdn.emoji.com/img/2764.png\" " +
                        "alt=\"heavy black heart\" /> Emoji");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testReplaceUnicodeWithImagesNoAssetsURLException()
    {
        EmojiApi.configure().assetsURL((String) null);

        EmojiApi.replaceUnicodeWithImages(SIMPLE_MOJI_TXT);
    }

    @Test
    public void testSearchByAnnotations()
    {
        List<EmojiChar> results = EmojiApi.search("cat", "smile");
        assertEquals(results.size(), 4);
    }
}
