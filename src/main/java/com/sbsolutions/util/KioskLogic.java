package com.sbsolutions.util;

import com.sbsolutions.order.models.Donut;
import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Pure, stateless helper methods extracted from {@code KioskView}.
 *
 * <p>All methods are static so this class is trivially unit-testable without any
 * Spring or Vaadin context.
 */
public final class KioskLogic {

  private static final List<String> DAY_ORDER =
      List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");

  private KioskLogic() {}

  // ── String utilities ─────────────────────────────────────────────────────

  /** Returns {@code true} when {@code s} is non-null and non-blank. */
  public static boolean notBlank(String s) {
    return s != null && !s.isBlank();
  }

  /**
   * Replaces a {@code .webp} extension with {@code .png}.
   * Returns the original value (including {@code null}) unchanged if it does not end in
   * {@code .webp}.
   */
  public static String fixExt(String url) {
    return (url != null && url.endsWith(".webp")) ? url.replace(".webp", ".png") : url;
  }

  // ── Image resolution ─────────────────────────────────────────────────────

  /**
   * Returns the best available image URL for a {@link Donut}, preferring
   * {@code imageSmall} → {@code imageMedium} → {@code url}, or {@code null} when none
   * are set. The chosen URL is passed through {@link #fixExt}.
   */
  public static String bestImageUrl(Donut item) {
    if (notBlank(item.getImageSmall()))  return fixExt(item.getImageSmall());
    if (notBlank(item.getImageMedium())) return fixExt(item.getImageMedium());
    if (notBlank(item.getUrl()))         return fixExt(item.getUrl());
    return null;
  }

  // ── Day-of-week helpers ──────────────────────────────────────────────────

  /**
   * Returns the zero-based display sort index (0 = Mon … 6 = Sun) of the first
   * recognizable day abbreviation found in {@code availableDays}, or {@code -1} when
   * {@code availableDays} is blank/null, or {@code 7} when no recognizable abbreviation
   * is found.
   */
  public static int dayOrder(String availableDays) {
    if (!notBlank(availableDays)) return -1;
    for (int i = 0; i < DAY_ORDER.size(); i++) {
      if (availableDays.contains(DAY_ORDER.get(i))) return i;
    }
    return DAY_ORDER.size();
  }

  /**
   * Parses a comma-separated {@code availableDays} string into a {@link Set} of
   * {@link DayOfWeek} values.
   *
   * <p>Supported formats per token (case-insensitive):
   * <ul>
   *   <li>Single day: {@code Mon}, {@code Monday}, {@code Tue}, {@code Tuesday}, …</li>
   *   <li>Range: {@code Mon-Fri}, {@code Sat-Mon} (wraps around Sunday→Monday)</li>
   * </ul>
   *
   * @param input comma-separated day string; may be {@code null} or blank
   * @return non-null, possibly empty {@link Set} of matching {@link DayOfWeek} values
   */
  public static Set<DayOfWeek> parseAvailableDays(String input) {
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
          } else {                     // wraps past Sunday back to Monday
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

  /**
   * Converts a single day name/abbreviation to a {@link DayOfWeek}.
   * Comparison is case-insensitive. Supported aliases:
   * <ul>
   *   <li>Mon / Monday</li>
   *   <li>Tue / Tues / Tuesday</li>
   *   <li>Wed / Wednesday</li>
   *   <li>Thu / Thur / Thurs / Thursday</li>
   *   <li>Fri / Friday</li>
   *   <li>Sat / Saturday</li>
   *   <li>Sun / Sunday</li>
   * </ul>
   *
   * @param text day name; leading/trailing whitespace is stripped before comparison
   * @return matching {@link DayOfWeek}, or {@code null} for unrecognised input
   */
  public static DayOfWeek parseDay(String text) {
    if (text == null) return null;
    return switch (text.trim().toLowerCase()) {
      case "mon", "monday"                     -> DayOfWeek.MONDAY;
      case "tue", "tues", "tuesday"            -> DayOfWeek.TUESDAY;
      case "wed", "wednesday"                  -> DayOfWeek.WEDNESDAY;
      case "thu", "thur", "thurs", "thursday" -> DayOfWeek.THURSDAY;
      case "fri", "friday"                     -> DayOfWeek.FRIDAY;
      case "sat", "saturday"                   -> DayOfWeek.SATURDAY;
      case "sun", "sunday"                     -> DayOfWeek.SUNDAY;
      default                                  -> null;
    };
  }
}
