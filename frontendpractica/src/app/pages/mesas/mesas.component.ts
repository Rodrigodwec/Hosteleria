import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { CdkDragEnd, DragDropModule } from '@angular/cdk/drag-drop';
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
    DragDropModule,
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

  private ultimoArrastre = 0;

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

  get esAdmin(): boolean {
    return this.authService.isAdmin();
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

  posicionX(mesa: Mesa, indice: number): number {
    return mesa.posX ?? 3 + (indice % 5) * 19;
  }

  posicionY(mesa: Mesa, indice: number): number {
    return mesa.posY ?? 4 + Math.floor(indice / 5) * 28;
  }

  onArrastreTerminado(event: CdkDragEnd, mesa: Mesa): void {
    const tarjeta = event.source.element.nativeElement;
    const plano = tarjeta.closest('.plano') as HTMLElement;
    const t = tarjeta.getBoundingClientRect();
    const p = plano.getBoundingClientRect();
    const posX = this.aPorcentaje(((t.left - p.left) / p.width) * 100);
    const posY = this.aPorcentaje(((t.top - p.top) / p.height) * 100);

    event.source.reset();
    this.ultimoArrastre = Date.now();
    this.mesas.update((lista) => lista.map((m) => (m.id === mesa.id ? { ...m, posX, posY } : m)));

    this.mesaService.actualizarPosicion(mesa.id, posX, posY).subscribe({
      error: () => {
        this.snackBar.open('No se pudo guardar la posición', 'Cerrar', { duration: 3000 });
        this.cargarMesas();
      }
    });
  }

  private aPorcentaje(valor: number): number {
    return Math.round(Math.min(100, Math.max(0, valor)) * 100) / 100;
  }

  onMesaClick(mesa: Mesa): void {
    if (Date.now() - this.ultimoArrastre < 300) {
      return;
    }
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