package humanize.emoji;

import humanize.emoji.EmojiInterpolator;
import humanize.text.util.InterpolationHelper;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestEmojiInterpolator
{

    @Test
    public void basic()
    {

        String text = "Lorem ipsum :sparkles: dolorem:star: and dolorem sit amet";
        String replaced = InterpolationHelper.interpolate(text, EmojiInterpolator.EMOJI_ALIAS,
                new EmojiInterpolator.EmojiAliasInterpolator(
                        "<img src=\"imgs/{0}.png\" title=\"{0}\" />"));

        Assert.assertEquals(
                replaced,
                "Lorem ipsum <img src=\"imgs/sparkles.png\" title=\"sparkles\" /> dolorem<img src=\"imgs/star.png\" title=\"star\" /> and dolorem sit amet");

    }

    @Test
    public void emojiChars()
    {

        String text = "Lorem ipsum \uE025 dolorem\uE30D and dolorem sit amet";
        String replaced = EmojiInterpolator.interpolateChars("<img src=\"imgs/{0}.png\" />", text);

        Assert.assertEquals(
                replaced,
                "Lorem ipsum <img src=\"imgs/uE025.png\" /> dolorem<img src=\"imgs/uE30D.png\" /> and dolorem sit amet");

    }

    @Test
    public void withHelper()
    {

        String text = "Lorem ipsum :sparkles: dolorem:star: and dolorem sit amet";
        String replaced = EmojiInterpolator.interpolateAlias("<img src=\"imgs/{0}.png\" title=\"{0}\" />", text);

        Assert.assertEquals(
                replaced,
                "Lorem ipsum <img src=\"imgs/sparkles.png\" title=\"sparkles\" /> dolorem<img src=\"imgs/star.png\" title=\"star\" /> and dolorem sit amet");

    }

}
