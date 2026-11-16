/// <reference types="cypress" />

describe('Application Routing', () => {
  /**
   * The application redirects the empty path ('/') to the dashboard.
   * This test verifies that behaviour.
   */
  it('redirects root to /dashboard', () => {
    cy.visit('/');
    cy.url().should('include', '/dashboard');
    // Verify that the dashboard component rendered (e.g., a heading exists)
    cy.get('h1').contains('Dashboard');
  });

  /**
   * Verify navigation to the live stream viewer component.
   * The dashboard contains a navigation link to the stream viewer.
   */
  it('navigates to the StreamViewerComponent', () => {
    cy.visit('/dashboard');
    // Assuming there is a routerLink or button that points to /stream-viewer
    cy.get('a[href="/stream-viewer"]').click();
    cy.url().should('include', '/stream-viewer');
    // Confirm that the StreamViewerComponent UI appears
    cy.get('h2').contains('Live Stream');
    // Verify the SSE connection placeholder element exists
    cy.get('[data-cy="stream-output"]').should('exist');
  });
});