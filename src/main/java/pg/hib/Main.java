package pg.hib;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.hib.dao.*;
import pg.hib.entities.CarEntity;
import pg.hib.entities.TestEntity;
import pg.hib.providers.HibernateSessionProvider;

import javax.cache.*;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        HibernateSessionProvider hibSessionProvider = HibernateSessionProvider.getInstance();
        SessionFactory sessionFactory = hibSessionProvider.getSessionFactory();

        playingWithTestBean(sessionFactory);
        playingWithCarEntity(sessionFactory);

        sessionFactory.close();
    }

    private static void playingWithTestBean(SessionFactory sessionFactory) {
        TestEntityDao repository = DaoFactory.getTestBeanRepository(sessionFactory);

        repository.deleteAll(repository.findAll());

        simpleOpr(repository);
    }

    private static void simpleOpr(TestEntityDao repository) {
        Optional<TestEntity> testBean = repository.save(new TestEntity(true, LocalDateTime.now()));
        testBean.ifPresent(bean -> LOGGER.info("This was saved {}.", bean));

        testBean = repository.findById(1);
        testBean.ifPresent(bean -> LOGGER.info("This was updated {}.", bean));

        testBean.ifPresent(bean -> {
            boolean delete = repository.delete(bean);
            LOGGER.info("Record with id {} deleted [{}].", bean.getEntityId(), delete);
        });
    }

    private static void batchTestBeanSave(TestEntityDao repository) {
        Random random = new Random();
        random.nextBoolean();
        List<TestEntity> beans = new LinkedList<>();
        for (int i = 0; i < 100; ++i) {
            beans.add(new TestEntity(random.nextBoolean(), LocalDateTime.now()));
        }
        List<TestEntity> testBean = repository.saveAll(beans);
        testBean.forEach(System.out::println);
    }

    private static void playingWithCarEntity(SessionFactory sessionFactory) {
        CarDao repository = DaoFactory.getCarRepository(sessionFactory);

        repository.deleteAll(repository.findAll());

        LOGGER.info("Creating cars with batch############.%n");
        List<Long> generateIds = batchCarSave(repository);

        Set<Serializable> ids = new HashSet<>(generateIds);
        LOGGER.info("Getting cars by ids {}", ids);
        List<CarEntity> carByIds = repository.findByIds(ids);
        carByIds.forEach(System.out::println);

        LOGGER.info("Getting cars by id {}", generateIds.get(0));
        repository.findById(generateIds.get(0)).ifPresent(e -> LOGGER.info("car: {}", e));
    }

    private static List<Long> batchCarSave(CarDao repository) {
        Random random = new Random();
        List<CarEntity> cars = new LinkedList<>();
        for (int i = 0; i < 10; ++i) {
            cars.add(new CarEntity(
                    random.nextBoolean(),
                    LocalDateTime.now(),
                    LocalDateTime.now().minusMonths(random.nextInt(250))
            ));
        }
        List<CarEntity> savedCars = repository.saveAll(cars);
        savedCars.forEach(System.out::println);
        return savedCars.stream().map(CarEntity::getId).collect(Collectors.toList());
    }

    private void play() {
        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();
        MutableConfiguration<String, String> config = new MutableConfiguration<>();
        Cache<String, String> cache = cacheManager.createCache("simpleCache", config);
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cacheManager.close();
    }

}
