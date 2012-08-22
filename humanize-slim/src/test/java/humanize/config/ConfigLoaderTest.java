package humanize.config;

import java.net.URL;
import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.cache.CacheBuilderSpec;

public class ConfigLoaderTest {

	@Test
	public void defaultsTests() {

		Properties p = ConfigLoader.loadProperties("inexistent.properties");
		Assert.assertNotNull(p);
		String specStr = p.getProperty(ConfigLoader.CACHE_BUILDER_SPEC);
		Assert.assertEquals(specStr, "expireAfterAccess=1h");

		CacheBuilderSpec spec = CacheBuilderSpec.parse(specStr);
		Assert.assertNotNull(spec);

	}

	@Test
	public void loadTest() {

		Properties p = ConfigLoader.loadProperties();
		Assert.assertNotNull(p);
		String specStr = p.getProperty(ConfigLoader.CACHE_BUILDER_SPEC);
		Assert.assertEquals(specStr, "expireAfterAccess=15m");

		CacheBuilderSpec spec = CacheBuilderSpec.parse(specStr);
		Assert.assertNotNull(spec);

	}

	@Test
	public void locateTest() {

		URL url = ConfigLoader.locateConfig("humanize.properties");
		Assert.assertNotNull(url);
		Assert.assertTrue(url.toExternalForm().endsWith("humanize.properties"));
		
	}

	@Test
	public void systemPropertyTest() {

		System.setProperty("humanize.config", "humanize.alt.properties");
		Properties p = ConfigLoader.loadProperties();
		Assert.assertNotNull(p);
		String specStr = p.getProperty(ConfigLoader.CACHE_BUILDER_SPEC);
		Assert.assertEquals(specStr, "expireAfterAccess=30m");

		CacheBuilderSpec spec = CacheBuilderSpec.parse(specStr);
		Assert.assertNotNull(spec);

	}

	@Test
	public void unparseableSpecTest() {

		try {
			CacheBuilderSpec.parse("bla blablaba !");
			Assert.fail("Exception not thrown");
		} catch (IllegalArgumentException e) {

		}

	}

}
