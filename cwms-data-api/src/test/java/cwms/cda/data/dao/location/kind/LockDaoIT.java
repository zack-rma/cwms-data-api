/*
 * MIT License
 *
 * Copyright (c) 2024 Hydrologic Engineering Center
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package cwms.cda.data.dao.location.kind;

import static cwms.cda.data.dao.DaoTest.getDslContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cwms.cda.api.errors.NotFoundException;
import cwms.cda.data.dao.DeleteRule;
import cwms.cda.data.dao.LocationsDaoImpl;
import cwms.cda.data.dto.CwmsId;
import cwms.cda.data.dto.Location;
import cwms.cda.data.dto.location.kind.Lock;
import cwms.cda.helpers.DTOMatch;
import fixtures.CwmsDataApiSetupCallback;
import java.io.IOException;
import java.util.List;
import mil.army.usace.hec.test.database.CwmsDatabaseContainer;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@Tag("integration")
@TestInstance(Lifecycle.PER_CLASS)
final class LockDaoIT extends ProjectStructureIT {
    private static final String LOCK_KIND = "LOCK";
    private static final Location LOCK_LOC1 = buildProjectStructureLocation("PROJECT-LOCK_LOC1_IT", LOCK_KIND);
    private static final Location LOCK_LOC2 = buildProjectStructureLocation("LOCK_LOC2_IT", LOCK_KIND);
    private static final Location LOCK_LOC3 = buildProjectStructureLocation("LOCK_LOC3_IT", LOCK_KIND);

    @BeforeAll
    public void setup() throws Exception {
        setupProject();
        CwmsDatabaseContainer<?> databaseLink = CwmsDataApiSetupCallback.getDatabaseLink();
        databaseLink.connection(c -> {
                DSLContext context = getDslContext(c, OFFICE_ID);
                LocationsDaoImpl locationsDao = new LocationsDaoImpl(context);
                try {
                    locationsDao.storeLocation(LOCK_LOC1);
                    locationsDao.storeLocation(LOCK_LOC2);
                    locationsDao.storeLocation(LOCK_LOC3);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            },
            CwmsDataApiSetupCallback.getWebUser());
    }

    @AfterAll
    public void tearDown() throws Exception {
        tearDownProject();
        CwmsDatabaseContainer<?> databaseLink = CwmsDataApiSetupCallback.getDatabaseLink();
        databaseLink.connection(c -> {
                DSLContext context = getDslContext(c, OFFICE_ID);
                LocationsDaoImpl locationsDao = new LocationsDaoImpl(context);
                try {
                    locationsDao.deleteLocation(LOCK_LOC1.getName(), OFFICE_ID, true);
                    locationsDao.deleteLocation(LOCK_LOC2.getName(), OFFICE_ID, true);
                    locationsDao.deleteLocation(LOCK_LOC3.getName(), OFFICE_ID, true);
                } catch (NotFoundException ex) {
                    /* only an error within the tests below. */
                }
            },
            CwmsDataApiSetupCallback.getWebUser());
    }

    @Test
    void testRoundTrip() throws Exception {
        CwmsDatabaseContainer<?> databaseLink = CwmsDataApiSetupCallback.getDatabaseLink();
        databaseLink.connection(c -> {
                DSLContext context = getDslContext(c, OFFICE_ID);
                LockDao lockDao = new LockDao(context);
                Lock lock = buildTestLock(LOCK_LOC1, PROJECT_LOC.getName());
                lockDao.storeLock(lock, false);
                String lockId = lock.getLocation().getName();
                String lockOfficeId = lock.getLocation().getOfficeId();
                CwmsId cwmsId = CwmsId.buildCwmsId(lockOfficeId, lockId);
                Lock retrievedLock = lockDao.retrieveLock(cwmsId);
                DTOMatch.assertMatch(lock, retrievedLock);
                lockDao.deleteLock(cwmsId, DeleteRule.DELETE_ALL);
                assertThrows(NotFoundException.class, () -> lockDao.retrieveLock(cwmsId));
            },
            CwmsDataApiSetupCallback.getWebUser());
    }

    @Test
    void testRoundTripMulti() throws Exception {
        CwmsDatabaseContainer<?> databaseLink = CwmsDataApiSetupCallback.getDatabaseLink();
        databaseLink.connection(c -> {
                DSLContext context = getDslContext(c, OFFICE_ID);
                LockDao lockDao = new LockDao(context);
                Lock lock1 = buildTestLock(LOCK_LOC1, PROJECT_LOC.getName());
                lockDao.storeLock(lock1, false);
                Lock lock2 = buildTestLock(LOCK_LOC2, PROJECT_LOC.getName());
                lockDao.storeLock(lock2, false);
                Lock lock3 = buildTestLock(LOCK_LOC3, PROJECT_LOC2.getName());
                lockDao.storeLock(lock2, false);
                String lockId = lock2.getLocation().getName();
                String lockOfficeId = lock2.getLocation().getOfficeId();
                CwmsId projectId = CwmsId.buildCwmsId(lock1.getProjectId().getOfficeId(), lock1.getProjectId().getName());
                List<CwmsId> retrievedLock = lockDao.retrieveLockIds(projectId);
                assertEquals(2, retrievedLock.size());
                assertTrue(retrievedLock.stream()
                    .anyMatch(e -> e.getName().equalsIgnoreCase(lock1.getLocation().getName())));
                assertTrue(retrievedLock.stream()
                    .anyMatch(e -> e.getName().equalsIgnoreCase(lock2.getLocation().getName())));
                assertFalse(retrievedLock.stream()
                    .anyMatch(e -> e.getName().equalsIgnoreCase(lock3.getLocation().getName())));
                CwmsId cwmsId = CwmsId.buildCwmsId(lockOfficeId, lockId);
                lockDao.deleteLock(cwmsId, DeleteRule.DELETE_ALL);
                assertThrows(NotFoundException.class, () -> lockDao.retrieveLock(cwmsId));
            },
            CwmsDataApiSetupCallback.getWebUser());
    }

    @Test
    void testRename() throws Exception {
        CwmsDatabaseContainer<?> databaseLink = CwmsDataApiSetupCallback.getDatabaseLink();
        databaseLink.connection(c -> {
                DSLContext context = getDslContext(c, OFFICE_ID);
                LockDao lockDao = new LockDao(context);
                Lock lock = buildTestLock(LOCK_LOC1, PROJECT_LOC.getName());
                lockDao.storeLock(lock, false);
                String originalId = lock.getLocation().getName();
                String office = lock.getLocation().getOfficeId();
                String newId = lock.getLocation().getName() + "New";
                CwmsId cwmsId = CwmsId.buildCwmsId(office, originalId);
                lockDao.renameLock(cwmsId, newId);
                assertThrows(NotFoundException.class, () -> lockDao.retrieveLock(cwmsId));
                CwmsId newCwmsId = CwmsId.buildCwmsId(office, newId);
                Lock retrievedLock = lockDao.retrieveLock(newCwmsId);
                assertEquals(newId, retrievedLock.getLocation().getName());
                lockDao.deleteLock(newCwmsId, DeleteRule.DELETE_ALL);
            },
            CwmsDataApiSetupCallback.getWebUser());
    }

    private static Lock buildTestLock(Location location, String projectId) {
        return new Lock.Builder()
            .withLocation(location)
            .withProjectId(new CwmsId.Builder()
                .withName(projectId)
                .withOfficeId(PROJECT_LOC.getOfficeId())
                .build())
            //TODO fill out the rest of the properties - subject to change based on https://jira.hecdev.net/browse/CTO-147
            .build();
    }
}
