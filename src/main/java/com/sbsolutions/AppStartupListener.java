package com.sbsolutions;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.util.logging.Logger;

@ApplicationScoped
public class AppStartupListener {

  private static final Logger log = Logger.getLogger(AppStartupListener.class.getName());

  void onStart(@Observes StartupEvent ev) {
    log.info("Application starting...");
  }
}
