package io.agrest.meta;

import io.agrest.AgException;
import io.agrest.protocol.Exp;
import io.agrest.runtime.meta.RequestSchema;

/**
 * An extension of {@link AgRelationshipOverlay} that represents a filtered view of an underlying relationship.
 *
 * @since 5.0
 */
public interface ConditionalRelationshipOverlay extends AgRelationshipOverlay {

    /**
     * Returns the name of the underlying physical relationship that this overlay filters.
     *
     * @return the name of the underlying relationship
     */
    String getUnderlyingRelationshipName();

    /**
     * Returns the filter expression applied to the underlying relationship.
     *
     * @return the qualifier expression, or null if no filtering is applied
     */
    Exp getQualifier();

    /**
     * Utility method to resolve a conditional relationship overlay from a schema.
     *
     * @since 5.0
     */
    static ConditionalRelationshipOverlay resolveOverlay(RequestSchema schema, AgEntity<?> entity, String relationshipName) {
        if (schema == null || entity == null || relationshipName == null) {
            return null;
        }

        AgEntityOverlay<?> overlay = schema.getOverlay(entity.getType());
        if (overlay != null) {
            AgRelationshipOverlay relOverlay = overlay.getRelationshipOverlay(relationshipName);
            if (relOverlay instanceof ConditionalRelationshipOverlay c && c.getUnderlyingRelationshipName() != null) {
                if (entity.getRelationship(c.getUnderlyingRelationshipName()) == null) {
                    throw AgException.internalServerError(
                            "Underlying relationship '%s' for overlay '%s' does not exist in entity '%s'",
                            c.getUnderlyingRelationshipName(), relationshipName, entity.getName());
                }
                return c;
            }
        }
        return null;
    }
}
