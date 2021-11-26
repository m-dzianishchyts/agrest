package io.agrest.meta;

import io.agrest.filter.ObjectFilter;
import io.agrest.property.PropertyReader;
import io.agrest.resolver.RootDataResolver;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A shared immutable model of a resource entity.
 *
 * @since 1.12
 */
public interface AgEntity<T> {

    /**
     * @return a mutable overlay object that can be used to customize the entity.
     * @since 3.4
     */
    static <T> AgEntityOverlay<T> overlay(Class<T> type) {
        return new AgEntityOverlay<>(type);
    }

    String getName();

    Class<T> getType();

    /**
     * @since 4.1
     */
    Collection<AgIdPart> getIdParts();

    /**
     * @since 4.1
     */
    AgIdPart getIdPart(String name);

    Collection<AgAttribute> getAttributes();

    AgAttribute getAttribute(String name);

    Collection<AgRelationship> getRelationships();

    AgRelationship getRelationship(String name);

    /**
     * @return a default data resolver for this entity for when it is resolved as a root of a request.
     * @since 3.4
     */
    RootDataResolver<T> getDataResolver();

    /**
     * Returns a reader that returns id values as a Map<String, Object>
     *
     * @since 4.2
     */
    default PropertyReader getIdReader() {
        Collection<AgIdPart> ids = getIdParts();
        switch (ids.size()) {
            case 0:
                throw new IllegalStateException("Can't create ID reader. No id parts defined for entity '" + getName() + "'");
            case 1:
                AgIdPart id = ids.iterator().next();
                return o -> Collections.singletonMap(id.getName(), id.getReader().value(o));
            default:
                return o -> {
                    Map<String, Object> values = new HashMap<>();
                    ids.forEach(idx -> values.put(idx.getName(), idx.getReader().value(o)));
                    return values;
                };
        }
    }

    /**
     * Returns an in-memory filter of objects with read access
     *
     * @since 4.8
     */
    ObjectFilter<T> getReadableObjectFilter();
}
