export type EstadoMesa = 'LIBRE' | 'OCUPADA';

export interface Mesa {
    id: number;
    numero: number;
    capacidad: number;
    estado: EstadoMesa;
    camareroId: number | null;
    camareroNombre: string | null;
    posX: number | null;
    posY: number | null;
}

export interface CrearMesaRequest {
  numero: number;
  capacidad: number | null;
}