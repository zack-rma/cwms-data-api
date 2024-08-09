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

package cwms.cda.api.project;

import static cwms.cda.api.Controllers.APPLICATION_MASK;
import static cwms.cda.api.Controllers.OFFICE_MASK;
import static cwms.cda.api.Controllers.PROJECT_MASK;
import static cwms.cda.api.project.ProjectLockHandlerUtil.buildTestProject;
import static cwms.cda.api.project.ProjectLockHandlerUtil.deleteProject;
import static cwms.cda.data.dao.DaoTest.getDslContext;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;

import cwms.cda.api.DataApiTestIT;
import cwms.cda.data.dao.project.ProjectDao;
import cwms.cda.data.dao.project.ProjectLockDao;
import cwms.cda.data.dto.project.Project;
import cwms.cda.formatters.Formats;
import fixtures.TestAccounts;
import io.restassured.filter.log.LogDetail;
import java.sql.SQLException;
import javax.servlet.http.HttpServletResponse;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class LockRevokerRightsCatalogHandlerIT extends DataApiTestIT {

    public static final String OFFICE = "SPK";

    String projId = "catRightsIT";
    String appId = "test_catRights";


    @BeforeEach
    void setup() throws SQLException {
        connectionAsWebUser(c -> {
            DSLContext dsl = getDslContext(c, OFFICE);
            ProjectLockDao lockDao = new ProjectLockDao(dsl);
            ProjectDao prjDao = new ProjectDao(dsl);

            Project testProject = buildTestProject(OFFICE, projId);
            prjDao.create(testProject, true);

            TestAccounts.KeyUser user = TestAccounts.KeyUser.SPK_NORMAL;
            String userName = user.getName();

            lockDao.removeAllLockRevokerRights(OFFICE, appId, userName);
            lockDao.allowLockRevokerRights(OFFICE, projId, appId, userName);
        });
    }

    @AfterEach
    void cleanup() throws SQLException {
        connectionAsWebUser(c -> {
            DSLContext dsl = getDslContext(c, OFFICE);

            ProjectLockDao lockDao = new ProjectLockDao(dsl);
            lockDao.removeAllLockRevokerRights(OFFICE, appId, TestAccounts.KeyUser.SPK_NORMAL.getName());

            deleteProject(dsl, projId, OFFICE, appId);
        });
    }


    @Test
    void test_allow_cat_deny_cat() throws SQLException {


        given()
            .log().ifValidationFails(LogDetail.ALL, true)
            .accept(Formats.JSON)
            .queryParam(OFFICE_MASK, OFFICE)
            .queryParam(PROJECT_MASK, projId)
            .queryParam(APPLICATION_MASK, appId)
        .when()
            .redirects().follow(true)
            .redirects().max(3)
            .get("/project-lock-rights/")
        .then()
            .log().ifValidationFails(LogDetail.ALL, true)
        .assertThat()
            .statusCode(is(HttpServletResponse.SC_OK))
            .body("$.size()", is(1))
            .body("[0].office-id", equalTo(OFFICE))
            .body("[0].project-id", equalTo(projId))
            .body("[0].application-id", equalToIgnoringCase(appId))  // actually lowercases it.
            .body("[0].user-id", equalTo(TestAccounts.KeyUser.SPK_NORMAL.getName()))
        ;

        denyRights();

        given()
            .log().ifValidationFails(LogDetail.ALL, true)
            .accept(Formats.JSON)
            .queryParam(OFFICE_MASK, OFFICE)
            .queryParam(PROJECT_MASK, projId)
            .queryParam(APPLICATION_MASK, appId)
        .when()
            .redirects().follow(true)
            .redirects().max(3)
            .get("/project-lock-rights/")
        .then()
            .log().ifValidationFails(LogDetail.ALL, true)
        .assertThat()
            .body("$.size()", is(0))
            .statusCode(is(HttpServletResponse.SC_OK))
        ;
    }

    private void denyRights() throws SQLException {
        connectionAsWebUser(c -> {
            DSLContext dsl = getDslContext(c, OFFICE);
            ProjectLockDao lockDao = new ProjectLockDao(dsl);

            lockDao.denyLockRevokerRights(OFFICE, projId, appId, TestAccounts.KeyUser.SPK_NORMAL.getName());
        });
    }

}
