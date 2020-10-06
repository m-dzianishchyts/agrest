package io.agrest.unit;

import io.agrest.AgModuleProvider;
import io.agrest.pojo.runtime.PojoDB;
import io.agrest.pojo.runtime.PojoFetchStage;
import io.agrest.pojo.runtime.PojoSelectProcessorFactoryProvider;
import io.agrest.pojo.model.*;
import io.agrest.runtime.AgBuilder;
import io.agrest.runtime.AgRuntime;
import io.agrest.runtime.IAgService;
import io.agrest.runtime.processor.delete.DeleteProcessorFactory;
import io.agrest.runtime.processor.select.SelectProcessorFactory;
import io.agrest.runtime.processor.unrelate.UnrelateProcessorFactory;
import io.agrest.runtime.processor.update.UpdateProcessorFactoryFactory;
import io.bootique.BQRuntime;
import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.jersey.JerseyModule;
import io.bootique.jersey.JerseyModuleExtender;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.BeforeEach;

import javax.inject.Singleton;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static org.mockito.Mockito.mock;

/**
 * An abstract superclass of integration tests that starts Bootique test runtime with JAX-RS service and an in-memory
 * "pojo database".
 */
@BQTest
public abstract class PojoTest {

    @BQTestTool
    static final BQTestFactory TEST_FACTORY = new BQTestFactory();

    // in-memory key/value "database" to store POJOs
    protected static PojoDB POJO_DB;
    private static BQRuntime TEST_RUNTIME;

    protected static void startTestRuntime(Class<?>... resources) {
        startTestRuntime(b -> b, resources);
    }

    protected static void startTestRuntime(UnaryOperator<AgBuilder> customizer, Class<?>... resources) {

        POJO_DB = new PojoDB();

        Function<AgBuilder, AgBuilder> customizerChain = customizer.compose(PojoTest::customizeForPojo);

        TEST_RUNTIME = TEST_FACTORY.app("-s")
                .autoLoadModules()
                .module(new AgModule(customizerChain))
                .module(b -> addResources(JerseyModule.extend(b), resources).addFeature(AgRuntime.class))
                .createRuntime();

        TEST_RUNTIME.run();
    }

    private static AgBuilder customizeForPojo(AgBuilder builder) {
        return builder.module(new PojoTestModuleProvider());
    }

    private static JerseyModuleExtender addResources(JerseyModuleExtender extender, Class<?>... resources) {
        for (Class<?> c : resources) {
            extender.addResource(c);
        }

        return extender;
    }

    @BeforeEach
    public void resetData() {
        POJO_DB.clear();
    }

    protected IAgService ag() {
        return agService(IAgService.class);
    }

    protected <T> T agService(Class<T> type) {
        return TEST_RUNTIME.getInstance(AgRuntime.class).service(type);
    }

    protected AgResponseAssertions onSuccess(Response response) {
        return onResponse(response).wasSuccess();
    }

    protected AgResponseAssertions onResponse(Response response) {
        return new AgResponseAssertions(response);
    }

    protected String urlEnc(String queryParam) {
        try {
            // URLEncoder replaces spaces with "+"... Those are not decoded
            // properly by Jersey in 'uriInfo.getQueryParameters()' (TODO: why?)
            return URLEncoder.encode(queryParam, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {

            // unexpected... we know that UTF-8 is present
            throw new RuntimeException(e);
        }
    }

    protected WebTarget target(String path) {
        // TODO: use JettyTester and dynamic port
        return ClientBuilder.newClient().target("http://127.0.0.1:8080/").path(path);
    }

    protected <T> Map<Object, T> bucket(Class<T> type) {
        return POJO_DB.bucketForType(type);
    }

    protected Map<Object, P1> p1() {
        return POJO_DB.bucketForType(P1.class);
    }

    protected Map<Object, P2> p2() {
        return POJO_DB.bucketForType(P2.class);
    }

    protected Map<Object, P4> p4() {
        return POJO_DB.bucketForType(P4.class);
    }

    protected Map<Object, P6> p6() {
        return POJO_DB.bucketForType(P6.class);
    }

    protected Map<Object, P8> p8() {
        return POJO_DB.bucketForType(P8.class);
    }

    protected Map<Object, P9> p9() {
        return POJO_DB.bucketForType(P9.class);
    }

    public static class AgModule implements BQModule {

        private final Function<AgBuilder, AgBuilder> agCustomizer;

        public AgModule(Function<AgBuilder, AgBuilder> agCustomizer) {
            this.agCustomizer = agCustomizer;
        }

        @Override
        public void configure(Binder binder) {
        }

        @Provides
        @Singleton
        AgRuntime createRuntime() {
            return agCustomizer.apply(new AgBuilder()).build();
        }
    }

    static class PojoTestModuleProvider implements AgModuleProvider {

        @Override
        public org.apache.cayenne.di.Module module() {
            return new PojoTestModule();
        }

        @Override
        public Class<? extends org.apache.cayenne.di.Module> moduleType() {
            return PojoTestModule.class;
        }
    }

    public static class PojoTestModule implements org.apache.cayenne.di.Module {

        @Override
        public void configure(org.apache.cayenne.di.Binder binder) {
            binder.bind(SelectProcessorFactory.class).toProvider(PojoSelectProcessorFactoryProvider.class);
            binder.bind(DeleteProcessorFactory.class).toInstance(mock(DeleteProcessorFactory.class));
            binder.bind(UpdateProcessorFactoryFactory.class).toInstance(mock(UpdateProcessorFactoryFactory.class));
            binder.bind(UnrelateProcessorFactory.class).toInstance(mock(UnrelateProcessorFactory.class));

            binder.bind(PojoFetchStage.class).to(PojoFetchStage.class);
            binder.bind(PojoDB.class).toInstance(POJO_DB);
        }
    }
}