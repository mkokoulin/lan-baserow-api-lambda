package com.lan.app.engine;

public record StepResult(String nextFlow, String nextStep) {

    public static StepResult stay(String flow, String step) {
        return new StepResult(flow, step);
    }

    public static StepResult finish() {
        return new StepResult(null, null);
    }
}
