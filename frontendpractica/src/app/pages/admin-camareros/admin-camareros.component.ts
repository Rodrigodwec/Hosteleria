import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { UsuarioService } from '../../core/services/usuario.service';
import { Usuario } from '../../core/models/usuario.model';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog.component';
import { CamareroFormDialogComponent } from './camarero-form-dialog/camarero-form-dialog.component';

@Component({
  selector: 'app-admin-camareros',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDialogModule,
    MatSnackBarModule
  ],
  templateUrl: './admin-camareros.component.html',
  styleUrl: './admin-camareros.component.scss'
})
export class AdminCamarerosComponent implements OnInit {
  readonly camareros = signal<Usuario[]>([]);
  readonly columnas = ['username', 'nombre', 'activo', 'acciones'];

  constructor(
    private usuarioService: UsuarioService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.usuarioService.listarCamareros().subscribe((camareros) => this.camareros.set(camareros));
  }

  nuevo(): void {
    const ref = this.dialog.open(CamareroFormDialogComponent, { data: { camarero: null }, width: '400px' });
    ref.afterClosed().subscribe((resultado) => {
      if (!resultado) {
        return;
      }
      this.usuarioService
        .crear({ username: resultado.username, password: resultado.password, nombre: resultado.nombre })
        .subscribe({
          next: () => {
            this.snackBar.open('Camarero creado', 'Cerrar', { duration: 2500 });
            this.cargar();
          },
          error: (err) =>
            this.snackBar.open(err.error?.message || 'No se pudo crear el camarero', 'Cerrar', { duration: 3000 })
        });
    });
  }

  editar(camarero: Usuario): void {
    const ref = this.dialog.open(CamareroFormDialogComponent, { data: { camarero }, width: '400px' });
    ref.afterClosed().subscribe((resultado) => {
      if (!resultado) {
        return;
      }
      this.usuarioService
        .actualizar(camarero.id, {
          nombre: resultado.nombre,
          password: resultado.password || undefined,
          activo: resultado.activo
        })
        .subscribe({
          next: () => {
            this.snackBar.open('Camarero actualizado', 'Cerrar', { duration: 2500 });
            this.cargar();
          },
          error: () => this.snackBar.open('No se pudo actualizar el camarero', 'Cerrar', { duration: 3000 })
        });
    });
  }

  eliminar(camarero: Usuario): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Eliminar camarero',
        message: `¿Seguro que quieres eliminar a ${camarero.nombre}?`,
        confirmText: 'Sí, eliminar'
      }
    });
    ref.afterClosed().subscribe((confirmado) => {
      if (confirmado) {
        this.usuarioService.eliminar(camarero.id).subscribe({
          next: () => {
            this.snackBar.open('Camarero eliminado', 'Cerrar', { duration: 2500 });
            this.cargar();
          },
          error: () => this.snackBar.open('No se pudo eliminar el camarero', 'Cerrar', { duration: 3000 })
        });
      }
    });
  }
}