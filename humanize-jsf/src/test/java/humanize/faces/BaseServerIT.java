package humanize.faces;


import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class BaseServerIT {

	private RunServer server;

	protected String baseUri = "http://127.0.0.1:7778";

	@BeforeSuite
	void beforeSuite() throws InterruptedException {

		server = new RunServer(7778);
		Thread.sleep(1000);

	}

	@AfterSuite
	void afterSuite() throws Exception {

		server.stop();

	}

}
