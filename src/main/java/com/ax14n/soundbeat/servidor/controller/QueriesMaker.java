package com.ax14n.soundbeat.servidor.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class QueriesMaker {

	/**
	 * Se encarga de gestionar las acciones con la base de datos. @Autowired inyecta
	 * las dependencias evitando que deba hacerlo manualmente.
	 */
	@Autowired
	private JdbcTemplate jdbcTemplate;

	/**
	 * Función que permite realizar consultas.
	 * 
	 * @param consultaSql Sentencia SQL de consulta.
	 * @return Resultado de la consulta.
	 */
	public List<Map<String, Object>> ejecutarConsulta(String consultaSql) {
		return this.jdbcTemplate.queryForList(consultaSql);
	}

	/**
	 * Ejecuta una consulta SQL con parámetros de forma segura, evitando inyección
	 * SQL.
	 *
	 * @param consultaSql Sentencia SQL con parámetros (ej. "SELECT * FROM songs
	 *                    WHERE ? = ANY(genres)").
	 * @param params      Parámetros que se inyectarán en la consulta de manera
	 *                    segura.
	 * @return Lista con los resultados de la consulta.
	 */
	public List<Map<String, Object>> ejecutarConsultaSegura(String consultaSql, Object... params) {
		return this.jdbcTemplate.queryForList(consultaSql, params);
	}

	/**
	 * Función que permite realizar inserciones y actualizaciones en la base de
	 * datos.
	 * 
	 * @param consultaSql Consulta de actualización o inserción.
	 * @return Resultado de la consulta.
	 */
	public int ejecutarActualizacion(String consultaSql, Object... params) {
		return this.jdbcTemplate.update(consultaSql, params);
	}

}
