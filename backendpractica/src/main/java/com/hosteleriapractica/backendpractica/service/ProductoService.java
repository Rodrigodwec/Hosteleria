package com.hosteleriapractica.backendpractica.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hosteleriapractica.backendpractica.dto.ProductoRequest;
import com.hosteleriapractica.backendpractica.model.CategoriaProducto;
import com.hosteleriapractica.backendpractica.model.Producto;
import com.hosteleriapractica.backendpractica.repository.ProductoRepository;
import com.hosteleriapractica.backendpractica.exception.ApiException;

@Service
public class ProductoService {
	
	private final ProductoRepository productoRepository;
	
	public ProductoService(ProductoRepository productoRepository) {
	    this.productoRepository = productoRepository;
	}
	public List<Producto> listar(String categoria) {
	    if (categoria == null || categoria.isBlank()) {
	        return productoRepository.findAll();
	    }
	    CategoriaProducto cat = CategoriaProducto.valueOf(categoria.toUpperCase());
	    return productoRepository.findByCategoria(cat);
	}
	
	public Producto crear(ProductoRequest request) {
		Producto producto = Producto.builder()
				.nombre(request.nombre())
				.descripcion(request.descripcion())
				.precio(request.precio())
				.categoria(request.categoria())
				.disponible(request.disponible())
				.build();
		return productoRepository.save(producto);
	}
	
	public Producto actualizar(Long id, ProductoRequest request) {
	    Producto producto = productoRepository.findById(id)
	            .orElseThrow(() -> ApiException.notFound("Producto no encontrado"));
	    producto.setNombre(request.nombre());
	    producto.setDescripcion(request.descripcion());
	    producto.setPrecio(request.precio());
	    producto.setCategoria(request.categoria());
	    producto.setDisponible(request.disponible());
	    return productoRepository.save(producto);
	}

	public void eliminar(Long id) {
	    Producto producto = productoRepository.findById(id)
	            .orElseThrow(() -> ApiException.notFound("Producto no encontrado"));
	    productoRepository.delete(producto);
	}

}
