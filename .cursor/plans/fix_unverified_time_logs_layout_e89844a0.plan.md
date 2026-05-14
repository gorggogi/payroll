---
name: Fix Unverified Time Logs layout
overview: Restructure the "Unverified Time Logs" section on the time-adjustments page into a clean, compact card-based layout with a thin employee header row and a structured horizontal grid for time fields. Replace inline styles with proper CSS classes in `time-adjustments.css`.
todos:
  - id: css-rewrite
    content: Rewrite time-adjustments.css with card-based layout system
    status: completed
  - id: html-restructure
    content: Restructure HTML template with compact header + horizontal time grid
    status: completed
isProject: false
---

## Plan: Fix "Unverified Time Logs" Layout

### Changes: 2 files

---

#### 1. `src/main/resources/static/css/time-adjustments.css`

**Replace all existing content** with a clean layout system:

- `**.log-card`** -- Each employee row becomes a card with a white background, border, border-radius, and shadow. This replaces the flat `.adjust-import-summary`.
- `**.log-card-header`** -- A single flex row for the compact employee info: verify checkbox, employee number, name, biometric ID, date, and shift. Styled with a light background, smaller font, and gap between items.
- `**.log-card-body`** -- A horizontal flex row containing three labeled sections:
  - `**.time-section**` (Time In, Time Out) -- contains radio options + editable text input stacked vertically
  - `**.break-section**` (Break) -- contains two inputs side by side with a "to" label between them
- `**.time-section**` -- flex column with a bold label on top, radio options below, and the editable input at the bottom
- `**.time-option**` -- a flex row of radio buttons with their time labels (flex-wrap so they reflow on small screens)
- `**.time-section input[type="text"]**` -- consistent styling with `time-adjustments.css` variables (border, padding, border-radius)

#### 2. `src/main/resources/templates/html/time-adjustments.html`

**Replace lines 93-150** (the `th:if="${previewRows != null}"` panel):

Replace the current flat, unstructured HTML with a card-based structure:

```html
<!-- each employee row becomes a .log-card -->
<div th:each="row : ${previewRows}" class="log-card">
    <!-- compact header row -->
    <div class="log-card-header">
        <label class="log-verify-label">
            <input type="checkbox" th:name="${'verify_' + row.key}" value="true" checked />
            Verify
        </label>
        <span class="log-emp-num" th:text="${row.employeeNumber}">EMP</span>
        <span class="log-emp-name" th:text="${row.employeeName}">Name</span>
        <span class="log-meta"><i class="fa-solid fa-fingerprint"></i> <span th:text="${row.biometricId}">Bio</span></span>
        <span class="log-meta"><i class="fa-solid fa-calendar"></i> <span th:text="${row.displayDate}">Date</span></span>
        <span class="log-meta"><i class="fa-solid fa-clock"></i> <span th:text="${row.shiftLabel}">Shift</span></span>
    </div>
    <!-- horizontal time fields -->
    <div class="log-card-body">
        <!-- Time In -->
        <div class="time-section">
            <div class="time-section-label">Time In</div>
            <div class="time-options">
                <label th:each="v : ${row.inCandidates}" class="time-option">
                    <input type="radio" th:name="${'inPick_' + row.key}" th:value="${v}"
                           th:checked="${v == row.selectedIn}"
                           th:attr="data-target=${'timeInEdit_' + row.key}" />
                    <span th:text="${row.format12h(v)}">08:00 AM</span>
                </label>
            </div>
            <input type="text" class="time-direct-input" th:name="${'timeInEdit_' + row.key}"
                   th:value="${row.selectedIn12h}" placeholder="hh:mm a" />
        </div>
        <!-- Break -->
        <div class="break-section">
            <div class="time-section-label">Break</div>
            <div class="break-inputs">
                <input type="text" th:name="${'breakOutEdit_' + row.key}"
                       th:value="${row.breakOut}" placeholder="Out" />
                <span class="break-sep">to</span>
                <input type="text" th:name="${'breakInEdit_' + row.key}"
                       th:value="${row.breakIn}" placeholder="In" />
            </div>
        </div>
        <!-- Time Out -->
        <div class="time-section">
            <div class="time-section-label">Time Out</div>
            <div class="time-options">
                <label th:each="v : ${row.outCandidates}" class="time-option">
                    <input type="radio" th:name="${'outPick_' + row.key}" th:value="${v}"
                           th:checked="${v == row.selectedOut}"
                           th:attr="data-target=${'timeOutEdit_' + row.key}" />
                    <span th:text="${row.format12h(v)}">05:00 PM</span>
                </label>
            </div>
            <input type="text" class="time-direct-input" th:name="${'timeOutEdit_' + row.key}"
                   th:value="${row.selectedOut12h}" placeholder="hh:mm a" />
        </div>
    </div>
</div>
```

**Remove inline `<style>` block** (lines 178-237) since all styles move to `time-adjustments.css`.

**No changes** to the upload panel (lines 28-91), the JavaScript (lines 154-176), or the modal (lines 40-56).