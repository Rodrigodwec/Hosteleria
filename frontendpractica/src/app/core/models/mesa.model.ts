export type EstadoMesa = 'LIBRE' | 'OCUPADA';

export interface Mesa {
    id: number;
    numero: number;
    capacidad: number;
    estado: String;
    camareroId: number;
    camareroNombre: String;
}