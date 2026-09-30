import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { AuthService } from '../../core/services/auth.service';
import { ComandaService } from '../../core/services/comanda.service';
import { MesaService } from '../../core/services/mesa.service';
import { ProductoService } from '../../core/services/producto.service';
import { Comanda, LineaComanda } from '../../core/models/comanda.model';
import { CategoriaProducto, Producto } from '../../core/models/producto.model';
import { Mesa } from '../../core/models/mesa.model';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog.component';

const CATEGORIAS: { valor: CategoriaProducto; etiqueta: string }[] = [
  { valor: 'COMIDA', etiqueta: 'Comida' },
  { valor: 'BEBIDA', etiqueta: 'Bebida' },
  { valor: 'POSTRE', etiqueta: 'Postre' }
];

@Component({
  selector: 'app-mesa-detalle',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatTabsModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './mesa-detalle.component.html',
  styleUrl: './mesa-detalle.component.scss'
})
export class MesaDetalleComponent implements OnInit {
  readonly categorias = CATEGORIAS;
  readonly mesa = signal<Mesa | null>(null);
  readonly comanda = signal<Comanda | null>(null);
  readonly productos = signal<Producto[]>([]);
  readonly loading = signal(true);
  readonly lineaSeleccionada = signal<LineaComanda | null>(null);

  private mesaId!: number;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private mesaService: MesaService,
    private comandaService: ComandaService,
    private productoService: ProductoService,
    private authService: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.mesaId = Number(this.route.snapshot.paramMap.get('id'));
    this.cargarTodo();
    this.cargarProductos('COMIDA');
  }

  get puedeEditar(): boolean {
    const comanda = this.comanda();
    const usuario = this.authService.usuario();
    if (!comanda || !usuario) {
      return false;
    }
    return comanda.camareroId === usuario.id || this.authService.isAdmin();
  }

  cargarTodo(): void {
    this.loading.set(true);
    this.comandaService.obtenerPorMesa(this.mesaId).subscribe({
      next: (comanda) => {
        this.comanda.set(comanda);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.snackBar.open('No se pudo cargar la comanda', 'Cerrar', { duration: 3000 });
      }
    });
  }

  cargarProductos(categoria: CategoriaProducto): void {
    this.productoService.listar(categoria).subscribe((productos) => this.productos.set(productos));
  }

  onTabChange(index: number): void {
    this.cargarProductos(this.categorias[index].valor);
  }

  agregarProducto(producto: Producto): void {
    if (!this.puedeEditar || !this.comanda()) {
      return;
    }
    this.comandaService.agregarLinea(this.comanda()!.id, producto.id, 1).subscribe({
      next: (comanda) => this.comanda.set(comanda),
      error: () => this.snackBar.open('No se pudo añadir el producto', 'Cerrar', { duration: 3000 })
    });
  }

  abrirOpciones(linea: LineaComanda): void {
    if (!this.puedeEditar) {
      return;
    }
    this.lineaSeleccionada.set(linea);
  }

  sumarUnidad(): void {
    const linea = this.lineaSeleccionada();
    if (!linea || !this.comanda()) {
      return;
    }
    this.comandaService.actualizarCantidad(this.comanda()!.id, linea.id, linea.cantidad + 1).subscribe({
      next: (comanda) => this.comanda.set(comanda)
    });
  }

  restarUnidad(): void {
    const linea = this.lineaSeleccionada();
    if (!linea || !this.comanda()) {
      return;
    }
    if (linea.cantidad <= 1) {
      this.eliminarLinea();
      return;
    }
    this.comandaService.actualizarCantidad(this.comanda()!.id, linea.id, linea.cantidad - 1).subscribe({
      next: (comanda) => this.comanda.set(comanda)
    });
  }

  eliminarLinea(): void {
    const linea = this.lineaSeleccionada();
    if (!linea || !this.comanda()) {
      return;
    }
    this.comandaService.eliminarLinea(this.comanda()!.id, linea.id).subscribe({
      next: (comanda) => this.comanda.set(comanda)
    });
  }

  eliminarComanda(): void {
    const comanda = this.comanda();
    if (!comanda) {
      return;
    }
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Eliminar comanda',
        message: '¿Seguro que quieres borrar la comanda completa? La mesa quedará libre.',
        confirmText: 'Sí, borrar'
      }
    });
    ref.afterClosed().subscribe((confirmado) => {
      if (confirmado) {
        this.comandaService.eliminarComanda(comanda.id).subscribe({
          next: () => this.router.navigate(['/mesas']),
          error: () => this.snackBar.open('No se pudo eliminar la comanda', 'Cerrar', { duration: 3000 })
        });
      }
    });
  }

  cobrarMesa(): void {
    const comanda = this.comanda();
    if (!comanda) {
      return;
    }
    const ref = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Cobrar mesa',
        message: '¿Confirmas que quieres cobrar y cerrar esta mesa?',
        confirmText: 'Sí, cobrar'
      }
    });
    ref.afterClosed().subscribe((confirmado) => {
      if (confirmado) {
        this.comandaService.cobrar(comanda.id).subscribe({
          next: () => this.router.navigate(['/mesas']),
          error: () => this.snackBar.open('No se pudo cobrar la mesa', 'Cerrar', { duration: 3000 })
        });
      }
    });
  }

  volver(): void {
    this.router.navigate(['/mesas']);
  }
}