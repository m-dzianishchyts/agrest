package io.agrest.meta.cayenne;

import io.agrest.meta.DefaultAgAttribute;
import io.agrest.meta.AgPersistentAttribute;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.util.ToStringBuilder;

/**
 * @since 3.4
 */
public class CayenneAgObjAttribute extends DefaultAgAttribute implements AgPersistentAttribute, CayenneAgAttribute {

	private ObjAttribute objAttribute;

	public CayenneAgObjAttribute(ObjAttribute objAttribute, Class<?> type) {
		super(objAttribute.getName(), type);
		this.objAttribute = objAttribute;
	}

	@Override
	public int getJdbcType() {
		return objAttribute.getDbAttribute().getType();
	}

	@Override
	public boolean isMandatory() {
		return objAttribute.getDbAttribute().isMandatory();
	}

	public ObjAttribute getObjAttribute() {
		return objAttribute;
	}

	@Override
	public DbAttribute getDbAttribute() {
		return objAttribute.getDbAttribute();
	}

	@Override
	public String toString() {

		ToStringBuilder tsb = new ToStringBuilder(this);
		tsb.append("name", objAttribute.getName());
		return tsb.toString();
	}
}