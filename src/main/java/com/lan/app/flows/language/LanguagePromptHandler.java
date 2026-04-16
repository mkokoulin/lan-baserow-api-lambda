package com.lan.app.flows.language;

import com.lan.app.domain.UpdateContext;
import com.lan.app.engine.StepHandler;
import com.lan.app.engine.StepResult;
import com.lan.app.i18n.I18n;
import com.lan.app.session.Session;
import com.lan.app.telegram.TelegramClient;
import com.lan.app.ui.KeyboardBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class LanguagePromptHandler implements StepHandler {

    @Inject TelegramClient telegramClient;
    @Inject I18n i18n;

    @Override
    public StepResult handle(UpdateContext ctx, Session session) {
        var kb = KeyboardBuilder.inline(List.of(
                KeyboardBuilder.row(
                        KeyboardBuilder.cbCmd("🇺🇸 English", LanguageFlowDef.CB_EN),
                        KeyboardBuilder.cbCmd("🇷🇺 Русский", LanguageFlowDef.CB_RU)
                )
        ));

        telegramClient.sendHtml(
                session.getChatId(),
                i18n.t(session.getLang(), "language_prompt"),
                kb
        );

        return StepResult.stay(LanguageFlowDef.FLOW, LanguageFlowDef.STEP_WAIT_CHOICE);
    }
}
