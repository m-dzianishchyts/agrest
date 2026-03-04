package io.agrest.meta;

import io.agrest.protocol.Exp;
import io.agrest.resolver.RelatedDataResolver;

/**
 * @since 5.0
 */
public class DefaultConditionalRelationshipOverlay extends DefaultRelationshipOverlay implements ConditionalRelationshipOverlay {

    private final String underlyingRelationshipName;
    private final Exp qualifier;

    public DefaultConditionalRelationshipOverlay(
            String name,
            Class<?> sourceType,
            Class<?> targetType,
            Boolean toMany,
            Boolean readable,
            Boolean writable,
            RelatedDataResolver<?> resolver,
            String underlyingRelationshipName,
            Exp qualifier) {
        super(name, sourceType, targetType, toMany, readable, writable, resolver);
        this.underlyingRelationshipName = underlyingRelationshipName;
        this.qualifier = qualifier;
    }

    @Override
    public String getUnderlyingRelationshipName() {
        return underlyingRelationshipName;
    }

    @Override
    public Exp getQualifier() {
        return qualifier;
    }
}
