package humanize.jsp;

import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

class RunServer {

	public static void main(String args[]) {

		new RunServer(8080);

	}

	private final Server server;

	public RunServer(int port) {

		System.out.println("Initializing server...");
		final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.setResourceBase("src/integration/webapp");

		context.setClassLoader(Thread.currentThread().getContextClassLoader());

		context.addServlet(DefaultServlet.class, "/");

		final ServletHolder jsp = context.addServlet(JspServlet.class, "*.jsp");
		jsp.setInitParameter("classpath", context.getClassPath());

		// add your own additional servlets like this:
		// context.addServlet(JSONServlet.class, "/json");

		server = new Server(port);
		server.setHandler(context);

		System.out.println("Starting server...");
		new Thread() {
			public void run() {

				try {
					server.start();
				} catch (Exception e) {
					System.out.println("Failed to start server!");
					return;
				}

				System.out.println("Server running...");
				while (true) {
					try {
						server.join();
					} catch (InterruptedException e) {
						System.out.println("Server interrupted!");
					}
				}
			};
		}.start();

	}

	public void stop() throws Exception {

		server.stop();

	}
}
