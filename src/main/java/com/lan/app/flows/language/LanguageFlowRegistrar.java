package com.lan.app.flows.language;

import com.lan.app.engine.FlowEntry;
import com.lan.app.engine.FlowRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class LanguageFlowRegistrar {

    @Inject FlowRegistry registry;
    @Inject LanguagePromptHandler     promptHandler;
    @Inject LanguageWaitChoiceHandler waitChoiceHandler;

    public void register() {
        registry.registerStep(LanguageFlowDef.FLOW, LanguageFlowDef.STEP_PROMPT,      promptHandler);
        registry.registerStep(LanguageFlowDef.FLOW, LanguageFlowDef.STEP_WAIT_CHOICE, waitChoiceHandler);

        registry.registerCommand("language",
                new FlowEntry(LanguageFlowDef.FLOW, LanguageFlowDef.STEP_PROMPT));

        // callback "lang:en" и "lang:ru" приходят как "/lang:en" → command() = "lang:en"
        // регистрируем их как команды — CommandRouter найдёт по имени
        registry.registerCommand(LanguageFlowDef.CB_EN,
                new FlowEntry(LanguageFlowDef.FLOW, LanguageFlowDef.STEP_WAIT_CHOICE));
        registry.registerCommand(LanguageFlowDef.CB_RU,
                new FlowEntry(LanguageFlowDef.FLOW, LanguageFlowDef.STEP_WAIT_CHOICE));
    }
}
