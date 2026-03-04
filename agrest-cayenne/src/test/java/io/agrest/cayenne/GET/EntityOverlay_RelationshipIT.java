package io.agrest.cayenne.GET;

import io.agrest.DataResponse;
import io.agrest.cayenne.CayenneResolvers;
import io.agrest.cayenne.cayenne.main.E34;
import io.agrest.cayenne.cayenne.main.E35;
import io.agrest.cayenne.unit.main.MainDbTest;
import io.agrest.cayenne.unit.main.MainModelTester;
import io.agrest.jaxrs3.AgJaxrs;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.protocol.Exp;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

public class EntityOverlay_RelationshipIT extends MainDbTest {

    @BQTestTool
    static final MainModelTester tester = tester(Resource.class)
            .entities(E34.class, E35.class)
            .build();

    @Test
    public void includeOverlay() {
        tester.e34().insertColumns("id", "name")
                .values(1, "Alice")
                .values(2, "Bob")
                .exec();

        tester.e35().insertColumns("id", "e34_id", "season", "ranking")
                .values(1, 1, "2025/2026", 3)
                .values(2, 2, "2024/2025", 2)
                .exec();

        tester.target("/e34")
                .queryParam("season", "2025/2026")
                .queryParam("include", "currentList")
                .get()
                .wasOk()
                .bodyEquals(2,
                        "{\"id\":1,\"currentList\":[{\"id\":1,\"ranking\":3,\"season\":\"2025/2026\"}],\"name\":\"Alice\"}",
                        "{\"id\":2,\"currentList\":[],\"name\":\"Bob\"}");
    }

    @Test
    public void includeOverlay_MultipleOverlays() {
        tester.e34().insertColumns("id", "name")
                .values(1, "Alice")
                .values(2, "Bob")
                .exec();

        tester.e35().insertColumns("id", "e34_id", "season", "ranking")
                .values(1, 1, "2024/2025", 1)
                .values(2, 1, "2025/2026", 3)
                .values(3, 2, "2024/2025", 2)
                .exec();

        tester.target("/e34/two-seasons")
                .queryParam("currentSeason", "2025/2026")
                .queryParam("previousSeason", "2024/2025")
                .queryParam("include", "[\"currentList\",\"prevList\"]")
                .get()
                .wasOk()
                .bodyEquals(2,
                        "{\"id\":1,\"currentList\":[{\"id\":2,\"ranking\":3,\"season\":\"2025/2026\"}],\"name\":\"Alice\",\"prevList\":[{\"id\":1,\"ranking\":1,\"season\":\"2024/2025\"}]}",
                        "{\"id\":2,\"currentList\":[],\"name\":\"Bob\",\"prevList\":[{\"id\":3,\"ranking\":2,\"season\":\"2024/2025\"}]}");
    }

    @Test
    public void includeOverlay_DifferentParametersPerRequest() {
        tester.e34().insertColumns("id", "name")
                .values(1, "Alice")
                .exec();

        tester.e35().insertColumns("id", "e34_id", "season", "ranking")
                .values(1, 1, "2024/2025", 1)
                .values(2, 1, "2025/2026", 3)
                .exec();

        tester.target("/e34")
                .queryParam("season", "2024/2025")
                .queryParam("include", "currentList")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":1,\"currentList\":[{\"id\":1,\"ranking\":1,\"season\":\"2024/2025\"}],\"name\":\"Alice\"}");

        tester.target("/e34")
                .queryParam("season", "2025/2026")
                .queryParam("include", "currentList")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":1,\"currentList\":[{\"id\":2,\"ranking\":3,\"season\":\"2025/2026\"}],\"name\":\"Alice\"}");
    }

    @Test
    public void sortByOverlay_Ascending() {
        tester.e34().insertColumns("id", "name")
                .values(1, "Alice")
                .values(2, "Charlie")
                .values(3, "David")
                .exec();

        tester.e35().insertColumns("id", "e34_id", "season", "ranking")
                .values(1, 1, "2025/2026", 3)
                .values(2, 2, "2025/2026", 2)
                .values(3, 3, "2025/2026", 1)
                .exec();

        tester.target("/e34")
                .queryParam("season", "2025/2026")
                .queryParam("sort", "currentList.ranking")
                .get()
                .wasOk()
                .bodyEquals(3,
                        "{\"id\":3,\"name\":\"David\"}",
                        "{\"id\":2,\"name\":\"Charlie\"}",
                        "{\"id\":1,\"name\":\"Alice\"}");
    }

    @Test
    public void sortByMultipleOverlays() {
        tester.e34().insertColumns("id", "name")
                .values(1, "Alice")
                .values(2, "Charlie")
                .exec();

        tester.e35().insertColumns("id", "e34_id", "season", "ranking")
                .values(1, 1, "2024/2025", 1)
                .values(2, 1, "2025/2026", 3)
                .values(3, 2, "2024/2025", 3)
                .values(4, 2, "2025/2026", 2)
                .exec();

        tester.target("/e34/two-seasons")
                .queryParam("currentSeason", "2025/2026")
                .queryParam("previousSeason", "2024/2025")
                .queryParam("sort", "[{\"path\":\"currentList.ranking\"},{\"path\":\"prevList.ranking\"}]")
                .get()
                .wasOk()
                .bodyEquals(2,
                        "{\"id\":2,\"name\":\"Charlie\"}",
                        "{\"id\":1,\"name\":\"Alice\"}");
    }

    @Test
    public void filterByOverlay() {
        tester.e34().insertColumns("id", "name")
                .values(1, "Alice")
                .values(2, "Charlie")
                .exec();

        tester.e35().insertColumns("id", "e34_id", "season", "ranking")
                .values(1, 1, "2025/2026", 3)
                .values(2, 2, "2025/2026", 1)
                .exec();

        tester.target("/e34")
                .queryParam("season", "2025/2026")
                .queryParam("exp", "currentList.ranking < 3")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":2,\"name\":\"Charlie\"}");
    }

    @Test
    public void filterByMultipleOverlays() {
        tester.e34().insertColumns("id", "name")
                .values(1, "Alice")
                .values(2, "Charlie")
                .exec();

        tester.e35().insertColumns("id", "e34_id", "season", "ranking")
                .values(1, 1, "2024/2025", 1)
                .values(2, 1, "2025/2026", 3)
                .values(3, 2, "2024/2025", 3)
                .values(4, 2, "2025/2026", 2)
                .exec();

        tester.target("/e34/two-seasons")
                .queryParam("currentSeason", "2025/2026")
                .queryParam("previousSeason", "2024/2025")
                .queryParam("exp", "currentList.ranking = 2 and prevList.ranking = 3")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":2,\"name\":\"Charlie\"}");
    }

    @Test
    public void sortAndFilterByOverlay() {
        tester.e34().insertColumns("id", "name")
                .values(1, "Charlie")
                .values(2, "David")
                .exec();

        tester.e35().insertColumns("id", "e34_id", "season", "ranking")
                .values(1, 1, "2025/2026", 2)
                .values(2, 2, "2025/2026", 1)
                .exec();

        tester.target("/e34")
                .queryParam("season", "2025/2026")
                .queryParam("exp", "currentList.ranking <= 2")
                .queryParam("sort", "currentList.ranking")
                .get()
                .wasOk()
                .bodyEquals(2,
                        "{\"id\":2,\"name\":\"David\"}",
                        "{\"id\":1,\"name\":\"Charlie\"}");
    }

    @Test
    public void includeOverlay_NoMatchingData() {
        tester.e34().insertColumns("id", "name")
                .values(1, "Alice")
                .exec();

        tester.e35().insertColumns("id", "e34_id", "season", "ranking")
                .values(1, 1, "2024/2025", 1)
                .exec();

        tester.target("/e34")
                .queryParam("season", "2025/2026")
                .queryParam("include", "currentList")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":1,\"currentList\":[],\"name\":\"Alice\"}");
    }

    @Test
    public void includeOverlay_MultipleRecordsPerEntity() {
        tester.e34().insertColumns("id", "name")
                .values(1, "Alice")
                .exec();

        tester.e35().insertColumns("id", "e34_id", "season", "ranking")
                .values(1, 1, "2025/2026", 1)
                .values(2, 1, "2025/2026", 2)
                .values(3, 1, "2025/2026", 3)
                .exec();

        tester.target("/e34")
                .queryParam("season", "2025/2026")
                .queryParam("include", "currentList")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":1,\"currentList\":[{\"id\":1,\"ranking\":1,\"season\":\"2025/2026\"},{\"id\":2,\"ranking\":2,\"season\":\"2025/2026\"},{\"id\":3,\"ranking\":3,\"season\":\"2025/2026\"}],\"name\":\"Alice\"}");
    }

    @Test
    public void includeToOneOverlay() {
        tester.e34().insertColumns("id", "name")
                .values(1, "Alice")
                .values(2, "Bob")
                .exec();

        tester.e35().insertColumns("id", "e34_id", "season", "ranking")
                .values(1, 1, "2025/2026", 3)
                .values(2, 2, "2024/2025", 2)
                .exec();

        tester.target("/e34/to-one")
                .queryParam("season", "2025/2026")
                .queryParam("include", "currentRecord")
                .get()
                .wasOk()
                .bodyEquals(2,
                        "{\"id\":1,\"currentRecord\":{\"id\":1,\"ranking\":3,\"season\":\"2025/2026\"},\"name\":\"Alice\"}",
                        "{\"id\":2,\"currentRecord\":null,\"name\":\"Bob\"}");
    }

    @Test
    public void sortByToOneOverlay_Ascending() {
        tester.e34().insertColumns("id", "name")
                .values(1, "Alice")
                .values(2, "Charlie")
                .values(3, "David")
                .exec();

        tester.e35().insertColumns("id", "e34_id", "season", "ranking")
                .values(1, 1, "2025/2026", 3)
                .values(2, 2, "2025/2026", 2)
                .values(3, 3, "2025/2026", 1)
                .exec();

        tester.target("/e34/to-one")
                .queryParam("season", "2025/2026")
                .queryParam("sort", "currentRecord.ranking")
                .get()
                .wasOk()
                .bodyEquals(3,
                        "{\"id\":3,\"name\":\"David\"}",
                        "{\"id\":2,\"name\":\"Charlie\"}",
                        "{\"id\":1,\"name\":\"Alice\"}");
    }

    @Test
    public void filterByToOneOverlay() {
        tester.e34().insertColumns("id", "name")
                .values(1, "Alice")
                .values(2, "Charlie")
                .exec();

        tester.e35().insertColumns("id", "e34_id", "season", "ranking")
                .values(1, 1, "2025/2026", 3)
                .values(2, 2, "2025/2026", 1)
                .exec();

        tester.target("/e34/to-one")
                .queryParam("season", "2025/2026")
                .queryParam("exp", "currentRecord.ranking < 3")
                .get()
                .wasOk()
                .bodyEquals(1, "{\"id\":2,\"name\":\"Charlie\"}");
    }

    @Test
    public void sortAndFilterByToOneOverlay() {
        tester.e34().insertColumns("id", "name")
                .values(1, "Charlie")
                .values(2, "David")
                .exec();

        tester.e35().insertColumns("id", "e34_id", "season", "ranking")
                .values(1, 1, "2025/2026", 2)
                .values(2, 2, "2025/2026", 1)
                .exec();

        tester.target("/e34/to-one")
                .queryParam("season", "2025/2026")
                .queryParam("exp", "currentRecord.ranking <= 2")
                .queryParam("sort", "currentRecord.ranking")
                .get()
                .wasOk()
                .bodyEquals(2,
                        "{\"id\":2,\"name\":\"David\"}",
                        "{\"id\":1,\"name\":\"Charlie\"}");
    }

    @Test
    public void overlayWithInvalidUnderlyingRelationship() {
        tester.e34().insertColumns("id", "name")
                .values(1, "Alice")
                .exec();

        tester.e35().insertColumns("id", "e34_id", "season", "ranking")
                .values(1, 1, "2025/2026", 3)
                .exec();

        tester.target("/e34/invalid")
                .queryParam("include", "invalidList")
                .get()
                .wasServerError()
                .bodyEquals("{\"message\":\"Underlying relationship 'invalid_e35' for overlay 'invalidList' does not exist in entity 'E34'\"}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e34")
        public DataResponse<E34> getE34s(
                @Context UriInfo uriInfo,
                @QueryParam("season") String season) {

            AgEntityOverlay<E34> overlay = AgEntity.overlay(E34.class)
                    .toMany("currentList", E35.class, "e35", Exp.equal("season", season), CayenneResolvers.relatedViaQueryWithParentIds());

            return AgJaxrs
                    .select(E34.class, config)
                    .entityOverlay(overlay)
                    .clientParams(uriInfo.getQueryParameters())
                    .get();
        }

        @GET
        @Path("e34/two-seasons")
        public DataResponse<E34> getE34sWithTwoSeasons(
                @Context UriInfo uriInfo,
                @QueryParam("currentSeason") String currentSeason,
                @QueryParam("previousSeason") String previousSeason) {

            AgEntityOverlay<E34> overlay = AgEntity.overlay(E34.class)
                    .toMany("currentList", E35.class, "e35", Exp.equal("season", currentSeason), CayenneResolvers.relatedViaQueryWithParentIds())
                    .toMany("prevList", E35.class, "e35", Exp.equal("season", previousSeason), CayenneResolvers.relatedViaQueryWithParentIds());

            return AgJaxrs
                    .select(E34.class, config)
                    .entityOverlay(overlay)
                    .clientParams(uriInfo.getQueryParameters())
                    .get();
        }

        @GET
        @Path("e34/to-one")
        public DataResponse<E34> getE34sToOne(
                @Context UriInfo uriInfo,
                @QueryParam("season") String season) {

            AgEntityOverlay<E34> overlay = AgEntity.overlay(E34.class)
                    .toOne("currentRecord", E35.class, "e35", Exp.equal("season", season), CayenneResolvers.relatedViaQueryWithParentExp());

            return AgJaxrs
                    .select(E34.class, config)
                    .entityOverlay(overlay)
                    .clientParams(uriInfo.getQueryParameters())
                    .get();
        }

        @GET
        @Path("e34/invalid")
        public DataResponse<E34> getE34sInvalidOverlay(
                @Context UriInfo uriInfo) {

            AgEntityOverlay<E34> overlay = AgEntity.overlay(E34.class)
                    .toMany("invalidList", E35.class, "invalid_e35", Exp.equal("season", "2025/2026"), CayenneResolvers.relatedViaQueryWithParentIds());

            return AgJaxrs
                    .select(E34.class, config)
                    .entityOverlay(overlay)
                    .clientParams(uriInfo.getQueryParameters())
                    .get();
        }
    }
}
