import { Component } from '@angular/core';

import { VentaRecargas } from './recargas/venta-recargas';

@Component({
  selector: 'app-root',
  imports: [VentaRecargas],
  template: '<app-venta-recargas />',
})
export class App {}
