import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-mesa-form-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './mesa-form-dialog.component.html'
})
export class MesaFormDialogComponent {
  private readonly fb = inject(FormBuilder);

  readonly form = this.fb.group({
    numero: [null as number | null, [Validators.required, Validators.min(1)]],
    capacidad: [null as number | null, Validators.min(1)]
  });

  constructor(private dialogRef: MatDialogRef<MesaFormDialogComponent>) {}

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.dialogRef.close(this.form.getRawValue());
  }
}