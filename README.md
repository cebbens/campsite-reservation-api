# campsite-reservation-api
Campsite REST API for managing campsite reservations.

#### Assumptions
- The API is open, no authentication nor user management is required.
- Each campsite has a maximum accommodation capacity which can only be defined
at Campsite creation time (no able to update later on).

#### Considerations
- More than one campsite is supported even though the challenge states that only a single campsite can be hosted in the island.
- Campsite availability is represented by available sites for each date. Once a reservation is made for a given date, the Availability’s sites is decremented for that date. Likewise, if the reservation is cancelled, it is incremented.
- Campsite reservations are retrieved directly, without going through Availability. That is the only reason as to why there is a direct relationship between Campsite and Reservation.
- Availability’s sites could removed altogether and be calculated through the reservations linked to each availability. But then, it would not be so performant as having it stored and updated on every reservation made or cancelled. In short, sites is there only for performance purposes.
Test cases covers all endpoints’ happy paths, and some exception paths. One can run them by executing _mvn test_.
- Concurrent requests handling to reserve the campsite for the same/overlapping date(s) is supported through Optimistic Locking at the DB level via a version field and through Spring transactional isolation level configuration. The same goes for every entity that can be updated concurrently, not just availability. Attempts were made to create test cases for such scenario, but unfortunately could not make it work (could be found under _ApplicationConcurrencyTests_). I decided to leave them (are commented, they will not run) as reference.
- Campsite availability retrieving load/stress test were made with _Taurus_. The configuration file for executing the test is under _src/test/resources/campsite.yml_. After installing Taurus (brew install bzt), one can run the test by executing _bzt -report ./src/test/resources/campsite.yml. Test results: Average throughput ~2000 hits/sec and average response time ~0,35 ms under max. 1000 concurrent users.
- I18n is supported.
- A campsite (id=9045b895-f634-4f2b-997c-fe5a2dbe8126) is automatically created at server startup (data.sql) and its availability initialized for a whole year. Moreover, a scheduled task is run once a year to keep availability data initialized for a whole year for all campsites.

#### Tech Stack
- Java 1.8
- Spring Boot 2.1.2
  - Spring MVC ○ Hibernate
  - Logback
  - JUnit
- H2 1.4.197
- Lombok 1.18.4
- Rest Assured 3.3.0
- Springfox Swagger 2.9.2

#### Execution
- Start server: _mvn spring-boot:run_
- Run integration test cases: _mvn test_
- Run stress/load test case: _bzt -report ./src/test/resources/campsite.yml_

#### Configuration
- Base path (api.base-path. Default: “/api/v1").
- Reservation max. days (api.campsite.reservation.max-days. Default: 3).
- Reservation min. days ahead of arrival (api.campsite.reservation.min-days-ahead.
Default: 1).
- Reservation max. days ahead of arrival (api.campsite.reservation.max-days-ahead. Default: 30).
- Availability range threshold (api.campsite.availability.range-threshold. Default: 30).

#### TODOs
- Add L2 Cache
- Integrate stress/load test (Taurus)
- Tweak DB connection pool (Hikari)
