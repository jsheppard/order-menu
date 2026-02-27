package com.sbsolutions.util;

import com.sbsolutions.order.models.Donut;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.DayOfWeek;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class KioskLogicTest {

  // ── notBlank ─────────────────────────────────────────────────────────────

  @Test
  void notBlank_null_returnsFalse() {
    assertThat(KioskLogic.notBlank(null)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   ", "\t", "\n"})
  void notBlank_blankStrings_returnsFalse(String s) {
    assertThat(KioskLogic.notBlank(s)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"a", " a ", "hello world", "0"})
  void notBlank_nonBlankStrings_returnsTrue(String s) {
    assertThat(KioskLogic.notBlank(s)).isTrue();
  }

  // ── fixExt ────────────────────────────────────────────────────────────────

  @Test
  void fixExt_null_returnsNull() {
    assertThat(KioskLogic.fixExt(null)).isNull();
  }

  @Test
  void fixExt_webpUrl_replaceWithPng() {
    assertThat(KioskLogic.fixExt("image.webp")).isEqualTo("image.png");
  }

  @Test
  void fixExt_httpWebpUrl_replaceWithPng() {
    assertThat(KioskLogic.fixExt("https://example.com/donut.webp"))
        .isEqualTo("https://example.com/donut.png");
  }

  @ParameterizedTest
  @ValueSource(strings = {"image.png", "image.jpg", "image.jpeg", "image.gif", ""})
  void fixExt_nonWebpExtension_returnsUnchanged(String url) {
    assertThat(KioskLogic.fixExt(url)).isEqualTo(url);
  }

  // ── bestImageUrl ─────────────────────────────────────────────────────────

  @Test
  void bestImageUrl_allNull_returnsNull() {
    Donut d = new Donut();
    assertThat(KioskLogic.bestImageUrl(d)).isNull();
  }

  @Test
  void bestImageUrl_onlyUrl_returnsUrl() {
    Donut d = new Donut();
    d.setUrl("https://example.com/donut.png");
    assertThat(KioskLogic.bestImageUrl(d)).isEqualTo("https://example.com/donut.png");
  }

  @Test
  void bestImageUrl_urlAndMedium_prefersMedium() {
    Donut d = new Donut();
    d.setUrl("https://example.com/original.png");
    d.setImageMedium("https://example.com/medium.png");
    assertThat(KioskLogic.bestImageUrl(d)).isEqualTo("https://example.com/medium.png");
  }

  @Test
  void bestImageUrl_allSet_prefersSmall() {
    Donut d = new Donut();
    d.setUrl("https://example.com/original.png");
    d.setImageMedium("https://example.com/medium.png");
    d.setImageSmall("https://example.com/small.png");
    assertThat(KioskLogic.bestImageUrl(d)).isEqualTo("https://example.com/small.png");
  }

  @Test
  void bestImageUrl_smallIsWebp_convertsExtension() {
    Donut d = new Donut();
    d.setImageSmall("https://example.com/small.webp");
    assertThat(KioskLogic.bestImageUrl(d)).isEqualTo("https://example.com/small.png");
  }

  @Test
  void bestImageUrl_blankSmallFallsBackToMedium() {
    Donut d = new Donut();
    d.setImageSmall("   ");
    d.setImageMedium("https://example.com/medium.png");
    assertThat(KioskLogic.bestImageUrl(d)).isEqualTo("https://example.com/medium.png");
  }

  // ── dayOrder ─────────────────────────────────────────────────────────────

  @Test
  void dayOrder_null_returnsNegativeOne() {
    assertThat(KioskLogic.dayOrder(null)).isEqualTo(-1);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void dayOrder_blankInput_returnsNegativeOne(String s) {
    assertThat(KioskLogic.dayOrder(s)).isEqualTo(-1);
  }

  @ParameterizedTest
  @CsvSource({
      "Mon,     0",
      "Monday,  0",
      "Tue,     1",
      "Wed,     2",
      "Thu,     3",
      "Fri,     4",
      "Sat,     5",
      "Sun,     6",
  })
  void dayOrder_recognisedDays_returnsCorrectIndex(String input, int expected) {
    assertThat(KioskLogic.dayOrder(input)).isEqualTo(expected);
  }

  @Test
  void dayOrder_unrecognisedText_returns7() {
    assertThat(KioskLogic.dayOrder("xyz")).isEqualTo(7);
  }

  @Test
  void dayOrder_firstDayInString_isUsed() {
    // "Mon,Fri" — Mon appears first in DAY_ORDER so index 0 is returned
    assertThat(KioskLogic.dayOrder("Mon,Fri")).isEqualTo(0);
  }

  // ── parseDay ─────────────────────────────────────────────────────────────

  @Test
  void parseDay_null_returnsNull() {
    assertThat(KioskLogic.parseDay(null)).isNull();
  }

  @Test
  void parseDay_unknownToken_returnsNull() {
    assertThat(KioskLogic.parseDay("xyz")).isNull();
  }

  @ParameterizedTest
  @CsvSource({
      "mon,     MONDAY",
      "Mon,     MONDAY",
      "MON,     MONDAY",
      "monday,  MONDAY",
      "Monday,  MONDAY",
      "tue,     TUESDAY",
      "tues,    TUESDAY",
      "tuesday, TUESDAY",
      "wed,     WEDNESDAY",
      "wednesday, WEDNESDAY",
      "thu,     THURSDAY",
      "thur,    THURSDAY",
      "thurs,   THURSDAY",
      "thursday, THURSDAY",
      "fri,     FRIDAY",
      "friday,  FRIDAY",
      "sat,     SATURDAY",
      "saturday, SATURDAY",
      "sun,     SUNDAY",
      "sunday,  SUNDAY",
  })
  void parseDay_allAliases(String input, DayOfWeek expected) {
    assertThat(KioskLogic.parseDay(input)).isEqualTo(expected);
  }

  @Test
  void parseDay_leadingTrailingWhitespace_isStripped() {
    assertThat(KioskLogic.parseDay("  Mon  ")).isEqualTo(DayOfWeek.MONDAY);
  }

  // ── parseAvailableDays ────────────────────────────────────────────────────

  @Test
  void parseAvailableDays_null_returnsEmpty() {
    assertThat(KioskLogic.parseAvailableDays(null)).isEmpty();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void parseAvailableDays_blankInput_returnsEmpty(String s) {
    assertThat(KioskLogic.parseAvailableDays(s)).isEmpty();
  }

  @Test
  void parseAvailableDays_singleDay() {
    assertThat(KioskLogic.parseAvailableDays("Mon")).containsExactlyInAnyOrder(DayOfWeek.MONDAY);
  }

  @Test
  void parseAvailableDays_commaSeparatedDays() {
    Set<DayOfWeek> result = KioskLogic.parseAvailableDays("Mon,Wed,Fri");
    assertThat(result).containsExactlyInAnyOrder(
        DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
  }

  @Test
  void parseAvailableDays_commaSeparatedWithSpaces() {
    Set<DayOfWeek> result = KioskLogic.parseAvailableDays("Mon, Wed, Fri");
    assertThat(result).containsExactlyInAnyOrder(
        DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
  }

  @Test
  void parseAvailableDays_rangeMonToFri() {
    Set<DayOfWeek> result = KioskLogic.parseAvailableDays("Mon-Fri");
    assertThat(result).containsExactlyInAnyOrder(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
  }

  @Test
  void parseAvailableDays_rangeFriToSun() {
    Set<DayOfWeek> result = KioskLogic.parseAvailableDays("Fri-Sun");
    assertThat(result).containsExactlyInAnyOrder(
        DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
  }

  @Test
  void parseAvailableDays_rangeWrapsAroundWeek() {
    // Sat-Mon should include Sat, Sun, Mon
    Set<DayOfWeek> result = KioskLogic.parseAvailableDays("Sat-Mon");
    assertThat(result).containsExactlyInAnyOrder(
        DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.MONDAY);
  }

  @Test
  void parseAvailableDays_singleDayRange_returnsThatDay() {
    Set<DayOfWeek> result = KioskLogic.parseAvailableDays("Wed-Wed");
    assertThat(result).containsExactlyInAnyOrder(DayOfWeek.WEDNESDAY);
  }

  @Test
  void parseAvailableDays_mixedCommaAndRange() {
    // "Mon-Wed,Fri" → Mon,Tue,Wed,Fri
    Set<DayOfWeek> result = KioskLogic.parseAvailableDays("Mon-Wed,Fri");
    assertThat(result).containsExactlyInAnyOrder(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
  }

  @Test
  void parseAvailableDays_unknownToken_skipped() {
    Set<DayOfWeek> result = KioskLogic.parseAvailableDays("xyz,Mon");
    assertThat(result).containsExactlyInAnyOrder(DayOfWeek.MONDAY);
  }

  @Test
  void parseAvailableDays_fullDayNames() {
    Set<DayOfWeek> result = KioskLogic.parseAvailableDays("Monday,Wednesday,Friday");
    assertThat(result).containsExactlyInAnyOrder(
        DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
  }
}
