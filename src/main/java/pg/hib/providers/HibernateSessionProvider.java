package pg.hib.providers;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;
import pg.hib.entities.CarEntity;
import pg.hib.entities.TestEntity;

import java.util.Properties;

public class HibernateSessionProvider {

    private static SessionFactory sessionFactory;

    private HibernateSessionProvider() { }

    private static class HibernateSessionProviderHolder {
        private static final HibernateSessionProvider INSTANCE = new HibernateSessionProvider();
    }

    public static HibernateSessionProvider getInstance() {
        return HibernateSessionProviderHolder.INSTANCE;
    }

    public SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            Properties settings = new Properties();
            settings.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");
            settings.put(Environment.URL, "jdbc:mysql://localhost:3306/ehcache");
            settings.put(Environment.USER, "root");
            settings.put(Environment.PASS, "MysqL12!@");
            settings.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
            settings.put(Environment.SHOW_SQL, "true");
            settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
            settings.put(Environment.HBM2DDL_AUTO, "update");
            settings.put("hibernate.cache.use_second_level_cache", "true");
            //settings.put("hibernate.cache.use_query_cache", "true");
            settings.put(Environment.CACHE_REGION_FACTORY, "org.hibernate.cache.jcache.JCacheRegionFactory");
            settings.put("hibernate.javax.cache.provider", "org.ehcache.jsr107.EhcacheCachingProvider");
            settings.put("net.sf.ehcache.configurationResourceName", "/ehcache/ehcache.xml");
            settings.put("hibernate.javax.cache.missing_cache_strategy", "create");

            Configuration cfg = new Configuration()
                    .setProperties(settings)
                    .addAnnotatedClass(TestEntity.class)
                    .addAnnotatedClass(CarEntity.class);

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(cfg.getProperties())
                    .build();

            sessionFactory = cfg.buildSessionFactory(serviceRegistry);
        }
        return sessionFactory;
    }
}
