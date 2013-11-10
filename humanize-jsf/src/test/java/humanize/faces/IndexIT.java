package humanize.faces;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class IndexIT extends BaseServerIT
{

    @Test
    public void homePage() throws Exception
    {

        final WebClient webClient = new WebClient();
        WebClientOptions options = webClient.getOptions();
        options.setJavaScriptEnabled(false);
        options.setCssEnabled(false);

        final HtmlPage page = webClient.getPage(baseUri);
        Assert.assertEquals(page.getTitleText(), "Humanize JSF Example");

        final String pageAsXml = page.asXml();
        Assert.assertTrue(pageAsXml.contains("<div class=\"wrapper\">"));

        final String pageAsText = page.asText();
        Assert.assertTrue(pageAsText.contains("hmnz:duration"));

        webClient.closeAllWindows();

    }
}
