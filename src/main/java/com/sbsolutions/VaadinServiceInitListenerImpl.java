package com.sbsolutions;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VaadinServiceInitListenerImpl implements VaadinServiceInitListener {
  
  @Override
  public void serviceInit(ServiceInitEvent event) {
    // Vaadin service initialized successfully
  }
}
