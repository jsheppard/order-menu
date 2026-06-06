package com.sbsolutions;

import com.vaadin.flow.server.VaadinServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import java.util.logging.Logger;

@WebServlet(urlPatterns = {"/*"}, name = "VaadinServlet", asyncSupported = true, loadOnStartup = 1)
public class VaadinServletConfig extends VaadinServlet {

  private static final Logger log = Logger.getLogger(VaadinServletConfig.class.getName());

  @Override
  public void init() throws ServletException {
    log.info("VaadinServlet initializing...");
    super.init();
    log.info("VaadinServlet initialized successfully");
  }
}
