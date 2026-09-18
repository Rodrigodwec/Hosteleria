package com.hosteleriapractica.backendpractica.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hosteleriapractica.backendpractica.dto.ProductoRequest;
import com.hosteleriapractica.backendpractica.model.Producto;
import com.hosteleriapractica.backendpractica.repository.ProductoRepository;

@Service
public class ProductoService {
	
	private final ProductoRepository productoRepository;
	
	public ProductoService(ProductoRepository productoRepository) {
	    this.productoRepository = productoRepository;
	}
	public List<Producto> listarTodos() {
		return productoRepository.findAll();
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

}
