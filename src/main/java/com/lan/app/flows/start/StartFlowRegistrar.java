package com.lan.app.flows.start;

import com.lan.app.engine.FlowEntry;
import com.lan.app.engine.FlowRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class StartFlowRegistrar {

    @Inject
    FlowRegistry registry;

    @Inject
    StartShowHandler startShowHandler;

    public void register() {
        registry.registerStep(StartFlowDef.FLOW, StartFlowDef.STEP_SHOW, startShowHandler);
        registry.registerCommand("start", new FlowEntry(StartFlowDef.FLOW, StartFlowDef.STEP_SHOW));
    }
}
