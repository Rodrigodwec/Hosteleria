export type EstadoComanda = 'ABIERTA' | 'CERRADA';

export interface LineaComanda {
    id: number;
    productoId: number;
    productoNombre: string;
    categoria: string;
    cantidad: number;
    precioUnitario: number;
    subtotal: number;
}

export interface Comanda {
    id: number;
    mesaId: number;
    mesaNumero: number;
    camareroId: number;
    camareroNombre: string;
    estado: EstadoComanda;
    fechaApertura: string;
    lineas: LineaComanda[];
    total: number;
}