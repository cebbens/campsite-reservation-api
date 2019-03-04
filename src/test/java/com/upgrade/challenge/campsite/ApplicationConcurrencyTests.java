package com.upgrade.challenge.campsite;

import com.upgrade.challenge.campsite.api.Campsite;
import com.upgrade.challenge.campsite.api.CampsiteRepository;
import com.upgrade.challenge.campsite.api.availability.Availability;
import com.upgrade.challenge.campsite.api.availability.AvailabilityRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;
import javax.persistence.OptimisticLockException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//@DataJpaTest
//@TestExecutionListeners(listeners = {DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class})
//@RunWith(SpringRunner.class)
public class ApplicationConcurrencyTests {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private CampsiteRepository campsiteRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    private LocalDate checkIn;
    private LocalDate checkOut;
    private UUID campsiteId;

//    @BeforeEach
    public void setUp() {
        Campsite campsite = campsiteRepository.save(Campsite.builder().name("Test Campsite").capacity(10).build());

        campsiteId = campsite.getId();

        checkIn = LocalDate.now();
        checkOut = checkIn.plusDays(1);

//        availabilityRepository.save(Availability.builder().date(checkIn).sites(10).campsite(campsite).build());
    }

//    @Transactional
//    @Test(expected = OptimisticLockException.class)
    public void testUpdateAvailabilitySites() throws Exception {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

//        UUID campsiteId = UUID.randomUUID();
//        Campsite campsite = Campsite.builder().name("Test Campsite").capacity(10).build();
//        campsite.setId(campsiteId);
//        entityManager.merge(campsite);

        Campsite campsite = entityManager.merge(Campsite.builder().name("Test Campsite").capacity(10).build());

        entityManager.flush();

        Campsite campsiteEntity = entityManager.find(Campsite.class, campsite.getId(), LockModeType.PESSIMISTIC_WRITE);

        new Thread(() -> {
            EntityManager entityManager2 = entityManagerFactory.createEntityManager();
            entityManager2.getTransaction().begin();

            Campsite campsite2 = entityManager2.find(Campsite.class, campsite.getId(), LockModeType.PESSIMISTIC_WRITE);

            campsite2.setCapacity(7);

            entityManager2.getTransaction().commit();
        }, "thread-2").start();

        Thread.sleep(3000);

        campsiteEntity.setCapacity(5);
        entityManager.getTransaction().commit();
    }

    @Transactional
//    @Test(expected = OptimisticLockException.class)
    public void testUpdateAvailabilitySites0() throws Exception {
        List<Availability> availabilities = availabilityRepository.findAllByCampsiteIdAndDateBetween(campsiteId, checkIn, checkOut);

        new Thread(() -> {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();

            availabilityRepository.findAllByCampsiteIdAndDateBetween(campsiteId, checkIn, checkOut)
                    .forEach(availability -> {
                        availability.setSites(availability.getSites() - 1);
                        entityManager.persist(availability);
                    });

            entityManager.getTransaction().commit();
        }).start();

        Thread.sleep(1000);

        availabilities.forEach(availability -> {
            availability.setSites(availability.getSites() - 1);
            availabilityRepository.save(availability);
        });
    }

//    @Test//(expected = OptimisticLockException.class)
    public void testUpdateAvailabilitySites1() throws Exception {
        Campsite campsite = campsiteRepository.save(Campsite.builder().name("Test Campsite").capacity(10).build());

        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(1);

        availabilityRepository.save(Availability.builder().date(checkIn).sites(10).campsite(campsite).build());

        new Thread(() -> {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            availabilityRepository.findAllByCampsiteIdAndDateBetween(campsite.getId(), checkIn, checkOut)
                    .forEach(availability -> availability.setSites(availability.getSites() - 1));
            entityManager.getTransaction().commit();
        }).start();

        new Thread(() -> {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin();
            this.sleep(3000);
            availabilityRepository.findAllByCampsiteIdAndDateBetween(campsite.getId(), checkIn, checkOut)
                    .forEach(availability -> availability.setSites(availability.getSites() - 1));
            entityManager.getTransaction().commit();
        }).start();
    }

//    @Test//(expected = OptimisticLockException.class)
    public void testUpdateAvailabilitySites2() throws Exception {
        UUID campsiteId = UUID.fromString("6f4e5157-8521-4946-8d11-3316ecbefe15");
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(1);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.execute(() -> {
            TestTransaction.start();
            availabilityRepository.findAllByCampsiteIdAndDateBetween(campsiteId, checkIn, checkOut)
                    .forEach(availability -> availability.setSites(availability.getSites() - 1));
            TestTransaction.end();
        });

        executorService.execute(() -> {
            TestTransaction.start();
            this.sleep(3000);
            availabilityRepository.findAllByCampsiteIdAndDateBetween(campsiteId, checkIn, checkOut)
                    .forEach(availability -> availability.setSites(availability.getSites() - 1));
            TestTransaction.end();
        });

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
