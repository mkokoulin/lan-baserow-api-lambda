package com.lan.app.bootstrap;

import com.lan.app.flows.start.StartFlowRegistrar;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@Startup
@ApplicationScoped
public class FlowBootstrap {

    @Inject
    StartFlowRegistrar startFlowRegistrar;

    void onStart(@Observes StartupEvent event) {
        startFlowRegistrar.register();
    }
}
