package com.sbsolutions.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout implements AfterNavigationObserver {

  private H1 viewTitle;

  public MainLayout() {
    setPrimarySection(Section.NAVBAR);
    setDrawerOpened(false);

    addDrawerContent();
    addHeaderContent();
  }

  private void addHeaderContent() {
    DrawerToggle toggle = new DrawerToggle();
    toggle.setAriaLabel("Menu toggle");

    viewTitle = new H1();
    viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

    addToNavbar(false, toggle, viewTitle);
  }

  private void addDrawerContent() {
    Span appName = new Span("Randy's Donuts");
    appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
    Header header = new Header(appName);

    Scroller scroller = new Scroller(createNavigation());

    addToDrawer(header, scroller);
  }

  private SideNav createNavigation() {
    SideNav nav = new SideNav();

    nav.addItem(new SideNavItem("Home", "",
        new SvgIcon(LineAwesomeIconUrl.HOME_SOLID)));
    nav.addItem(new SideNavItem("Menu", MenuView.class,
        new SvgIcon(LineAwesomeIconUrl.SHOPPING_BAG_SOLID)));

    return nav;
  }

  @Override
  public void afterNavigation(AfterNavigationEvent event) {
    viewTitle.setText(MenuConfiguration.getPageHeader(getContent()).orElse(""));
  }
}
