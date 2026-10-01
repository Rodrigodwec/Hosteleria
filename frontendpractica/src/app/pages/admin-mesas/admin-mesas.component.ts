import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MesaService } from '../../core/services/mesa.service';
import { Mesa } from '../../core/models/mesa.model';
import { MesaFormDialogComponent } from './mesa-form-dialog/mesa-form-dialog.component';

@Component({
  selector: 'app-admin-mesas',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatButtonModule, MatIconModule, MatChipsModule, MatDialogModule, MatSnackBarModule],
  templateUrl: './admin-mesas.component.html',
  styleUrl: './admin-mesas.component.scss'
})
export class AdminMesasComponent implements OnInit {
  readonly mesas = signal<Mesa[]>([]);
  readonly columnas = ['numero', 'capacidad', 'estado', 'camarero'];

  constructor(
    private mesaService: MesaService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.mesaService.listar().subscribe((mesas) => this.mesas.set(mesas));
  }

  nueva(): void {
    const ref = this.dialog.open(MesaFormDialogComponent, { width: '400px' });
    ref.afterClosed().subscribe((resultado) => {
      if (!resultado) {
        return;
      }
      this.mesaService.crear(resultado).subscribe({
        next: () => {
          this.snackBar.open('Mesa creada', 'Cerrar', { duration: 2500 });
          this.cargar();
        },
        error: (err) => this.snackBar.open(err.error?.message || 'No se pudo crear la mesa', 'Cerrar', { duration: 3000 })
      });
    });
  }
}