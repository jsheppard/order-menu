package com.sbsolutions.views;

import com.sbsolutions.api.DonutsClient;
import com.sbsolutions.api.PricingSheetClient;
import com.sbsolutions.api.RollClient;
import com.sbsolutions.order.models.Donut;
import com.sbsolutions.order.models.PricingSheet;
import com.sbsolutions.order.models.Roll;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PageTitle("Kiosk Menu")
@Route(value = "", autoLayout = false)
@AnonymousAllowed
public class KioskView extends VerticalLayout {

  private static final Logger log = LoggerFactory.getLogger(KioskView.class);

  private static final ScheduledExecutorService SCHEDULER =
      Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "kiosk-refresh");
        t.setDaemon(true);
        return t;
      });

  private final DonutsClient       donutsClient;
  private final RollClient         rollClient;
  private final PricingSheetClient pricingSheetClient;
  private ScheduledFuture<?>       refreshTask;
  private final Div  content        = new Div();
  private final Div  pricesSidebar  = new Div();
  private final Span headerDate     = new Span();
  private final Span lastRefreshed  = new Span();

  public KioskView(DonutsClient donutsClient, RollClient rollClient,
      PricingSheetClient pricingSheetClient) {
    this.donutsClient       = donutsClient;
    this.rollClient         = rollClient;
    this.pricingSheetClient = pricingSheetClient;

    setSizeFull();
    setPadding(false);
    setSpacing(false);
    addClassName("kiosk-root");

    // ── Top header ─────────────────────────────────────────
    Div header = new Div();
    header.addClassName("kiosk-header");

    Span brand = new Span("Randy's Donuts");
    brand.addClassName("kiosk-header-brand");

    headerDate.addClassName("kiosk-header-date");
    lastRefreshed.addClassName("kiosk-header-refreshed");

    Div headerRight = new Div();
    headerRight.addClassName("kiosk-header-right");
    headerRight.add(headerDate, lastRefreshed);

    header.add(brand, headerRight);
    add(header);

    Div main = new Div();
    main.addClassName("kiosk-main");

    content.addClassName("kiosk-content");
    pricesSidebar.addClassName("kiosk-prices");

    main.add(content, pricesSidebar);
    add(main);

    loadData();
  }

  @Override
  protected void onAttach(AttachEvent event) {
    super.onAttach(event);
    UI ui = event.getUI();
    refreshTask = SCHEDULER.scheduleAtFixedRate(
        () -> ui.access(this::refresh),
        15, 15, TimeUnit.MINUTES
    );
  }

  @Override
  protected void onDetach(DetachEvent event) {
    super.onDetach(event);
    if (refreshTask != null) {
      refreshTask.cancel(false);
      refreshTask = null;
    }
  }

  private void refresh() {
    log.info("Refreshing kiosk products from database");
    loadData();
  }

  private void loadData() {
    List<Donut>         donuts        = List.of();
    List<Roll>          rolls         = List.of();
    List<PricingSheet>  pricingSheets = List.of();

    try { donuts        = donutsClient.findAll();        } catch (Exception e) { log.warn("Could not load donuts: {}",          e.getMessage()); }
    try { rolls         = rollClient.findAll();           } catch (Exception e) { log.warn("Could not load rolls: {}",           e.getMessage()); }
    try { pricingSheets = pricingSheetClient.findAll();   } catch (Exception e) { log.warn("Could not load pricing sheets: {}",  e.getMessage()); }

    headerDate.getElement().executeJs(
        "const d = new Date();" +
        "this.textContent = d.toLocaleDateString('en-GB', " +
        "{weekday:'long', day:'numeric', month:'long', year:'numeric'});"
    );
    lastRefreshed.getElement().executeJs(
        "this.textContent = 'Last refreshed: ' + " +
        "new Date().toLocaleTimeString([], {hour: 'numeric', minute: '2-digit'});"
    );

    content.removeAll();

    List<Donut> donutHoles = donuts.stream()
        .filter(d -> d.getDescription() != null
            && d.getDescription().toLowerCase().contains("donut holes"))
        .collect(Collectors.toList());
    List<Donut> regularDonuts = donuts.stream()
        .filter(d -> d.getDescription() == null
            || !d.getDescription().toLowerCase().contains("donut holes"))
        .collect(Collectors.toList());

    if (!regularDonuts.isEmpty()) content.add(createSection("Donuts",      regularDonuts));
    if (!donutHoles.isEmpty())    content.add(createSection("Donut Holes", donutHoles));
    if (!rolls.isEmpty())         content.add(createSection("Rolls",       rolls));

    if (donuts.isEmpty() && rolls.isEmpty()) {
      Span empty = new Span("No products available at this time.");
      empty.addClassName("kiosk-empty");
      content.add(empty);
    }

    List<Donut> allProducts = new ArrayList<>();
    allProducts.addAll(donuts);
    allProducts.addAll(rolls);
    buildPricesSidebar(pricingSheets, allProducts);
  }

  private void buildPricesSidebar(List<PricingSheet> sheets, List<Donut> allProducts) {
    pricesSidebar.removeAll();

    // ── Prices ───────────────────────────────────────────────
    Span title = new Span("Prices");
    title.addClassName("kiosk-prices-title");
    pricesSidebar.add(title);

    sheets.stream()
        .sorted(Comparator.comparing(PricingSheet::getOrder,
            Comparator.nullsLast(Comparator.naturalOrder())))
        .forEach(sheet -> {
          Div row = new Div();
          row.addClassName("kiosk-prices-row");

          Span desc = new Span(sheet.getDescription() != null ? sheet.getDescription() : "");
          desc.addClassName("kiosk-prices-desc");

          Span unit = new Span(sheet.getUnit() != null ? sheet.getUnit() : "");
          unit.addClassName("kiosk-prices-unit");

          String priceStr = sheet.getPrice() != null
              ? String.format("$%.2f", sheet.getPrice()) : "";
          Span price = new Span(priceStr);
          price.addClassName("kiosk-prices-price");

          row.add(desc, unit, price);
          pricesSidebar.add(row);
        });

    // ── Specials ─────────────────────────────────────────────
    DayOfWeek today = LocalDate.now().getDayOfWeek();

    List<Donut> todaySpecials = allProducts.stream()
        .filter(d -> notBlank(d.getAvailableDays())
            && parseAvailableDays(d.getAvailableDays()).contains(today))
        .collect(Collectors.toList());

    if (!todaySpecials.isEmpty()) {
      Span specialsTitle = new Span("Specials");
      specialsTitle.addClassName("kiosk-specials-title");
      pricesSidebar.add(specialsTitle);

      for (int i = 0; i < todaySpecials.size(); i++) {
        Donut special = todaySpecials.get(i);

        Div card = new Div();
        card.addClassName("kiosk-specials-card");
        if (i > 0) {
          card.getStyle().set("display", "none").set("opacity", "0");
        }

        String imgUrl = bestImageUrl(special);
        if (imgUrl != null) {
          Image img = new Image(imgUrl, special.getDescription() != null ? special.getDescription() : "");
          img.addClassName("kiosk-specials-img");
          card.add(img);
        }

        Span name = new Span(special.getDescription() != null ? special.getDescription() : "");
        name.addClassName("kiosk-specials-name");
        card.add(name);

        if (special.getPrice() != null) {
          String priceText = String.format("$%.2f", special.getPrice());
          if (notBlank(special.getUnit())) {
            priceText += " / " + special.getUnit();
          }
          Span price = new Span(priceText);
          price.addClassName("kiosk-specials-price");
          card.add(price);
        }

        pricesSidebar.add(card);
      }

      if (todaySpecials.size() > 1) {
        pricesSidebar.getElement().executeJs(
            "if (window._kioskSpecialsTimer) clearInterval(window._kioskSpecialsTimer);" +
            "var cards = $0.querySelectorAll('.kiosk-specials-card');" +
            "var cur = 0;" +
            "window._kioskSpecialsTimer = setInterval(function() {" +
            "  var outCard = cards[cur];" +
            "  outCard.style.opacity = '0';" +
            "  setTimeout(function() {" +
            "    outCard.style.display = 'none';" +
            "    cur = (cur + 1) % cards.length;" +
            "    var inCard = cards[cur];" +
            "    inCard.style.display = '';" +
            "    inCard.offsetHeight;" +
            "    inCard.style.opacity = '1';" +
            "  }, 600);" +
            "}, 10000);",
            pricesSidebar.getElement()
        );
      }
    }
  }

  private Div createSection(String label, List<? extends Donut> items) {
    Div section = new Div();
    section.addClassName("kiosk-section");

    H3 sectionLabel = new H3(label);
    sectionLabel.addClassName("kiosk-section-label");
    section.add(sectionLabel);

    section.add(createScrollingRow(items));

    return section;
  }

  private Div createScrollingRow(List<? extends Donut> items) {
    Div viewport = new Div();
    viewport.addClassName("kiosk-viewport");

    Div track = new Div();
    track.addClassName("kiosk-track");

    // Products without availableDays first; those with it sorted ascending by first day
    List<? extends Donut> sorted = items.stream()
        .sorted(Comparator.comparingInt((Donut d) -> notBlank(d.getAvailableDays()) ? 1 : 0)
            .thenComparingInt(d -> dayOrder(d.getAvailableDays())))
        .toList();

    // Duplicate items for seamless infinite scroll loop
    for (int pass = 0; pass < 2; pass++) {
      for (Donut item : sorted) {
        track.add(createCard(item));
      }
    }

    // 3 seconds per card, minimum 12 seconds
    int durationSeconds = Math.max(12, sorted.size() * 3);
    track.getStyle().set("animation-duration", durationSeconds + "s");

    viewport.add(track);
    return viewport;
  }

  private Div createCard(Donut item) {
    Div card = new Div();
    card.addClassName("kiosk-card");

    // ── Media row: [left info | image] ───────────────────────
    Div mediaRow = new Div();
    mediaRow.addClassName("kiosk-card-media");

    // Left column: price + available days
    Div left = new Div();
    left.addClassName("kiosk-card-left");

    if (item.getPrice() != null) {
      String priceText = String.format("$%.2f", item.getPrice());
      if (item.getUnit() != null && !item.getUnit().isBlank()) {
        priceText += " / " + item.getUnit();
      }
      Span price = new Span(priceText);
      price.addClassName("kiosk-card-price");
      left.add(price);
    }

    if (item.getAvailableDays() != null && !item.getAvailableDays().isBlank()) {
      Span days = new Span(item.getAvailableDays().replace(",", " · "));
      days.addClassName("kiosk-card-days");
      left.add(days);
    }

    mediaRow.add(left);

    // Image (right side)
    String imgUrl = bestImageUrl(item);
    if (imgUrl != null) {
      Image img = new Image(imgUrl, item.getDescription());
      img.addClassName("kiosk-card-img");
      mediaRow.add(img);
    } else {
      Div placeholder = new Div();
      placeholder.addClassName("kiosk-card-img-placeholder");
      mediaRow.add(placeholder);
    }

    card.add(mediaRow);

    // Name below the media row
    Span name = new Span(item.getDescription());
    name.addClassName("kiosk-card-name");
    card.add(name);

    return card;
  }

  private String bestImageUrl(Donut item) {
    if (notBlank(item.getImageSmall()))  return fixExt(item.getImageSmall());
    if (notBlank(item.getImageMedium())) return fixExt(item.getImageMedium());
    if (notBlank(item.getUrl()))         return fixExt(item.getUrl());
    return null;
  }

  private String fixExt(String url) {
    return (url != null && url.endsWith(".webp")) ? url.replace(".webp", ".png") : url;
  }

  private static final List<String> DAY_ORDER =
      List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");

  private int dayOrder(String availableDays) {
    if (!notBlank(availableDays)) return -1;
    for (int i = 0; i < DAY_ORDER.size(); i++) {
      if (availableDays.contains(DAY_ORDER.get(i))) return i;
    }
    return DAY_ORDER.size();
  }

  /**
   * Parses an availableDays string into a Set of DayOfWeek values.
   * Handles comma-separated values, ranges (e.g. "Mon-Fri"), short names ("Mon"),
   * full names ("Monday"), and full uppercase enum names ("MONDAY").
   */
  private Set<DayOfWeek> parseAvailableDays(String input) {
    Set<DayOfWeek> result = EnumSet.noneOf(DayOfWeek.class);
    if (input == null || input.isBlank()) return result;

    for (String part : input.split(",")) {
      part = part.trim();
      if (part.contains("-")) {
        String[] range = part.split("-", 2);
        DayOfWeek start = parseDay(range[0].trim());
        DayOfWeek end   = parseDay(range[1].trim());
        if (start != null && end != null) {
          int s = start.getValue(), e = end.getValue();
          if (s <= e) {
            for (int i = s; i <= e; i++) result.add(DayOfWeek.of(i));
          } else {
            for (int i = s; i <= 7; i++) result.add(DayOfWeek.of(i));
            for (int i = 1; i <= e; i++) result.add(DayOfWeek.of(i));
          }
        }
      } else {
        DayOfWeek day = parseDay(part);
        if (day != null) result.add(day);
      }
    }
    return result;
  }

  private DayOfWeek parseDay(String text) {
    return switch (text.trim().toLowerCase()) {
      case "mon", "monday"                       -> DayOfWeek.MONDAY;
      case "tue", "tues", "tuesday"              -> DayOfWeek.TUESDAY;
      case "wed", "wednesday"                    -> DayOfWeek.WEDNESDAY;
      case "thu", "thur", "thurs", "thursday"   -> DayOfWeek.THURSDAY;
      case "fri", "friday"                       -> DayOfWeek.FRIDAY;
      case "sat", "saturday"                     -> DayOfWeek.SATURDAY;
      case "sun", "sunday"                       -> DayOfWeek.SUNDAY;
      default                                    -> null;
    };
  }

  private boolean notBlank(String s) {
    return s != null && !s.isBlank();
  }
}
