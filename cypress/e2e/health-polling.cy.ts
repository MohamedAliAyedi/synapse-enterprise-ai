/// <reference types="cypress" />

describe('Health Check Polling', () => {
  beforeEach(() => {
    // Assuming the dashboard is served at the root route
    cy.visit('/')
  })

  it('should display health status and update on subsequent polls', () => {
    // The component should initially show a loading placeholder
    cy.get('[data-cy=health-status]')
      .should('exist')
      .and('contain.text', 'Loading')

    // Mock the backend health endpoint: first response UP, then DOWN
    cy.intercept('GET', '/api/health', { statusCode: 200, body: { status: 'UP' } }).as('healthUp')
    cy.intercept('GET', '/api/health', { statusCode: 200, body: { status: 'DOWN' } }).as('healthDown')

    // Wait for the first poll (default interval is 5 seconds)
    cy.wait('@healthUp')
    cy.get('[data-cy=health-status]').should('contain.text', 'UP')

    // Fast‑forward time to trigger the next poll
    cy.clock()
    cy.tick(5000)
    cy.wait('@healthDown')
    cy.get('[data-cy=health-status]').should('contain.text', 'DOWN')
  })
})
