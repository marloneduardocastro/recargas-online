export interface Operador {
  id: number;
  nombre: string;
}

export interface Vendedor {
  id: number;
  numeroDocumento: string;
  nombre: string;
}

export interface VentaRecarga {
  operadorId: number;
  vendedorId: number;
  numeroCelular: string;
  valor: number;
}

export interface Recarga {
  id: number;
  operadorId: number;
  operador: string;
  vendedorId: number;
  vendedor: string;
  numeroCelular: string;
  valor: number;
  fechaVenta: string;
}

export interface TotalRecargas {
  id: number;
  nombre: string;
  cantidad: number;
  valorTotal: number;
}

export interface TotalOperadorVendedor {
  operador: string;
  vendedor: string;
  cantidad: number;
  valorTotal: number;
}

export interface ResumenRecargas {
  desde: string | null;
  hasta: string | null;
  cantidadTotal: number;
  valorTotal: number;
  porOperador: TotalRecargas[];
  porVendedor: TotalRecargas[];
  porOperadorYVendedor: TotalOperadorVendedor[];
}

/** Respuesta de error RFC 7807 que devuelve la API. */
export interface ProblemaApi {
  title?: string;
  detail?: string;
  errores?: Record<string, string>;
}
