import { CommonModule } from '@angular/common';
import { Component, Inject, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { CategoriaProducto, Producto } from '../../../core/models/producto.model';

export interface ProductoFormDialogData {
  producto: Producto | null;
}

@Component({
  selector: 'app-producto-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatCheckboxModule
  ],
  templateUrl: './producto-form-dialog.component.html'
})
export class ProductoFormDialogComponent {
  readonly esEdicion: boolean;
  readonly categorias: CategoriaProducto[] = ['COMIDA', 'BEBIDA', 'POSTRE'];

  private readonly fb = inject(FormBuilder);

  readonly form = this.fb.group({
    nombre: ['', Validators.required],
    descripcion: [''],
    precio: [0, [Validators.required, Validators.min(0)]],
    categoria: ['COMIDA' as CategoriaProducto, Validators.required],
    disponible: [true]
  });

  constructor(
    private dialogRef: MatDialogRef<ProductoFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ProductoFormDialogData
  ) {
    this.esEdicion = data.producto !== null;
    if (data.producto) {
      this.form.patchValue(data.producto);
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.dialogRef.close(this.form.getRawValue());
  }
}