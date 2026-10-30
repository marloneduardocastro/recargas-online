import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';

import { RecargasApi } from './recargas-api';
import { Operador, ProblemaApi, Recarga, ResumenRecargas, Vendedor } from './recargas.models';

interface Mensaje {
  tipo: 'exito' | 'error';
  texto: string;
}

interface CeldaMatriz {
  cantidad: number;
  valorTotal: number;
}

const formatoPesos = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
});

@Component({
  selector: 'app-venta-recargas',
  imports: [ReactiveFormsModule, CurrencyPipe, DatePipe],
  templateUrl: './venta-recargas.html',
  styleUrl: './venta-recargas.css',
})
export class VentaRecargas implements OnInit {
  private readonly api = inject(RecargasApi);
  private readonly fb = inject(NonNullableFormBuilder);

  readonly operadores = signal<Operador[]>([]);
  readonly vendedores = signal<Vendedor[]>([]);
  readonly resumen = signal<ResumenRecargas | null>(null);
  readonly ultimas = signal<Recarga[]>([]);
  readonly enviando = signal(false);
  readonly mensaje = signal<Mensaje | null>(null);
  readonly erroresServidor = signal<Record<string, string>>({});

  readonly valoresSugeridos = [2000, 5000, 10000, 20000, 50000];

  readonly form = this.fb.group({
    vendedorId: this.fb.control<number | null>(null, Validators.required),
    operadorId: this.fb.control<number | null>(null, Validators.required),
    numeroCelular: ['', [Validators.required, Validators.pattern(/^3\d{9}$/)]],
    valor: this.fb.control<number | null>(null, [
      Validators.required,
      Validators.min(1000),
      Validators.max(500000),
    ]),
  });

  readonly filtros = this.fb.group({
    desde: [''],
    hasta: [''],
  });

  /** Tabla cruzada vendedor (filas) x operador (columnas). */
  readonly matriz = computed(() => {
    const resumen = this.resumen();
    if (!resumen) {
      return null;
    }
    const operadores = resumen.porOperador.map((o) => o.nombre);
    const filas = resumen.porVendedor.map((v) => ({
      vendedor: v.nombre,
      celdas: operadores.map((operador): CeldaMatriz => {
        const total = resumen.porOperadorYVendedor.find(
          (t) => t.operador === operador && t.vendedor === v.nombre,
        );
        return { cantidad: total?.cantidad ?? 0, valorTotal: total?.valorTotal ?? 0 };
      }),
      cantidad: v.cantidad,
      valorTotal: v.valorTotal,
    }));
    return { operadores, filas, totalesOperador: resumen.porOperador };
  });

  ngOnInit(): void {
    this.api.operadores().subscribe((operadores) => this.operadores.set(operadores));
    this.api.vendedores().subscribe((vendedores) => this.vendedores.set(vendedores));
    this.cargarResumen();
    this.cargarUltimas();
  }

  seleccionarValor(valor: number): void {
    this.form.controls.valor.setValue(valor);
    this.form.controls.valor.markAsTouched();
  }

  vender(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { vendedorId, operadorId, numeroCelular, valor } = this.form.getRawValue();

    this.enviando.set(true);
    this.mensaje.set(null);
    this.erroresServidor.set({});

    this.api
      .vender({ vendedorId: vendedorId!, operadorId: operadorId!, numeroCelular, valor: valor! })
      .pipe(finalize(() => this.enviando.set(false)))
      .subscribe({
        next: (recarga) => {
          this.mensaje.set({
            tipo: 'exito',
            texto: `Recarga #${recarga.id} registrada: ${formatoPesos.format(recarga.valor)} ` +
              `${recarga.operador} al ${recarga.numeroCelular}.`,
          });
          // Se conserva el vendedor y el operador para agilizar la siguiente venta
          this.form.controls.numeroCelular.reset();
          this.form.controls.valor.reset();
          this.cargarResumen();
          this.cargarUltimas();
        },
        error: (error: HttpErrorResponse) => {
          const problema = (error.error ?? {}) as ProblemaApi;
          this.erroresServidor.set(problema.errores ?? {});
          this.mensaje.set({
            tipo: 'error',
            texto: problema.detail ?? 'No fue posible registrar la recarga. Intente de nuevo.',
          });
        },
      });
  }

  cargarResumen(): void {
    const { desde, hasta } = this.filtros.getRawValue();
    this.api.resumen(desde || undefined, hasta || undefined).subscribe({
      next: (resumen) => this.resumen.set(resumen),
      error: (error: HttpErrorResponse) =>
        this.mensaje.set({
          tipo: 'error',
          texto: (error.error as ProblemaApi | null)?.detail ?? 'No fue posible consultar el resumen.',
        }),
    });
  }

  limpiarFiltros(): void {
    this.filtros.reset();
    this.cargarResumen();
  }

  cargarUltimas(): void {
    this.api.ultimas(10).subscribe((recargas) => this.ultimas.set(recargas));
  }

  /** Mensaje de error de un campo: primero el del servidor, luego el de la validación local. */
  error(campo: 'vendedorId' | 'operadorId' | 'numeroCelular' | 'valor'): string | null {
    const servidor = this.erroresServidor()[campo];
    if (servidor) {
      return servidor;
    }
    const control = this.form.controls[campo];
    if (!control.touched || !control.errors) {
      return null;
    }
    if (control.errors['required']) {
      return 'Campo obligatorio';
    }
    if (control.errors['pattern']) {
      return 'Debe tener 10 dígitos y empezar por 3';
    }
    if (control.errors['min']) {
      return 'El valor mínimo es $1.000';
    }
    if (control.errors['max']) {
      return 'El valor máximo es $500.000';
    }
    return 'Valor inválido';
  }
}
