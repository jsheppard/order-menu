package com.sbsolutions;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.TimeZone;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@CssImport("./themes/order-menu/styles.css")
@Theme("order-menu")
@Push
public class Application implements AppShellConfigurator {

  @ConfigProperty(name = "app.time-zone", defaultValue = "America/Chicago")
  String timeZone;

  @PostConstruct
  public void init() {
    TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
  }

  @Override
  public void configurePage(AppShellSettings settings) {
    settings.addFavIcon("icon", "icons/icon.png", "192x192");
  }
}
