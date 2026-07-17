package util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/** Releases the JDBC connection pool when Tomcat stops or redeploys the app. */
@WebListener
public class DatabaseLifecycleListener implements ServletContextListener {
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DBUtil.shutdown();
    }
}
