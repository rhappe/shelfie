# Shelfie — Product Requirements Document

**Version:** 0.1 (Draft)
**Date:** 2026-03-22
**Status:** Draft

---

## 1. Overview

Shelfie is a cross-platform pantry management application that helps households track their food inventory, reduce waste, and streamline grocery shopping. It is self-hostable and designed to grow from a simple inventory tracker into a full household kitchen management tool.

### 1.1 Vision

Give households a single source of truth for what's in their pantry, reduce food waste through expiration tracking, and make grocery shopping and meal planning a natural extension of knowing what you already have.

### 1.2 Target Platforms

| Platform | Phase |
|----------|-------|
| Android  | Phase 1 |
| Web      | Phase 1 |
| iOS      | Phase 4 (indefinitely TBD — pending Apple Developer account) |

---

## 2. User Stories

### Phase 1 — Pantry Management

- As a user, I can add items to my pantry with a name, quantity, unit of measure, optional expiration date, and category.
- As a user, I can edit or remove pantry items.
- As a user, I can create and manage my own item categories.
- As a user, I can set a low-stock threshold per item (the data model supports this from day one, but notifications are a later phase).
- As a user, I can search my pantry by item name, filtering results as I type (local, no API call).
- As a user, I can view my pantry filtered by category, sorted by name, quantity, or expiration date.
- Search and category filter can be used together.
- As a user, my data syncs across devices and loads instantly from a local cache.

### Phase 2 — Shopping Lists

- A default shopping list named "My List" is created automatically for new users.
- As a user, I can create additional named shopping lists (e.g., "Kroger", "Costco").
- As a user, I can rename or delete a shopping list.
- As a user, I can add items to a shopping list from my pantry with a single tap, which opens a picker to choose which list to add to. The list I last added that item to is pre-selected.
- As a user, I can also add free-form entries to a shopping list not tied to any pantry item.
- The app remembers the last quantity I specified for each pantry-linked item to pre-fill future shopping trips.
- As a user, I can view and edit a shopping list, checking off items as I shop.
- As a user, I can tap "Done Shopping" to automatically update pantry quantities for all checked pantry-linked items.
- For any checked free-form items, I am prompted to optionally add them to my pantry as new entries.
- As a user, I can sort my list of shopping lists by creation order (default), alphabetical, most items, or least items.

### Phase 3 — Recipes

- As a user, I can create personal recipes with: title, description, ingredient list (with quantities and units), and cooking instructions.
- As a user, I can search my recipe list.
- Each recipe displays an at-a-glance indicator of whether I can make it based on current pantry contents.
- As a user, I can tap "Make This" on a recipe to automatically deduct quantifiable ingredients from my pantry.
  - Quantifiable: specific amounts with standard units (e.g., "1 cup flour", "3 tbsp milk").
  - Non-quantifiable: approximate or subjective amounts (e.g., "a pinch of salt", "to taste") — these are displayed as reminders only and not deducted.

---

## 3. Feature Specifications

### 3.1 Pantry Item

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| Name | String | Yes | |
| Quantity | Decimal | Yes | |
| Unit | Reference | Yes | References a system-defined unit of measure (see section 3.6) |
| Category | Reference | No | User-defined |
| Expiration Date | Date | No | |
| Low-Stock Threshold | Decimal | No | Default: 0 |
| Notify on Low Stock | Boolean | Yes | Default: true |
| Barcode | String | No | Stored for future lookup |

### 3.2 Categories

- User-created, named, with an optional description and optional color.
- Suggested defaults on first launch:
  - Produce
  - Dairy & Eggs
  - Meat & Seafood
  - Deli & Prepared
  - Bread & Bakery
  - Grains, Pasta & Rice
  - Canned & Jarred Goods
  - Legumes & Nuts
  - Snacks
  - Breakfast & Cereal
  - Baking Supplies
  - Oils, Vinegars & Sauces
  - Spices & Seasonings
  - Beverages
  - Frozen
  - Household & Other
- Users can rename, reorder, or delete categories (items in deleted categories become uncategorized).

### 3.3 Notifications (Future Phase)

Deferred — implementation details TBD when this phase is reached.

### 3.4 Units of Measure

#### Measurement Types

There are three measurement types. Each type has a **base unit** that serves as the canonical storage format.

| Type | Base Unit | Example Units |
|------|-----------|---------------|
| Weight | grams (g) | g, kg, oz, lbs |
| Volume | milliliters (mL) | mL, L, tsp, tbsp, fl oz, cups, pt, qt, gal |
| Count | count | count, dozen |

#### Unit Definition Table (Backend)

Units are stored in a database table with the following structure:

| Column | Type | Example |
|--------|------|---------|
| type | String | `weight`, `volume`, `count` |
| label | String | `grams`, `pounds`, `cups` |
| short_label | String | `g`, `lbs`, `cups` |
| to_base_ratio | Decimal | `1.0`, `453.5924`, `236.588` |

The `to_base_ratio` defines how many base units equal one of this unit. For example:
- 1 lb = 453.5924 g → `to_base_ratio = 453.5924`
- 1 cup = 236.588 mL → `to_base_ratio = 236.588`
- 1 g = 1 g → `to_base_ratio = 1.0`
- 1 dozen = 12 count → `to_base_ratio = 12.0`

#### Storage and Display

- When a user creates or updates a pantry item, they select their preferred unit and enter a quantity.
- The system stores the quantity converted to the base unit (e.g., 2 lbs is stored as 907.1848 g).
- The user's preferred unit is stored alongside the item.
- For display, the stored base-unit quantity is converted back to the user's preferred unit.

#### Same-Dimension Conversion

Conversions within the same measurement type (weight-to-weight, volume-to-volume) are automatic and exact using the `to_base_ratio` values. No user interaction is needed.

Example: A recipe calls for 2 cups of milk. The pantry has milk stored in liters. Both are volume — the system converts both to mL and deducts automatically.

#### Cross-Dimension Conversion (Weight ↔ Volume)

Converting between weight and volume requires knowing the density of the specific substance, which varies by item (e.g., a cup of water weighs ~236g, but a cup of flour weighs ~125g). The system cannot resolve this automatically.

**Reconciliation flow:**

1. When a deduction requires a cross-dimension conversion (e.g., deducting cups from an item stored in grams), the system prompts the user to declare how much was actually consumed in the item's stored unit.
2. The system calculates and stores a **per-item cross-dimension conversion factor** (e.g., for "All-Purpose Flour": 1 mL ≈ 0.529 g).
3. On subsequent deductions for that item, the stored conversion factor is used automatically — no further prompts.
4. The user can update the conversion factor at any time if it was inaccurate.

This means cross-dimension conversions are learned per item, and the system gets smarter over time.

#### Count Type

Count-based items (e.g., "6 eggs", "1 dozen rolls") cannot be converted to weight or volume. Deductions involving a count item and a weight/volume recipe ingredient require manual reconciliation as described above.

### 3.5 Shopping Lists (Phase 2)

- Users can have multiple named shopping lists. A default list named "My List" is created automatically for new users.
- Each list has a name and an ordered set of entries.
- Each entry: item name, desired quantity, unit, checked state, and a flag indicating whether it is linked to a pantry item or is a free-form entry.
- The shopping lists index screen displays lists in creation order by default. The user can change sort order: creation order, alphabetical, most items, or least items.
- "Add to List" button on each pantry item opens a two-step flow:
  1. A picker showing all shopping lists. The list this item was most recently added to is pre-selected.
  2. A quantity dialog pre-filled with the last-used quantity for that item.
- Free-form items can be typed directly into any shopping list with a name, quantity, and unit.
- Shopping mode: full-screen list optimized for one-handed use while in-store.
- "Done Shopping" flow (per list):
  1. Confirmation dialog showing all checked items and quantities.
  2. On confirm: pantry quantities are incremented for all checked pantry-linked items.
  3. For any checked free-form items: prompt asking whether to add each to the pantry. User can set category and adjust details before confirming.
  4. List is cleared (entries removed; the list itself is retained).

### 3.6 Recipes (Phase 3)

| Field | Type |
|-------|------|
| Title | String |
| Description | String (optional) |
| Ingredients | List of { name, quantity, unit, isQuantifiable } |
| Instructions | Rich text or ordered list of steps |
| Tags | List of strings |

- Pantry match indicator: "Can make", "Missing X items", "Short on X items".
- "Make This" deduction tiers:
  1. **Same-dimension conversion** (e.g., cups → mL, lbs → g) — deducted automatically using the unit conversion system (see section 3.4).
  2. **Cross-dimension conversion** (e.g., cups → g) — if the item has a stored cross-dimension conversion factor, deducted automatically. Otherwise, the user is prompted via the reconciliation flow (see section 3.4) to declare consumption, which teaches the system for future use.
  3. **Item not found in pantry** — user is prompted to confirm or skip.
  4. **Non-quantifiable** (e.g., "pinch of salt") — shown as a reminder only; never deducted.
- All unit conversion logic lives in the backend.

---

## 4. Technical Architecture

### 4.1 High-Level Architecture

Two separate repositories:

| Repo | Contents |
|------|----------|
| `shelfie-app` | Compose Multiplatform client (Android + Web Wasm) |
| `shelfie-server` | Ktor backend + PostgreSQL, deployed independently |

```
┌──────────────────────────────────────────┐
│  shelfie-app (Compose Multiplatform)     │
│  ┌──────────────┐  ┌────────────────┐    │
│  │  Android     │  │  Web (Wasm)    │    │
│  │  + SQLDelight│  │  (migrate to   │    │
│  │    local db  │  │  own impl if   │    │
│  │              │  │  needed)       │    │
│  └──────┬───────┘  └───────┬────────┘    │
└─────────┼──────────────────┼────────────┘
          └────────┬──────────┘
                   │ HTTPS / REST
┌──────────────────▼──────────────────────┐
│  shelfie-server                         │
│  ┌─────────────────┐                    │
│  │  Ktor Backend   │  ← complex logic   │
│  └────────┬────────┘                    │
│           │                             │
│  ┌────────▼────────┐                    │
│  │   PostgreSQL    │                    │
│  └─────────────────┘                    │
└─────────────────────────────────────────┘
```

### 4.2 Client Philosophy: Thin Client

All complex operations (low-stock evaluation, expiration checks, recipe pantry matching, ingredient deduction logic, shopping list reconciliation) are performed by the backend. The client's responsibilities are:

- Render UI and handle user input
- Call API endpoints and display results
- Cache responses locally via SQLDelight (Android/iOS) for instant loads and reactive UI
- Manage navigation and UI state

This keeps the client simple, makes a future web-only reimplementation straightforward, and means business logic is tested and maintained in one place.

### 4.3 Compose Multiplatform App (`shelfie-app`)

A single Compose Multiplatform module produces both the Android app and the Web (Wasm) app.

**Module structure:**

```
:composeApp
  ├── commonMain/
  │   ├── ui/
  │   │   ├── screens/        # All screens (PantryScreen, ShoppingScreen, etc.)
  │   │   ├── components/     # Shared composables
  │   │   └── navigation/     # Nav graph (Jetpack Navigation for CMP)
  │   ├── viewmodel/          # ViewModels / presentation state holders
  │   ├── data/
  │   │   ├── remote/         # Ktor HTTP client, API DTOs
  │   │   └── repository/     # Thin repository implementations (mostly API pass-throughs)
  │   ├── domain/
  │   │   └── model/          # Kotlin data classes mirroring API responses
  │   └── di/                 # Metro modules
  ├── androidMain/
  │   ├── MainActivity.kt     # Single activity entry point
  │   ├── notifications/      # FCM token registration
  │   └── db/                 # SQLDelight local cache
  ├── iosMain/                # Phase 4 — iOS entry point, APNs
  └── wasmJsMain/
      └── main.kt             # Web entry point
```

**Key libraries:**

| Library | Purpose |
|---------|---------|
| Compose Multiplatform | UI (Android + Web Wasm) |
| Jetpack Navigation (CMP) | Navigation — same API as Android nav, multiplatform-aware |
| androidx.lifecycle.ViewModel (2.8+) | Shared ViewModel across Android and Web Wasm — same `StateFlow`-based UI state pattern on both targets |
| Ktor Client | HTTP requests to backend |
| Metro | Compile-time DI across all targets |
| SQLDelight | Local cache for instant loads and reactive UI (Android; skip on web) |
| ML Kit | Barcode scanning (androidMain only — future) |

**Local caching (Android only):**
- SQLDelight serves as a local cache — the backend remains the source of truth
- The UI observes the local database, so screens update reactively
- API responses are written to the cache; the UI renders from the cache, not directly from network responses
- Writes are sent to the API; on success the local cache is updated, which automatically propagates to the UI

### 4.4 Backend (`shelfie-server`)

- **Framework:** Ktor (Kotlin, aligns with KMP ecosystem)
- **Database:** PostgreSQL
- **Auth:** JWT-based authentication; single user or household-scoped multi-user. Token expiry is long-lived and configurable via environment variable (default: 90 days). No refresh token flow.
- **Database migrations:** Flyway — versioned SQL migration files run automatically on server startup
- **Deployment:** Docker Compose (Ktor server + PostgreSQL), deployed independently of the client
- **API style:** REST (JSON); versioned under `/v1/` (see section 4.7)

### 4.5 Self-Hosting

- A `docker-compose.yml` in `shelfie-server` defines the full stack.
- Configuration via environment variables (DB credentials, JWT secret, JWT expiry duration).
- A setup guide documents how to deploy on a personal server or Raspberry Pi.
- Data persistence via a named Docker volume for PostgreSQL.

### 4.7 API Versioning

All endpoints are prefixed with `/v1/` (e.g., `GET /v1/pantry/items`). This allows self-hosters running older server versions to continue using their existing client until they choose to update.

### 4.6 Authentication & Multi-User

- JWT-based login (email + password).
- Households: a user can create a household and invite others via a shareable invite link (no SMTP required). All members share the same pantry, shopping list, and recipe book.
- Role model: Owner and Member (Owner can manage household members; both can edit pantry data).
- This enables partners or family members to collaboratively manage the same pantry from their own devices.

### 4.9 Error Handling

| Scenario | Pattern |
|----------|---------|
| Failed API write (optimistic rollback) | Snackbar |
| Form validation failure | Inline, next to the offending field |
| Network unavailable | Cached data served transparently; failed writes surface via snackbar |
| Catastrophic / unrecoverable error | Full-screen error state with retry |

---

## 5. Data Sync & Local Caching

**Write path — inline quantity adjustments** (e.g., +/- on the pantry list):
1. Each tap updates the UI immediately (optimistic) and resets a debounce timer (~600ms).
2. When the timer fires: if no request is in-flight, send the current absolute quantity to the API. If a request is already in-flight, hold the latest value and wait.
3. When an in-flight request completes: if the value changed while it was running, immediately fire one more request with the latest value.
4. On failure: roll back to the last server-confirmed value and show an error.

This ensures at most one in-flight request per item at any time, no tap is lost, and out-of-order responses are not possible.

**Write path — full item edits** (name, unit, expiration date, category, threshold):
1. User edits fields in a form and taps "Save".
2. UI is updated optimistically.
3. Single API call is made.
4. On failure: changes are rolled back and the form is reopened with an error.

**Read path:**
- The UI observes SQLDelight queries, so data is available instantly from cache on launch.
- On launch / foreground: fetch fresh data from the API and update the local cache, which reactively updates the UI.

---

## 6. Non-Functional Requirements

| Requirement | Target |
|-------------|--------|
| Instant loads | Screens render from local cache immediately; network refreshes in background |
| Cold start time (Android) | < 2 seconds |
| Sync latency | < 5 seconds on reconnect |
| Self-host resource footprint | Runs comfortably on a 1GB RAM VPS or Raspberry Pi 4 |
| Data portability | Export pantry/recipes to JSON or CSV |
| Accessibility | WCAG 2.1 AA for web; Android accessibility guidelines |

---

## 7. Out of Scope (for now)

- AI-generated recipe suggestions
- Nutritional information tracking
- Price tracking / budget management
- Integration with grocery delivery services
- Barcode scanning (item entry via camera + optional Open Food Facts lookup)
- iOS native app (indefinitely TBD; iOS users use the web app in the interim)
- Social/sharing features

---

## 8. Open Questions

1. ~~**Web UI framework:**~~ **Resolved** — Compose Multiplatform (Android + Web Wasm), thin client. Web may be migrated to a platform-specific implementation later if needed.
2. ~~**Barcode lookup:**~~ **Deferred** — barcode scanning moved to future scope.
3. ~~**Expiration notifications:**~~ **Resolved** — warning window is user-configurable (default: 3 days before expiration).
4. ~~**Household invites:**~~ **Resolved** — shareable invite links (no SMTP dependency).
5. ~~**iOS timeline:**~~ **Resolved** — iOS is indefinitely TBD; iOS users will use the web app until an Apple Developer account is available. The `iosMain` target placeholder will be kept in the CMP module for a future native app.

---

## 9. Phased Rollout

| Phase | Features | Target |
|-------|----------|--------|
| Phase 1 | Pantry CRUD, categories, local caching, Android + Web | MVP |
| Phase 2 | Multiple named shopping lists, household multi-user | Post-MVP |
| Phase 3 | Notifications (low-stock + expiration), recipes, pantry match indicator, "Make This" deduction | Future |
| Phase 4 | iOS native app | Indefinitely TBD (requires Apple Developer account) |
