package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.access.PathChecker;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.meta.ConditionalRelationshipOverlay;
import io.agrest.protocol.Sort;
import io.agrest.runtime.meta.RequestSchema;

import java.util.List;

/**
 * @since 2.13
 */
public class SortMerger implements ISortMerger {

    /**
     * @since 2.13
     */
    @Override
    public void merge(ResourceEntity<?> resourceEntity, List<Sort> orderings, PathChecker pathChecker) {
        merge(resourceEntity, orderings, pathChecker, null);
    }

    /**
     * @since 5.0
     */
    @Override
    public void merge(ResourceEntity<?> resourceEntity, List<Sort> orderings, PathChecker pathChecker, RequestSchema schema) {
        orderings.forEach(o -> collectOrdering(resourceEntity, o, pathChecker, schema));
    }

    private void collectOrdering(ResourceEntity<?> resourceEntity, Sort ordering, PathChecker pathChecker, RequestSchema schema) {

        // check for dupes...
        for (Sort o : resourceEntity.getOrderings()) {
            if (o.equals(ordering)) {
                return;
            }
        }

        // Skip depth check if path uses overlay relationships - will be validated after overlay processing
        if (!usesOverlayRelationship(ordering.getPath(), resourceEntity.getAgEntity(), schema)) {
            pathChecker.exceedsDepth(ordering.getPath());
        }
        
        resourceEntity.getOrderings().add(ordering);
    }

    private boolean usesOverlayRelationship(String path, AgEntity<?> entity, RequestSchema schema) {
        if (schema == null || path == null) {
            return false;
        }

        String[] segments = path.split("\\.");
        AgEntity<?> currentEntity = entity;

        for (int i = 0; i < segments.length - 1; i++) {
            String segment = segments[i];
            if (ConditionalRelationshipOverlay.resolveOverlay(schema, currentEntity, segment) != null) {
                return true;
            }

            AgRelationship relationship = currentEntity.getRelationship(segment);
            if (relationship != null) {
                currentEntity = relationship.getTargetEntity();
            } else {
                break;
            }
        }

        return false;
    }
}
