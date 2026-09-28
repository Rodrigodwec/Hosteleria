import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';
import { MesaService } from '../../core/services/mesa.service';
import { Mesa } from '../../core/models/mesa.model';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'app-mesas',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './mesas.component.html',
  styleUrl: './mesas.component.scss'
})
export class MesasComponent implements OnInit {
  readonly mesas = signal<Mesa[]>([]);
  readonly loading = signal(true);

  constructor(
    private mesaService: MesaService,
    private authService: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cargarMesas();
  }

  cargarMesas(): void {
    this.loading.set(true);
    this.mesaService.listar().subscribe({
      next: (mesas) => {
        this.mesas.set(mesas);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.snackBar.open('No se pudieron cargar las mesas', 'Cerrar', { duration: 3000 });
      }
    });
  }

  onMesaClick(mesa: Mesa): void {
    if (mesa.estado === 'LIBRE') {
      const ref = this.dialog.open(ConfirmDialogComponent, {
        data: {
          title: `Mesa ${mesa.numero}`,
          message: '¿Quieres empezar a comandar en esta mesa?'
        }
      });

      ref.afterClosed().subscribe((confirmado) => {
        if (confirmado) {
          this.mesaService.ocupar(mesa.id).subscribe({
            next: () => this.router.navigate(['/mesas', mesa.id]),
            error: () => this.snackBar.open('No se pudo ocupar la mesa', 'Cerrar', { duration: 3000 })
          });
        }
      });
    } else {
      this.router.navigate(['/mesas', mesa.id]);
    }
  }
}