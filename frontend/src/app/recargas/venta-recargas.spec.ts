import { registerLocaleData } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import localeEsCo from '@angular/common/locales/es-CO';
import { LOCALE_ID } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Recarga, ResumenRecargas } from './recargas.models';
import { VentaRecargas } from './venta-recargas';

registerLocaleData(localeEsCo);

const OPERADORES = [
  { id: 1, nombre: 'Comcel' },
  { id: 2, nombre: 'Movistar' },
  { id: 3, nombre: 'Tigo' },
  { id: 4, nombre: 'Uff' },
];
const VENDEDORES = [
  { id: 1, numeroDocumento: '1010101010', nombre: 'Ana Gómez' },
  { id: 2, numeroDocumento: '2020202020', nombre: 'Carlos Pérez' },
];

function resumen(cantidadTigo: number, valorTigo: number): ResumenRecargas {
  return {
    desde: null,
    hasta: null,
    cantidadTotal: cantidadTigo,
    valorTotal: valorTigo,
    porOperador: OPERADORES.map((o) => ({
      id: o.id,
      nombre: o.nombre,
      cantidad: o.nombre === 'Tigo' ? cantidadTigo : 0,
      valorTotal: o.nombre === 'Tigo' ? valorTigo : 0,
    })),
    porVendedor: VENDEDORES.map((v) => ({
      id: v.id,
      nombre: v.nombre,
      cantidad: v.id === 1 ? cantidadTigo : 0,
      valorTotal: v.id === 1 ? valorTigo : 0,
    })),
    porOperadorYVendedor: [
      { operador: 'Tigo', vendedor: 'Ana Gómez', cantidad: cantidadTigo, valorTotal: valorTigo },
    ],
  };
}

const RECARGA: Recarga = {
  id: 10,
  operadorId: 3,
  operador: 'Tigo',
  vendedorId: 1,
  vendedor: 'Ana Gómez',
  numeroCelular: '3001234567',
  valor: 5000,
  fechaVenta: '2026-10-28T20:00:00',
};

describe('VentaRecargas', () => {
  let fixture: ComponentFixture<VentaRecargas>;
  let http: HttpTestingController;
  let el: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VentaRecargas],
      providers: [provideHttpClient(), provideHttpClientTesting(), { provide: LOCALE_ID, useValue: 'es-CO' }],
    }).compileComponents();

    fixture = TestBed.createComponent(VentaRecargas);
    http = TestBed.inject(HttpTestingController);
    el = fixture.nativeElement as HTMLElement;

    fixture.detectChanges(); // ngOnInit
    http.expectOne('/api/operadores').flush(OPERADORES);
    http.expectOne('/api/vendedores').flush(VENDEDORES);
    http.expectOne((r) => r.url === '/api/recargas/resumen').flush(resumen(1, 10000));
    http.expectOne((r) => r.url === '/api/recargas').flush([]);
    await fixture.whenStable();
  });

  afterEach(() => http.verify());

  it('muestra el resumen por operador, por vendedor y la tabla cruzada', () => {
    const porOperador = el.querySelectorAll('[data-testid="tabla-operador"] tbody tr');
    expect(porOperador.length).toBe(4);
    expect(porOperador[2].textContent).toContain('Tigo');
    expect(porOperador[2].textContent).toContain('10.000');

    const porVendedor = el.querySelectorAll('[data-testid="tabla-vendedor"] tbody tr');
    expect(porVendedor.length).toBe(2);

    const cruzada = el.querySelector('[data-testid="tabla-cruzada"]')!;
    expect(cruzada.querySelectorAll('thead th').length).toBe(6); // vendedor + 4 operadores + total
    expect(el.querySelector('[data-testid="total-general"]')!.textContent).toContain('1');
  });

  it('no envía la venta si el formulario es inválido y muestra los errores', async () => {
    (el.querySelector('[data-testid="vender"]') as HTMLButtonElement).click();
    await fixture.whenStable();

    http.expectNone({ method: 'POST', url: '/api/recargas' });
    expect(el.querySelectorAll('.error-campo').length).toBe(4);
  });

  it('envía la venta, conserva vendedor y operador, y refresca el resumen', async () => {
    const componente = fixture.componentInstance;
    componente.form.setValue({ vendedorId: 1, operadorId: 3, numeroCelular: '3001234567', valor: 5000 });

    (el.querySelector('[data-testid="vender"]') as HTMLButtonElement).click();

    const venta = http.expectOne({ method: 'POST', url: '/api/recargas' });
    expect(venta.request.body).toEqual({ vendedorId: 1, operadorId: 3, numeroCelular: '3001234567', valor: 5000 });
    venta.flush(RECARGA, { status: 201, statusText: 'Created' });

    http.expectOne((r) => r.url === '/api/recargas/resumen').flush(resumen(2, 15000));
    http.expectOne((r) => r.url === '/api/recargas').flush([RECARGA]);
    await fixture.whenStable();

    expect(el.textContent).toContain('Recarga #10 registrada');
    expect(componente.form.controls.vendedorId.value).toBe(1);
    expect(componente.form.controls.operadorId.value).toBe(3);
    expect(componente.form.controls.numeroCelular.value).toBe('');
    expect(el.querySelector('[data-testid="total-general"]')!.textContent).toContain('15.000');
    expect(el.querySelector('[data-testid="tabla-ultimas"]')!.textContent).toContain('3001234567');
  });

  it('muestra los errores de validación que devuelve la API', async () => {
    const componente = fixture.componentInstance;
    componente.form.setValue({ vendedorId: 1, operadorId: 3, numeroCelular: '3001234567', valor: 5000 });
    (el.querySelector('[data-testid="vender"]') as HTMLButtonElement).click();

    http.expectOne({ method: 'POST', url: '/api/recargas' }).flush(
      {
        title: 'Operación no permitida',
        detail: 'El operador Tigo no está habilitado para la venta',
      },
      { status: 422, statusText: 'Unprocessable Entity' },
    );
    await fixture.whenStable();

    expect(el.querySelector('.mensaje.error')!.textContent).toContain('no está habilitado');
  });
});
