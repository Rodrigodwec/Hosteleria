import { CommonModule } from '@angular/common';
import { Component, Inject, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Usuario } from '../../../core/models/usuario.model';

export interface CamareroFormDialogData {
  camarero: Usuario | null;
}

@Component({
  selector: 'app-camarero-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCheckboxModule
  ],
  templateUrl: './camarero-form-dialog.component.html'
})
export class CamareroFormDialogComponent {
  readonly esEdicion: boolean;

  private readonly fb = inject(FormBuilder);

  readonly form = this.fb.group({
    username: [{ value: '', disabled: false }, Validators.required],
    nombre: ['', Validators.required],
    password: [''],
    activo: [true]
  });

  constructor(
    private dialogRef: MatDialogRef<CamareroFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: CamareroFormDialogData
  ) {
    this.esEdicion = data.camarero !== null;

    if (data.camarero) {
      this.form.patchValue({
        username: data.camarero.username,
        nombre: data.camarero.nombre,
        activo: data.camarero.activo
      });
      this.form.controls.username.disable();
    } else {
      this.form.controls.password.addValidators([Validators.required, Validators.minLength(4)]);
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