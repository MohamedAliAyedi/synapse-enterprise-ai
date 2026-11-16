import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { StreamViewerComponent } from './components/stream-viewer/stream-viewer.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';

/**
 * Application routing configuration.
 *
 * - The default route redirects to the dashboard.
 * - The `/stream` route displays the live streaming viewer.
 * - Additional routes can be added here as the application grows.
 */
const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  // Fixed missing route for the StreamViewerComponent
  { path: 'stream', component: StreamViewerComponent },
  // Wildcard route for a 404 page (optional)
  { path: '**', redirectTo: '/dashboard' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { scrollPositionRestoration: 'enabled' })],
  exports: [RouterModule]
})
export class AppRoutingModule {}
