package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.access.PathChecker;
import io.agrest.protocol.Sort;
import io.agrest.runtime.meta.RequestSchema;

import java.util.List;

/**
 * @since 2.13
 */
public interface ISortMerger {

    void merge(ResourceEntity<?> entity, List<Sort> ordering, PathChecker pathChecker);
    
    /**
     * @since 5.0
     */
    void merge(ResourceEntity<?> entity, List<Sort> ordering, PathChecker pathChecker, RequestSchema schema);
}
