export type CategoriaProducto = 'COMIDA' | 'BEBIDA' | 'POSTRE';

export interface Producto {
    id: number;
    nombre: string;
    descripcion: string;
    precio: number;
    categoria: CategoriaProducto;
    disponible: boolean | null;
}