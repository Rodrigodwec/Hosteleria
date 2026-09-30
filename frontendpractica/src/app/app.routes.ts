import { Routes } from '@angular/router';
import { adminGuard } from './core/guards/admin.guard';
import { authGuard } from './core/guards/auth.guard';
import { loginGuard } from './core/guards/login.guard';
import { ShellComponent } from './layout/shell/shell.component';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [loginGuard],
    loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent)
  },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'mesas' },
      {
        path: 'mesas',
        loadComponent: () => import('./pages/mesas/mesas.component').then((m) => m.MesasComponent)
      },
      {
        path: 'mesas/:id',
        loadComponent: () =>
          import('./pages/mesa-detalle/mesa-detalle.component').then((m) => m.MesaDetalleComponent)
      }
    ]
  },
  { path: '**', redirectTo: 'login' }
];