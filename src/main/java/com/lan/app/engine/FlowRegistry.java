package com.lan.app.engine;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class FlowRegistry {

    private final Map<String, StepHandler> steps = new HashMap<>();
    private final Map<String, FlowEntry> commands = new HashMap<>();

    public void registerStep(String flow, String step, StepHandler handler) {
        steps.put(key(flow, step), handler);
    }

    public void registerCommand(String command, FlowEntry entry) {
        commands.put(command.toLowerCase(), entry);
    }

    public Optional<StepHandler> getStep(String flow, String step) {
        return Optional.ofNullable(steps.get(key(flow, step)));
    }

    public Optional<FlowEntry> getCommand(String command) {
        if (command == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(commands.get(command.toLowerCase()));
    }

    private String key(String flow, String step) {
        return flow + "::" + step;
    }
}
