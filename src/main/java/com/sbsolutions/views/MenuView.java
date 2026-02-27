package com.sbsolutions.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Menu")
@Route(value = "menu", layout = MainLayout.class)
@Menu(order = 1, icon = LineAwesomeIconUrl.SHOPPING_BAG_SOLID)
@AnonymousAllowed
public class MenuView extends VerticalLayout {

  public MenuView() {
    setAlignItems(Alignment.CENTER);

    H2 title = new H2("Order Menu");
    Paragraph placeholder = new Paragraph("Menu items will appear here.");

    add(title, placeholder);
  }
}
