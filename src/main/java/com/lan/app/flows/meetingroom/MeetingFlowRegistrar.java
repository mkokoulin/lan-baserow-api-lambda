package com.lan.app.flows.meetingroom;

import com.lan.app.engine.FlowEntry;
import com.lan.app.engine.FlowRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MeetingFlowRegistrar {

    @Inject FlowRegistry registry;
    @Inject MeetingPromptHandler      promptHandler;
    @Inject MeetingWaitDateHandler    waitDateHandler;
    @Inject MeetingWaitStartHandler   waitStartHandler;
    @Inject MeetingWaitEndHandler     waitEndHandler;
    @Inject MeetingWaitContactHandler waitContactHandler;

    public void register() {
        registry.registerStep(MeetingFlowDef.FLOW, MeetingFlowDef.STEP_PROMPT,       promptHandler);
        registry.registerStep(MeetingFlowDef.FLOW, MeetingFlowDef.STEP_WAIT_DATE,    waitDateHandler);
        registry.registerStep(MeetingFlowDef.FLOW, MeetingFlowDef.STEP_WAIT_START,   waitStartHandler);
        registry.registerStep(MeetingFlowDef.FLOW, MeetingFlowDef.STEP_WAIT_END,     waitEndHandler);
        registry.registerStep(MeetingFlowDef.FLOW, MeetingFlowDef.STEP_WAIT_CONTACT, waitContactHandler);

        registry.registerCommand("meetingroom",
                new FlowEntry(MeetingFlowDef.FLOW, MeetingFlowDef.STEP_PROMPT));
    }
}