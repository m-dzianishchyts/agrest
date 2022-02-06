package io.agrest.jaxrs;

import io.agrest.SimpleResponse;
import io.agrest.jaxrs.junit.AgPojoTester;
import io.agrest.jaxrs.junit.PojoTest;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

public class GET_SimpleResponseIT extends PojoTest {

    @BQTestTool
    static final AgPojoTester tester = PojoTest.tester(Resource.class).build();

    @Test
    public void testWrite() {

        tester.target("/simple").get()
                .wasOk()
                .bodyEquals("{\"success\":true,\"message\":\"Hi!\"}");

        tester.target("/simple/2").get()
                .wasOk()
                .bodyEquals("{\"success\":false,\"message\":\"Hi2!\"}");
    }

    @Path("simple")
    public static class Resource {

        @GET
        public SimpleResponse get() {
            return SimpleResponse.of(200, true, "Hi!");
        }

        @GET
        @Path("2")
        public SimpleResponse get2() {
            return SimpleResponse.of(200, false, "Hi2!");
        }
    }
}
