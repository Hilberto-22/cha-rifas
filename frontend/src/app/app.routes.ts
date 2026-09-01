import { Routes } from '@angular/router';
import { adminGuard } from './admin.guard';
import { AdminDashboard } from './admin-dashboard';
import { AdminLogin } from './admin-login';
import { RafflePage } from './raffle-page';
export const routes: Routes = [
  { path: '', component: RafflePage },
  { path: 'admin/login', component: AdminLogin },
  { path: 'admin', component: AdminDashboard, canActivate: [adminGuard] },
  { path: '**', redirectTo: '' },
];
