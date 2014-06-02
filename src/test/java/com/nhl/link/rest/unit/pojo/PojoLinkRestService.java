package com.nhl.link.rest.unit.pojo;

import java.util.Map.Entry;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.di.Inject;
import org.apache.cayenne.query.SelectQuery;
import org.apache.cayenne.reflect.PropertyUtils;

import com.nhl.link.rest.DataResponseConfig;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.runtime.BaseLinkRestService;
import com.nhl.link.rest.runtime.config.IConfigMerger;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.parser.IRequestParser;

public class PojoLinkRestService extends BaseLinkRestService {

	private PojoDB db;
	private IConfigMerger configMerger;

	public PojoLinkRestService(@Inject IRequestParser requestParser, @Inject IEncoderService encoderService,
			@Inject IConfigMerger configMerger) {
		super(requestParser, encoderService);
		this.db = JerseyTestOnPojo.pojoDB;
		this.configMerger = configMerger;
	}

	@Override
	public DataResponseConfig newConfig(Class<?> root) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public <T> SelectBuilder<T> forSelect(Class<T> root) {
		return new PojoSelectBuilder<>(root, encoderService, requestParser, configMerger, db.bucketForType(root));
	}

	@Override
	public <T> SelectBuilder<T> forSelect(SelectQuery<T> query) {
		throw new UnsupportedOperationException("Can't select with Cayenne query");
	}

	@Override
	protected <T> void doDelete(Class<T> root, Object id) {
		db.bucketForType(root).remove(id);
	}

	@Override
	protected <T> T doInsert(UpdateResponse<T> response) {

		T object;
		try {
			object = response.getType().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("Error creating entity", e);
		}

		mergeChanges(response, object);
		db.bucketForType(response.getType()).put(getId(response, object), object);
		return object;
	}

	@Override
	protected <T> T doUpdate(UpdateResponse<T> response) {
		T object = db.bucketForType(response.getType()).get(response.getId());

		if (object == null) {
			throw new LinkRestException(Status.NOT_FOUND, "Object  with ID '" + response.getId() + "' is not found");
		}

		mergeChanges(response, object);

		return object;
	}

	private <T> Object getId(UpdateResponse<T> response, T pojo) {
		String pkProperty = response.getEntity().getEntity().getPrimaryKeyNames().iterator().next();
		return PropertyUtils.getProperty(pojo, pkProperty);
	}

	private <T> void mergeChanges(UpdateResponse<T> response, T object) {

		// attributes
		for (Entry<String, Object> e : response.getValues().entrySet()) {
			PropertyUtils.setProperty(object, e.getKey(), e.getValue());
		}
	}

}
