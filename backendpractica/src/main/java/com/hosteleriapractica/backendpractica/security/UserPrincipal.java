package com.hosteleriapractica.backendpractica.security;

import com.hosteleriapractica.backendpractica.model.Usuario;

import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {

    private final Usuario usuario;

    public UserPrincipal(Usuario usuario) {
        this.usuario = usuario;
    }

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
	}

	@Override
	public @Nullable String getPassword() {
		return usuario.getPassword();
	}

	@Override
	public String getUsername() {
		return usuario.getUsername();
	}
	
	@Override
	public boolean isEnabled() {
	    return usuario.isActivo();
	}
	
	public Long getId() {
	    return usuario.getId();
	}

	public Usuario getUsuario() {
	    return usuario;
	}

}