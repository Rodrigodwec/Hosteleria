export type EstadoMesa = 'LIBRE' | 'OCUPADA';

export interface Mesa {
    id: number;
    numero: number;
    capacidad: number;
    estado: EstadoMesa;
    camareroId: number | null;
    camareroNombre: string | null;
}