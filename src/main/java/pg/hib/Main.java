package pg.hib;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.hib.dao.*;
import pg.hib.entities.CarEntity;
import pg.hib.entities.TestEntity;
import pg.hib.providers.HibernateSessionProvider;

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
/*
        List<TestEntity> all = repository.findAll();
        LOGGER.info("This is what I got from db {}", all);

        all = repository.findByIds(Stream.of(1, 3).collect(toSet()));
        LOGGER.info("This is what I got from db {}", all);

        Optional<TestEntity> testBean = repository.findById(4);
        testBean.ifPresent(tb -> LOGGER.info("This is what I got from db {}", tb));

        List<TestEntity> activeEntities = repository.findByActive(true);
        LOGGER.info("Only active entities {}", activeEntities);

        repository.save(new TestEntity(false, LocalDateTime.now()));
        List<TestEntity> inactiveEntities = repository.findByActive(false);
        LOGGER.info("Only inactive entities {}", inactiveEntities);

        batchTestBeanSave(repository);*/
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

        /*
        CacheManager cacheManager = CacheManager.ALL_CACHE_MANAGERS.get(0);
        LOGGER.info("Cache Manager Name {}", cacheManager.getName());
        Cache carsCache = cacheManager.getCache("cars");
        LOGGER.info("Cars cache {}", carsCache.getSize());
        carsCache.getKeys().forEach(key -> LOGGER.info("Key - [{}]", key));

        Cache testCache = cacheManager.getCache("test_bean");
        LOGGER.info("Test bean cache {}", testCache.getSize());
        testCache.getKeys().forEach(key -> LOGGER.info("Key - [{}]", key));

        LOGGER.info("Getting cars by firstRegistrationDate.");
        LinkedList<CarEntity> carsByDate = new LinkedList<>(
                repository.findAllByFirstRegistrationDateAfter(LocalDateTime.now().minusWeeks(50))
        );
        carsByDate.forEach(System.out::println);

        LOGGER.info("Deleting cars from previous query.");
        boolean deleteAll = repository.deleteAll(carsByDate);
        LOGGER.info("Cars from previous query deleted [{}}].", deleteAll);

        LOGGER.info("Deleting by Ids [{}] cars.", carByIds.size());
        boolean deleteByIds = repository.deleteByIds(carByIds.stream().map(CarEntity::getId).collect(toSet()));
        LOGGER.info("Cars from previous query deleted [{}].", deleteByIds);

        carByIds = repository.findAll();
        LOGGER.info("There are: {} cars.", carByIds.size());

        final boolean queryResult = repository.executeUpdateQuery(
                "update cars c set active = NOT active",
                Collections.emptyMap()
        );
        LOGGER.info("Result is here {}", queryResult);

        final List<CarEntity> carEntities = repository.executeSelectQuery(
                "SELECT * FROM cars WHERE active = :active",
                Stream.of("active").collect(toMap(value -> value, value -> true)),
                fields -> new CarEntity(
                        Long.valueOf(String.valueOf(fields[0])),
                        Boolean.parseBoolean(String.valueOf(fields[1])),
                        new LocalDateTimeConverter().convertToEntityAttribute(String.valueOf(fields[2])),
                        new LocalDateTimeConverter().convertToEntityAttribute(String.valueOf(fields[3]))
                )
        );
        LOGGER.info("{}", carEntities);*/
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

}
