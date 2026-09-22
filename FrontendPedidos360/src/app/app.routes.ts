import { Routes } from '@angular/router';
import { MsalGuard } from '@azure/msal-angular';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { OrdersComponent } from './components/orders/orders.component';
import { CatalogComponent } from './components/catalog/catalog.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [MsalGuard] },
  { path: 'orders', component: OrdersComponent, canActivate: [MsalGuard] },
  { path: 'catalog', component: CatalogComponent, canActivate: [MsalGuard] },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];