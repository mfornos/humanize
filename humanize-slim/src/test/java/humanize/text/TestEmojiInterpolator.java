package humanize.text;

import org.testng.Assert;
import org.testng.annotations.Test;

import humanize.text.EmojiInterpolator;
import humanize.text.util.InterpolationHelper;

public class TestEmojiInterpolator {

	@Test
	public void basic() {

		String text = "Lorem ipsum :sparkles: dolorem:star: and dolorem sit amet";
		String replaced = InterpolationHelper.interpolate(text, EmojiInterpolator.EMOJI, new EmojiInterpolator(
		        "<img src=\"imgs/{0}.png\" title=\"{0}\" />"));

		Assert.assertEquals(
		        replaced,
		        "Lorem ipsum <img src=\"imgs/sparkles.png\" title=\"sparkles\" /> dolorem<img src=\"imgs/star.png\" title=\"star\" /> and dolorem sit amet");

	}

	@Test
	public void withHelper() {

		String text = "Lorem ipsum :sparkles: dolorem:star: and dolorem sit amet";
		String replaced = EmojiInterpolator.interpolate("<img src=\"imgs/{0}.png\" title=\"{0}\" />", text);

		Assert.assertEquals(
		        replaced,
		        "Lorem ipsum <img src=\"imgs/sparkles.png\" title=\"sparkles\" /> dolorem<img src=\"imgs/star.png\" title=\"star\" /> and dolorem sit amet");

	}

}
