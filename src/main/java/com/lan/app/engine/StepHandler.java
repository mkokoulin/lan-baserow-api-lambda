package com.lan.app.engine;

import com.lan.app.domain.UpdateContext;
import com.lan.app.session.Session;

public interface StepHandler {
    StepResult handle(UpdateContext ctx, Session session);
}
