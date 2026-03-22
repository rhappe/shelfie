# Shelfie — Product Requirements Document

**Version:** 0.1 (Draft)
**Date:** 2026-03-22
**Status:** Draft

---

## 1. Overview

Shelfie is a cross-platform pantry management application that helps households track their food inventory, reduce waste, and streamline grocery shopping. It is self-hostable, offline-capable, and designed to grow from a simple inventory tracker into a full household kitchen management tool.

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
- As a user, my data syncs across devices and is available offline.

### Phase 2 — Shopping Lists

- As a user, I can add items to the shopping list from my pantry with a single tap, or as free-form entries not tied to any pantry item.
- The app remembers the last quantity I specified for each pantry-linked item to pre-fill future shopping trips.
- As a user, I can view and edit the shopping list, checking off items as I shop.
- As a user, I can tap "Done Shopping" to automatically update pantry quantities for all checked pantry-linked items.
- For any checked free-form items, I am prompted to optionally add them to my pantry as new entries.

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
| Unit | String | Yes | e.g., "oz", "lbs", "count", "cups" — user-selectable from a list or custom entry |
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

### 3.4 Shopping List (Phase 2)

- A single active shopping list at a time.
- Each entry: item name, desired quantity, unit, checked state, and a flag indicating whether it is linked to a pantry item or is a free-form entry.
- "Add to List" button on each pantry item opens a quantity dialog pre-filled with last-used quantity.
- Free-form items can be typed directly into the shopping list with a name, quantity, and unit.
- Shopping mode: full-screen list optimized for one-handed use while in-store.
- "Done Shopping" flow:
  1. Confirmation dialog showing all checked items and quantities.
  2. On confirm: pantry quantities are incremented for all checked pantry-linked items.
  3. For any checked free-form items: prompt asking whether to add each to the pantry. User can set category and adjust details before confirming.
  4. List is cleared.

### 3.5 Recipes (Phase 3)

| Field | Type |
|-------|------|
| Title | String |
| Description | String (optional) |
| Ingredients | List of { name, quantity, unit, isQuantifiable } |
| Instructions | Rich text or ordered list of steps |
| Tags | List of strings |

- Pantry match indicator: "Can make", "Missing X items", "Short on X items".
- "Make This" deduction tiers:
  1. **Exact unit match** — deducted automatically.
  2. **Same-dimension unit conversion** — resolved automatically using a built-in conversion table (volume: cups, tbsp, tsp, fl oz, ml, L; weight: oz, lbs, g, kg). Deducted automatically.
  3. **Weight ↔ volume mismatch** — cannot be resolved without ingredient density data; user is prompted to confirm or skip.
  4. **Item not found in pantry** — user is prompted to confirm or skip.
  5. **Non-quantifiable** (e.g., "pinch of salt") — shown as a reminder only; never deducted.
- Unit conversion logic lives in the backend.

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
│  │    cache     │  │  own impl if   │    │
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
- Cache responses locally for offline reads (Android/iOS only via SQLDelight)
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
| SQLDelight | Local read cache for offline support (Android; skip on web) |
| ML Kit | Barcode scanning (androidMain only — future) |

**Offline support (Android only):**
- SQLDelight is used as a read cache only — the backend is the source of truth
- Writes are sent directly to the API; on success the local cache is updated
- On network loss, the app shows cached data with a "you're offline" indicator
- No complex sync/conflict resolution needed given the thin-client approach

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
| Offline / connectivity lost | Persistent banner |
| Catastrophic / unrecoverable error | Full-screen error state with retry |

---

## 5. Data Sync & Offline Support

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
- On launch / foreground: fetch fresh data from API, update local cache.
- While offline (Android): serve cached SQLDelight data with an "you're offline" indicator.

**Offline writes:** Not supported in Phase 1. Write actions are blocked when offline with an explanatory message, avoiding conflict resolution complexity.

---

## 6. Non-Functional Requirements

| Requirement | Target |
|-------------|--------|
| Offline capability | Full read/write while offline; sync on reconnect |
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
| Phase 1 | Pantry CRUD, categories, offline sync, Android + Web | MVP |
| Phase 2 | Shopping lists, household multi-user | Post-MVP |
| Phase 3 | Notifications (low-stock + expiration), recipes, pantry match indicator, "Make This" deduction | Future |
| Phase 4 | iOS native app | Indefinitely TBD (requires Apple Developer account) |
