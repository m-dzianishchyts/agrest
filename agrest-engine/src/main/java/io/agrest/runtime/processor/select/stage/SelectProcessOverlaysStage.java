package io.agrest.runtime.processor.select.stage;

import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.select.OverlayExpressionProcessor;
import io.agrest.runtime.processor.select.SelectContext;

/**
 * Processes overlay expressions before server params are applied and validated.
 * This ensures overlay paths are annotated with aliases before path validation occurs.
 *
 * @since 5.0
 */
public class SelectProcessOverlaysStage implements Processor<SelectContext<?>> {

    private final OverlayExpressionProcessor overlayProcessor;

    public SelectProcessOverlaysStage() {
        this.overlayProcessor = new OverlayExpressionProcessor();
    }

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(SelectContext<T> context) {
        overlayProcessor.process(context.getEntity(), context.getSchema());
    }
}
