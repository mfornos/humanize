package humanize.emoji;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestEmojiInterpolator
{

    @Test
    public void emojiCharsTest()
    {

        String text = "Lorem ipsum \u2639 dolorem\uD83D\uDD36 and dolorem sit amet";
        String replaced = EmojiInterpolator.interpolateUnicode("<img src=\"imgs/{0}.png\" />", text);

        Assert.assertEquals(replaced,
                "Lorem ipsum <img src=\"imgs/2639.png\" /> dolorem<img src=\"imgs/1f536.png\" /> and dolorem sit amet");

    }

    @Test
    public void emojiAliasTest()
    {

        String text = "Lorem ipsum :sparkles: dolorem:star: and dolorem sit amet";
        String replaced = EmojiInterpolator.interpolateAlias("<img src=\"imgs/{0}.png\" title=\"{1}\" />", text);

        Assert.assertEquals(
                replaced,
                "Lorem ipsum <img src=\"imgs/2728.png\" title=\"sparkles\" /> dolorem<img src=\"imgs/2B50.png\" title=\"star\" /> and dolorem sit amet");

    }

}
