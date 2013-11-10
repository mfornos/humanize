package humanize.jsp;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class BaseServerIT
{

    private RunServer server;

    protected String baseUri = "http://127.0.0.1:7778";

    @AfterSuite
    void afterSuite() throws Exception
    {

        server.stop();

    }

    @BeforeSuite
    void beforeSuite()
    {

        server = new RunServer(7778);

    }

}
