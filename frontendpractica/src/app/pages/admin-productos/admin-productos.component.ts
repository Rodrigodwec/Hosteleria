import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { ProductoService } from '../../core/services/producto.service';
import { Producto } from '../../core/models/producto.model';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog.component';
import { ProductoFormDialogComponent } from './producto-form-dialog/producto-form-dialog.component';

@Component({
  selector: 'app-admin-productos',
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
  templateUrl: './admin-productos.component.html',
  styleUrl: './admin-productos.component.scss'
})
export class AdminProductosComponent implements OnInit {
  readonly productos = signal<Producto[]>([]);
  readonly columnas = ['nombre', 'categoria', 'precio', 'disponible', 'acciones'];

  constructor(
    private productoService: ProductoService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.productoService.listar().subscribe((productos) => this.productos.set(productos));
  }

  nuevo(): void {
    const ref = this.dialog.open(ProductoFormDialogComponent, { data: { producto: null }, width: '400px' });
    ref.afterClosed().subscribe((resultado) => {
      if (!resultado) {
        return;
      }
      this.productoService.crear(resultado).subscribe({
        next: () => {
          this.snackBar.open('Producto creado', 'Cerrar', { duration: 2500 });
          this.cargar();
        },
        error: () => this.snackBar.open('No se pudo crear el producto', 'Cerrar', { duration: 3000 })
      });
    });
  }

  editar(producto: Producto): void {
    const ref = this.dialog.open(ProductoFormDialogComponent, { data: { producto }, width: '400px' });
    ref.afterClosed().subscribe((resultado) => {
      if (!resultado) {
        return;
      }
      this.productoService.actualizar(producto.id, resultado).subscribe({
        next: () => {
          this.snackBar.open('Producto actualizado', 'Cerrar', { duration: 2500 });
          this.cargar();
        },
        error: () => this.snackBar.open('No se pudo actualizar el producto', 'Cerrar', { duration: 3000 })
      });
    });
  }

  eliminar(producto: Producto): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Eliminar producto',
        message: `¿Seguro que quieres eliminar "${producto.nombre}"?`,
        confirmText: 'Sí, eliminar'
      }
    });
    ref.afterClosed().subscribe((confirmado) => {
      if (confirmado) {
        this.productoService.eliminar(producto.id).subscribe({
          next: () => {
            this.snackBar.open('Producto eliminado', 'Cerrar', { duration: 2500 });
            this.cargar();
          },
          error: (err) => this.snackBar.open(err.error?.message || 'No se pudo eliminar el producto', 'Cerrar', { duration: 3000 })
        });
      }
    });
  }
}