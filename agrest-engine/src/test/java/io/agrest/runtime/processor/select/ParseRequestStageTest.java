package io.agrest.runtime.processor.select;

import io.agrest.AgException;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.base.protocol.Dir;
import io.agrest.base.protocol.Sort;
import io.agrest.runtime.jackson.IJacksonService;
import io.agrest.runtime.jackson.JacksonService;
import io.agrest.runtime.protocol.CayenneExpParser;
import io.agrest.runtime.protocol.ExcludeParser;
import io.agrest.runtime.protocol.ICayenneExpParser;
import io.agrest.runtime.protocol.IExcludeParser;
import io.agrest.runtime.protocol.IIncludeParser;
import io.agrest.runtime.protocol.ISizeParser;
import io.agrest.runtime.protocol.ISortParser;
import io.agrest.runtime.protocol.IncludeParser;
import io.agrest.runtime.protocol.SizeParser;
import io.agrest.runtime.protocol.SortParser;
import io.agrest.runtime.request.DefaultRequestBuilderFactory;
import io.agrest.runtime.request.IAgRequestBuilderFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ParseRequestStageTest {

    private static ParseRequestStage stage;

    @BeforeClass
    public static void beforeAll() {

        IJacksonService jacksonService = new JacksonService();

        // prepare parse request stage
        ICayenneExpParser expParser = new CayenneExpParser(jacksonService);
        ISortParser sortParser = new SortParser(jacksonService);
        ISizeParser sizeParser = new SizeParser();
        IIncludeParser includeParser = new IncludeParser(jacksonService, expParser, sortParser, sizeParser);
        IExcludeParser excludeParser = new ExcludeParser(jacksonService);

        IAgRequestBuilderFactory requestBuilderFactory
                = new DefaultRequestBuilderFactory(expParser, sortParser, includeParser, excludeParser);
        stage = new ParseRequestStage(requestBuilderFactory);
    }

    @Test
    public void testExecute_Default() {

        SelectContext<Tr> context = prepareContext(Tr.class, new MultivaluedHashMap<>());

        stage.execute(context);

        assertNotNull(context.getMergedRequest());
        assertNull(context.getMergedRequest().getCayenneExp());
        assertTrue(context.getMergedRequest().getOrderings().isEmpty());
        assertNull(context.getMergedRequest().getMapBy());
        assertNull(context.getMergedRequest().getLimit());
        assertNull(context.getMergedRequest().getStart());
        assertTrue(context.getMergedRequest().getIncludes().isEmpty());
        assertTrue(context.getMergedRequest().getExcludes().isEmpty());
    }

    @Test
    public void testExecute_IncludeAttrs() {

        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.put("include", Arrays.asList("a", "b"));
        SelectContext<Tr> context = prepareContext(Tr.class, params);

        stage.execute(context);

        assertNotNull(context.getMergedRequest());
        assertEquals(2, context.getMergedRequest().getIncludes().size());
        assertEquals("a", context.getMergedRequest().getIncludes().get(0).getPath());
        assertEquals("b", context.getMergedRequest().getIncludes().get(1).getPath());
    }

    @Test
    public void testExecute_IncludeAttrs_AsArray() {

        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("include", "[\"a\", \"b\"]");
        SelectContext<Tr> context = prepareContext(Tr.class, params);

        stage.execute(context);

        assertNotNull(context.getMergedRequest());

        assertEquals(2, context.getMergedRequest().getIncludes().size());
        assertEquals("a", context.getMergedRequest().getIncludes().get(0).getPath());
        assertEquals("b", context.getMergedRequest().getIncludes().get(1).getPath());
    }

    @Test
    public void testExecute_ExcludeAttrs() {

        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.put("exclude", Arrays.asList("a", "b"));
        SelectContext<Tr> context = prepareContext(Tr.class, params);

        stage.execute(context);

        assertNotNull(context.getMergedRequest());

        assertEquals(2, context.getMergedRequest().getExcludes().size());
        assertEquals("a", context.getMergedRequest().getExcludes().get(0).getPath());
        assertEquals("b", context.getMergedRequest().getExcludes().get(1).getPath());
    }

    @Test
    public void testExecute_ExcludeAttrs_AsArray() {

        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("exclude", "[\"a\", \"b\"]");
        SelectContext<Tr> context = prepareContext(Tr.class, params);

        stage.execute(context);

        assertNotNull(context.getMergedRequest());

        assertEquals(2, context.getMergedRequest().getExcludes().size());
        assertEquals("a", context.getMergedRequest().getExcludes().get(0).getPath());
        assertEquals("b", context.getMergedRequest().getExcludes().get(1).getPath());
    }

    @Test
    public void testExecute_IncludeExcludeAttrs() {

        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.put("include", Arrays.asList("a", "b", "id"));
        params.put("exclude", Arrays.asList("a", "c"));
        SelectContext<Tr> context = prepareContext(Tr.class, params);

        stage.execute(context);

        assertNotNull(context.getMergedRequest());

        assertEquals(3, context.getMergedRequest().getIncludes().size());
        assertEquals("a", context.getMergedRequest().getIncludes().get(0).getPath());
        assertEquals("b", context.getMergedRequest().getIncludes().get(1).getPath());
        assertEquals("id", context.getMergedRequest().getIncludes().get(2).getPath());

        assertEquals(2, context.getMergedRequest().getExcludes().size());
        assertEquals("a", context.getMergedRequest().getExcludes().get(0).getPath());
        assertEquals("c", context.getMergedRequest().getExcludes().get(1).getPath());
    }

    @Test
    public void testExecute_IncludeRels() {

        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("include", "rtss");
        SelectContext<Tr> context = prepareContext(Tr.class, params);

        stage.execute(context);

        assertNotNull(context.getMergedRequest());
        assertEquals(1, context.getMergedRequest().getIncludes().size());
        assertEquals("rtss", context.getMergedRequest().getIncludes().get(0).getPath());
    }

    @Test
    public void testExecute_SortSimple_NoDir() {

        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("sort", "rtss");
        SelectContext<Tr> context = prepareContext(Tr.class, params);

        stage.execute(context);

        assertNotNull(context.getMergedRequest());
        assertNotNull(context.getMergedRequest().getOrderings());
        Sort ordering = context.getMergedRequest().getOrderings().get(0);
        assertEquals("rtss", ordering.getProperty());
    }

    @Test
    public void testExecute_SortSimple_ASC() {

        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("sort", "rtss");
        params.putSingle("dir", "ASC");
        SelectContext<Tr> context = prepareContext(Tr.class, params);

        stage.execute(context);

        assertNotNull(context.getMergedRequest());
        assertNotNull(context.getMergedRequest().getOrderings());
        assertEquals(1, context.getMergedRequest().getOrderings().size());
        Sort ordering = context.getMergedRequest().getOrderings().get(0);
        assertEquals("rtss", ordering.getProperty());
        assertEquals(Dir.ASC, ordering.getDirection());
    }

    @Test
    public void testExecute_SortSimple_DESC() {

        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("sort", "rtss");
        params.putSingle("dir", "DESC");
        SelectContext<Tr> context = prepareContext(Tr.class, params);

        stage.execute(context);

        assertNotNull(context.getMergedRequest());
        assertNotNull(context.getMergedRequest().getOrderings());

        Sort ordering = context.getMergedRequest().getOrderings().get(0);
        assertEquals("rtss", ordering.getProperty());
        assertEquals(Dir.DESC, ordering.getDirection());
    }

    @Test(expected = AgException.class)
    public void testExecute_SortSimple_Garbage() {

        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("sort", "xx");
        params.putSingle("dir", "XYZ");
        SelectContext<Tr> context = prepareContext(Tr.class, params);

        stage.execute(context);
    }

    @Test
    public void testExecute_Sort() {

        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("sort", "[{\"property\":\"a\",\"direction\":\"DESC\"},{\"property\":\"b\",\"direction\":\"ASC\"}]");
        SelectContext<Tr> context = prepareContext(Tr.class, params);

        stage.execute(context);

        assertNotNull(context.getMergedRequest());
        assertNotNull(context.getMergedRequest().getOrderings());
        assertEquals(2, context.getMergedRequest().getOrderings().size());

        Sort o1 = context.getMergedRequest().getOrderings().get(0);
        Sort o2 = context.getMergedRequest().getOrderings().get(1);

        assertEquals("a", o1.getProperty());
        assertEquals(Dir.DESC, o1.getDirection());
        assertEquals("b", o2.getProperty());
        assertEquals(Dir.ASC, o2.getDirection());
    }

    @Test
    public void testExecute_Sort_Dupes() {

        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("sort", "[{\"property\":\"a\",\"direction\":\"DESC\"},{\"property\":\"a\",\"direction\":\"ASC\"}]");
        SelectContext<Tr> context = prepareContext(Tr.class, params);

        stage.execute(context);

        assertNotNull(context.getMergedRequest());
        assertNotNull(context.getMergedRequest().getOrderings());
        assertEquals(2, context.getMergedRequest().getOrderings().size());

        Sort o1 = context.getMergedRequest().getOrderings().get(0);
        Sort o2 = context.getMergedRequest().getOrderings().get(1);

        assertEquals("a", o1.getProperty());
        assertEquals(Dir.DESC, o1.getDirection());
        assertEquals("a", o2.getProperty());
        assertEquals(Dir.ASC, o2.getDirection());
    }

    @Test(expected = AgException.class)
    public void testExecute_Sort_BadSpec() {

        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("sort", "[{\"property\":\"p1\",\"direction\":\"DESC\"},{\"property\":\"p2\",\"direction\":\"XXX\"}]");
        SelectContext<Tr> context = prepareContext(Tr.class, params);

        stage.execute(context);
    }

    @Test(expected = AgException.class)
    public void testExecute_CayenneExp_BadSpec() {

        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("cayenneExp", "{exp : \"numericProp = 12345 and stringProp = 'John Smith' and booleanProp = true\"}");
        SelectContext<Tr> context = prepareContext(Tr.class, params);

        stage.execute(context);
    }

    @Test
    public void testExecute_CayenneExp() {

        MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("cayenneExp", "{\"exp\" : \"a = 'John Smith'\"}");
        SelectContext<Tr> context = prepareContext(Tr.class, params);

        stage.execute(context);

        assertNotNull(context.getMergedRequest());
        assertNotNull(context.getMergedRequest().getCayenneExp());

        assertEquals("a = 'John Smith'", context.getMergedRequest().getCayenneExp().getExp());
    }

    protected <T> SelectContext<T> prepareContext(Class<T> type, MultivaluedMap<String, String> params) {
        SelectContext<T> context = new SelectContext<>(type);

        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getQueryParameters()).thenReturn(params);

        context.setUriInfo(uriInfo);
        return context;
    }

    public static class Tr {

        @AgId
        public int getId() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public int getA() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getB() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getC() {
            throw new UnsupportedOperationException();
        }

        @AgRelationship
        public List<Ts> getRtss() {
            throw new UnsupportedOperationException();
        }
    }

    public static class Ts {

        @AgId
        public int getId() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getN() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getM() {
            throw new UnsupportedOperationException();
        }
    }
}