package com.mx.cbtis.gestor.modelo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Usuario del sistema.
 * La contrasena se almacena como hash BCrypt y es WRITE_ONLY:
 * se puede recibir en el JSON de entrada, pero nunca se devuelve en las respuestas.
 */
@Entity
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id_usuario")
	@JsonProperty("id_usuario")
	private Integer idUsuario;

	private String nombre;

	@Column(name = "correo_electronico")
	@JsonProperty("correo_electronico")
	private String correoElectronico;

	@JsonProperty(value = "contrasena", access = JsonProperty.Access.WRITE_ONLY)
	private String contrasena;

	private String rol;

	@Column(name = "imagen_usuario")
	@JsonProperty("imagen_usuario")
	private String imagenUsuario;

	public Usuario() {
		super();
	}

	public Usuario(Integer idUsuario, String nombre, String correoElectronico, String contrasena, String rol,
			String imagenUsuario) {
		super();
		this.idUsuario = idUsuario;
		this.nombre = nombre;
		this.correoElectronico = correoElectronico;
		this.contrasena = contrasena;
		this.rol = rol;
		this.imagenUsuario = imagenUsuario;
	}

	public Integer getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getCorreoElectronico() {
		return correoElectronico;
	}

	public void setCorreoElectronico(String correoElectronico) {
		this.correoElectronico = correoElectronico;
	}

	public String getContrasena() {
		return contrasena;
	}

	public void setContrasena(String contrasena) {
		this.contrasena = contrasena;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}

	public String getImagenUsuario() {
		return imagenUsuario;
	}

	public void setImagenUsuario(String imagenUsuario) {
		this.imagenUsuario = imagenUsuario;
	}
}
