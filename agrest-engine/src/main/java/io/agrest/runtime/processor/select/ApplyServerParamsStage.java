package io.agrest.runtime.processor.select;

import io.agrest.ResourceEntity;
import io.agrest.encoder.EntityEncoderFilter;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.constraints.IConstraintsHandler;
import org.apache.cayenne.di.Inject;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 2.7
 */
public class ApplyServerParamsStage implements Processor<SelectContext<?>> {

    private IConstraintsHandler constraintsHandler;
    private List<EntityEncoderFilter> filters;

    public ApplyServerParamsStage(
            @Inject IConstraintsHandler constraintsHandler,
            @Inject List<EntityEncoderFilter> filters) {

        this.constraintsHandler = constraintsHandler;
        this.filters = filters;
    }

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(SelectContext<T> context) {
        constraintsHandler.constrainResponse(context.getEntity(), context.getSizeConstraints(), context.getConstraint());
        initEncoderFilters(context);
    }

    @Deprecated
    protected void initEncoderFilters(SelectContext<?> context) {
        List<EntityEncoderFilter> filters = mergeFilters(context.getEntityEncoderFilters());
        if (!filters.isEmpty()) {
            initEncoderFilters(context.getEntity(), filters);
        }
    }

    @Deprecated
    protected void initEncoderFilters(ResourceEntity<?> entity, List<EntityEncoderFilter> filters) {

        for (EntityEncoderFilter filter : filters) {
            if (filter.matches(entity)) {
                entity.getEntityEncoderFilters().add(filter);
            }
        }

        for (ResourceEntity<?> child : entity.getChildren().values()) {
            initEncoderFilters(child, filters);
        }
    }

    @Deprecated
    protected List<EntityEncoderFilter> mergeFilters(List<EntityEncoderFilter> requestFilters) {

        if (requestFilters == null || requestFilters.isEmpty()) {
            return this.filters;
        }

        if (this.filters.isEmpty()) {
            return requestFilters;
        }

        List<EntityEncoderFilter> combined = new ArrayList<>(requestFilters.size() + filters.size());

        // global filters go first, per-request filters applied after them
        combined.addAll(filters);
        combined.addAll(requestFilters);

        return combined;
    }
}