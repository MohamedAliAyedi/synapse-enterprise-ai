# End‑to‑End Routing Test Documentation

This document explains the purpose, setup, and execution steps for the end‑to‑end routing test that verifies navigation to `/dashboard` loads the `DashboardComponent` which contains the `StreamViewerComponent`.

## Test Scenario
- Navigate to `/dashboard` via the browser.
- Assert that the URL matches `/dashboard`.
- Confirm that the `StreamViewerComponent` is rendered inside the dashboard.

## Prerequisites
- Angular CLI installed.
- Cypress installed (`npm run test:e2e` or `npx cypress open`).
- Backend services running (or mocked).

## Test File Location
`frontend/src/e2e/dashboard-routing.cy.ts`

## Key Assertions
- URL ends with `/dashboard`.
- Dashboard component is present in the DOM.
- StreamViewer component exists within dashboard.

## Running the Test
```bash
# Start the application
npm run start

# In a separate terminal, run the Cypress tests
npm run test:e2e
```

The test file is referenced in the `cypress.json` configuration.
