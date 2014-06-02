package com.nhl.link.rest.runtime.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.ClientEntity;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.DataResponseConfig;

public class IntersectConfigMergerTest {

	private IntersectConfigMerger merger;
	private ObjEntity entity;

	@Before
	public void before() {
		this.merger = new IntersectConfigMerger();
		
		DataMap dm = new DataMap();

		ObjEntity e0 = new ObjEntity("Test");
		dm.addObjEntity(e0);
		
		ObjEntity e1 = new ObjEntity("Test1");
		dm.addObjEntity(e1);
		
		ObjEntity e2 = new ObjEntity("Test2");
		dm.addObjEntity(e2);
		
		ObjEntity e3 = new ObjEntity("Test3");
		dm.addObjEntity(e3);
		
		ObjRelationship r01 = new ObjRelationship("r1");
		r01.setTargetEntityName(e1.getName());
		e0.addRelationship(r01);
		
		ObjRelationship r12 = new ObjRelationship("r11");
		r12.setTargetEntityName(e2.getName());
		e1.addRelationship(r12);
		
		ObjRelationship r03 = new ObjRelationship("r2");
		r03.setTargetEntityName(e3.getName());
		e0.addRelationship(r03);

		this.entity = e0;
	}

	@Test
	public void testMerge_FetchOffset() {

		DataResponseConfig s1 = new DataResponseConfig(entity).fetchOffset(5);
		DataResponseConfig s2 = new DataResponseConfig(entity).fetchOffset(0);

		DataResponse<?> t1 = DataResponse.forType(Object.class).withFetchOffset(0);
		merger.merge(s1, t1);
		assertEquals(0, t1.getFetchOffset());
		assertEquals(5, s1.getFetchOffset());

		DataResponse<?> t2 = DataResponse.forType(Object.class).withFetchOffset(3);
		merger.merge(s1, t2);
		assertEquals(3, t2.getFetchOffset());
		assertEquals(5, s1.getFetchOffset());

		DataResponse<?> t3 = DataResponse.forType(Object.class).withFetchOffset(6);
		merger.merge(s1, t3);
		assertEquals(5, t3.getFetchOffset());
		assertEquals(5, s1.getFetchOffset());

		DataResponse<?> t4 = DataResponse.forType(Object.class).withFetchOffset(6);
		merger.merge(s2, t4);
		assertEquals(6, t4.getFetchOffset());
		assertEquals(0, s2.getFetchOffset());
	}

	@Test
	public void testMerge_FetchLimit() {

		DataResponseConfig s1 = new DataResponseConfig(entity).fetchLimit(5);
		DataResponseConfig s2 = new DataResponseConfig(entity).fetchLimit(0);

		DataResponse<?> t1 = DataResponse.forType(Object.class).withFetchLimit(0);
		merger.merge(s1, t1);
		assertEquals(0, t1.getFetchLimit());
		assertEquals(5, s1.getFetchLimit());

		DataResponse<?> t2 = DataResponse.forType(Object.class).withFetchLimit(3);
		merger.merge(s1, t2);
		assertEquals(3, t2.getFetchLimit());
		assertEquals(5, s1.getFetchLimit());

		DataResponse<?> t3 = DataResponse.forType(Object.class).withFetchLimit(6);
		merger.merge(s1, t3);
		assertEquals(5, t3.getFetchLimit());
		assertEquals(5, s1.getFetchLimit());

		DataResponse<?> t4 = DataResponse.forType(Object.class).withFetchLimit(6);
		merger.merge(s2, t4);
		assertEquals(6, t4.getFetchLimit());
		assertEquals(0, s2.getFetchLimit());
	}

	@Test
	public void testMerge_ClientEntity_NoTargetRel() {

		DataResponseConfig s1 = new DataResponseConfig(entity);
		s1.getEntity().attributes("a", "b");

		ClientEntity<Object> te1 = new ClientEntity<>(Object.class);
		te1.getAttributes().add("c");
		te1.getAttributes().add("b");

		ClientEntity<?> te11 = new ClientEntity<>(Object.class);
		te11.getAttributes().add("a1");
		te11.getAttributes().add("b1");
		te1.getRelationships().put("d", te11);

		DataResponse<?> t1 = DataResponse.forType(Object.class).withClientEntity(te1);

		merger.merge(s1, t1);
		assertEquals(1, t1.getEntity().getAttributes().size());
		assertTrue(t1.getEntity().getAttributes().contains("b"));
		assertTrue(t1.getEntity().getRelationships().isEmpty());
	}

	@Test
	public void testMerge_ClientEntity_TargetRel() {

		DataResponseConfig s1 = new DataResponseConfig(entity);
		s1.getEntity().attributes("a", "b");
		s1.getEntity().getOrMakeChild("r1").attributes("n", "m").getOrMakeChild("r11").attributes("p", "r");
		s1.getEntity().getOrMakeChild("r2").attributes("k", "l");

		ClientEntity<Object> te1 = new ClientEntity<>(Object.class);
		te1.getAttributes().add("c");
		te1.getAttributes().add("b");

		ClientEntity<?> te11 = new ClientEntity<>(Object.class);
		te11.getAttributes().add("m");
		te11.getAttributes().add("z");
		te1.getRelationships().put("r1", te11);

		ClientEntity<?> te21 = new ClientEntity<>(Object.class);
		te21.getAttributes().add("p");
		te21.getAttributes().add("z");
		te1.getRelationships().put("r3", te21);

		DataResponse<?> t1 = DataResponse.forType(Object.class).withClientEntity(te1);

		merger.merge(s1, t1);
		assertEquals(1, t1.getEntity().getAttributes().size());
		assertTrue(t1.getEntity().getAttributes().contains("b"));
		assertEquals(1, t1.getEntity().getRelationships().size());

		ClientEntity<?> mergedTe11 = t1.getEntity().getRelationships().get("r1");
		assertNotNull(mergedTe11);
		assertTrue(mergedTe11.getRelationships().isEmpty());
		assertEquals(1, mergedTe11.getAttributes().size());
		assertTrue(mergedTe11.getAttributes().contains("m"));
	}

	@Test
	public void testMerge_ClientEntity_Id() {

		DataResponseConfig s1 = new DataResponseConfig(entity);
		s1.getEntity().excludeId();

		DataResponseConfig s2 = new DataResponseConfig(entity);
		s2.getEntity().includeId();

		ClientEntity<Object> te1 = new ClientEntity<>(Object.class);
		te1.setIdIncluded(true);
		DataResponse<?> t1 = DataResponse.forType(Object.class).withClientEntity(te1);
		merger.merge(s1, t1);
		assertFalse(t1.getEntity().isIdIncluded());

		ClientEntity<Object> te2 = new ClientEntity<>(Object.class);
		te2.setIdIncluded(true);
		DataResponse<?> t2 = DataResponse.forType(Object.class).withClientEntity(te2);
		merger.merge(s2, t2);
		assertTrue(t2.getEntity().isIdIncluded());

		ClientEntity<Object> te3 = new ClientEntity<>(Object.class);
		te3.setIdIncluded(false);
		DataResponse<?> t3 = DataResponse.forType(Object.class).withClientEntity(te3);
		merger.merge(s2, t3);
		assertFalse(t3.getEntity().isIdIncluded());
	}

	@Test
	public void testMerge_CayenneExp() {

		Expression q1 = Expression.fromString("a = 5");

		DataResponseConfig s1 = new DataResponseConfig(entity);
		s1.getEntity().andQualifier(q1);

		ClientEntity<Object> te1 = new ClientEntity<>(Object.class);
		DataResponse<?> t1 = DataResponse.forType(Object.class).withClientEntity(te1);
		merger.merge(s1, t1);
		assertEquals(Expression.fromString("a = 5"), t1.getEntity().getQualifier());

		ClientEntity<Object> te2 = new ClientEntity<>(Object.class);
		te2.andQualifier(Expression.fromString("b = 'd'"));
		DataResponse<?> t2 = DataResponse.forType(Object.class).withClientEntity(te2);
		merger.merge(s1, t2);
		assertEquals(Expression.fromString("b = 'd' and a = 5"), t2.getEntity().getQualifier());
	}

	@Test
	public void testMerge_MapBy() {

		DataResponseConfig s1 = new DataResponseConfig(entity);
		s1.getEntity().getOrMakeChild("r1").attribute("a");

		ClientEntity<Object> te1MapByTarget = new ClientEntity<>(Object.class);
		te1MapByTarget.getAttributes().add("b");

		ClientEntity<Object> te1MapBy = new ClientEntity<>(Object.class);
		te1MapBy.getRelationships().put("r1", te1MapByTarget);

		ClientEntity<Object> te1 = new ClientEntity<>(Object.class);
		te1.setMapByPath("r1.b");
		te1.setMapBy(te1MapBy);

		DataResponse<?> t1 = DataResponse.forType(Object.class).withClientEntity(te1);
		merger.merge(s1, t1);
		assertNull(t1.getEntity().getMapBy());
		assertNull(t1.getEntity().getMapByPath());

		ClientEntity<Object> te2MapByTarget = new ClientEntity<>(Object.class);
		te2MapByTarget.getAttributes().add("a");

		ClientEntity<Object> te2MapBy = new ClientEntity<>(Object.class);
		te1MapBy.getRelationships().put("r1", te2MapByTarget);

		ClientEntity<Object> te2 = new ClientEntity<>(Object.class);
		te2.setMapByPath("r1.a");
		te2.setMapBy(te2MapBy);

		DataResponse<?> t2 = DataResponse.forType(Object.class).withClientEntity(te2);
		merger.merge(s1, t2);
		assertSame(te2MapBy, t2.getEntity().getMapBy());
		assertEquals("r1.a", t2.getEntity().getMapByPath());
	}

	@Test
	public void testMerge_MapById_Exclude() {

		DataResponseConfig s1 = new DataResponseConfig(entity);
		s1.getEntity().getOrMakeChild("r1").excludeId();

		ClientEntity<Object> te1MapByTarget = new ClientEntity<>(Object.class);
		te1MapByTarget.setIdIncluded(true);

		ClientEntity<Object> te1MapBy = new ClientEntity<>(Object.class);
		te1MapBy.getRelationships().put("r1", te1MapByTarget);

		ClientEntity<Object> te1 = new ClientEntity<>(Object.class);
		te1.setMapByPath("r1");
		te1.setMapBy(te1MapBy);

		DataResponse<?> t1 = DataResponse.forType(Object.class).withClientEntity(te1);
		merger.merge(s1, t1);
		assertNull(t1.getEntity().getMapBy());
		assertNull(t1.getEntity().getMapByPath());

	}

	@Test
	public void testMerge_MapById_Include() {

		DataResponseConfig s1 = new DataResponseConfig(entity);
		s1.getEntity().getOrMakeChild("r1").includeId();

		ClientEntity<Object> te1MapByTarget = new ClientEntity<>(Object.class);
		te1MapByTarget.setIdIncluded(true);

		ClientEntity<Object> te1MapBy = new ClientEntity<>(Object.class);
		te1MapBy.getRelationships().put("r1", te1MapByTarget);

		ClientEntity<Object> te1 = new ClientEntity<>(Object.class);
		te1.setMapByPath("r1");
		te1.setMapBy(te1MapBy);

		DataResponse<?> t1 = DataResponse.forType(Object.class).withClientEntity(te1);
		merger.merge(s1, t1);
		assertSame(te1MapBy, t1.getEntity().getMapBy());
		assertEquals("r1", t1.getEntity().getMapByPath());

	}
}
