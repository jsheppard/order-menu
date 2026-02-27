# order-menu

Digital kiosk display for Randy's Donuts. Built with **Spring Boot 3** and **Vaadin 24**.

The single-page kiosk view auto-refreshes product data every 15 minutes and displays
donuts, donut holes, and rolls in horizontally scrolling carousels alongside a live
pricing sidebar and rotating daily specials.

---

## Architecture

```
order-menu (this app)
├── depends on → order-models  (shared domain POJOs: Donut, Roll, PricingSheet, …)
└── depends on → order-client  (REST clients: DonutsClient, RollClient, PricingSheetClient)
```

### Key classes

| Class | Purpose |
|---|---|
| `Application` | Spring Boot entry point; configures PWA, Push, and Vaadin theme. |
| `KioskView` | Full-screen kiosk display (`/`). Loads products, builds carousels and sidebar. Schedules auto-refresh every 15 minutes. |
| `MainLayout` | `AppLayout` shell used by the admin-facing `MenuView`. |
| `MenuView` | Placeholder admin route (`/menu`). |
| `NoCacheFilter` | Servlet filter that adds `no-store` cache headers to every response. |
| `KioskLogic` | Stateless utility methods extracted from `KioskView` (day parsing, image URL selection, etc.). |

### Data flow

```
KioskView (onAttach)
  └─ loadData()
       ├─ DonutsClient.findAll()      → List<Donut>
       ├─ RollClient.findAll()        → List<Roll>
       └─ PricingSheetClient.findAll()→ List<PricingSheet>
            │
            ├─ content div: createSection() × 3  (Donuts / Donut Holes / Rolls)
            │    └─ createScrollingRow() → CSS marquee animation
            │         └─ createCard() × n
            │
            └─ pricesSidebar div: pricing rows + today's specials carousel
```

---

## Configuration

All configuration is in `src/main/resources/application.properties`.

| Property | Default | Description |
|---|---|---|
| `PORT` | `8082` | HTTP port (overridden to `8080` in Docker / Fly.io). |
| `ORDER_DATA_REST_URL` | `https://order-data.fly.dev` | Base URL of the `order-data` REST API. |

---

## Development

### Prerequisites

- Java 21+
- Maven 3.9+ (or use the included `./mvnw` wrapper)
- The `order-models` and `order-client` snapshots must be installed in your local Maven repository. Build them from their respective directories first:

```bash
cd ../order-models && mvn install -DskipTests
cd ../order-client && mvn install -DskipTests
```

### Run locally

```bash
./mvnw spring-boot:run
```

The app starts on port **8082** by default. Open `http://localhost:8082` for the kiosk view.

Vaadin dev-tools (live reload) are active in development mode.

### Run tests

```bash
./mvnw test
```

Tests do **not** start a Spring context or a browser — they are plain JUnit 5 unit tests.

---

## Testing

Tests live under `src/test/java/com/sbsolutions/`.

| Test class | Covers |
|---|---|
| `util.KioskLogicTest` | All pure-logic helpers in `KioskLogic` — day parsing, image URL selection, extension fixing, `notBlank`. 74 test cases including parameterised and edge-case coverage. |
| `components.NoCacheFilterTest` | `NoCacheFilter.doFilter()` — verifies `Cache-Control`, `Pragma`, and `Expires` headers are set before the chain is invoked. |

### `KioskLogic` helper methods

`KioskLogic` (package `com.sbsolutions.util`) contains the stateless helpers used by `KioskView`:

- **`notBlank(String)`** — `true` when non-null and non-blank.
- **`fixExt(String)`** — replaces a `.webp` extension with `.png`.
- **`bestImageUrl(Donut)`** — returns the best available image URL (`imageSmall` → `imageMedium` → `url`), run through `fixExt`.
- **`dayOrder(String)`** — zero-based sort index (Mon=0 … Sun=6) of the first recognised day abbreviation in a string.
- **`parseDay(String)`** — converts a day name or abbreviation to `DayOfWeek` (case-insensitive; supports `Mon`/`Monday`/`Tue`/`Tues`/`Tuesday` etc.).
- **`parseAvailableDays(String)`** — parses a comma-separated day string (supports individual days and ranges like `Mon-Fri` or wrap-around ranges like `Sat-Mon`) into a `Set<DayOfWeek>`.

---

## Production build

The production build enables Vaadin production mode (pre-bundled frontend assets):

```bash
./mvnw package -Pproduction -DskipTests
java -jar target/order-menu-1.0-SNAPSHOT.jar
```

---

## Docker / Fly.io deployment

The Dockerfile is a multi-stage build. The **build context must be the parent directory**
(so the sibling `order-models` and `order-client` projects are accessible):

```bash
# From the parent directory
docker build -f order-menu/Dockerfile .

# Or deploy directly with Fly.io
fly deploy --config order-menu/fly.toml --build-context ../
```

Fly.io app: `rdonutslnk-menu` (region: `dfw`).

Environment variables set by `fly.toml`:

| Variable | Value |
|---|---|
| `PORT` | `8080` |
| `ORDER_DATA_REST_URL` | `https://order-data.fly.dev` |
