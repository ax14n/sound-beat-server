package com.ax14n.soundbeat.servidor.controller;

import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Clase encargada de gestionar las recepciones de los usuarios para el envío de
 * sus canciones.
 */
@RestController // Esto le indica a Spring que esta clase es un controlador.
@RequestMapping("/media") // Base URL: http://localhost:8080/media/
public final class HLSController {

	/**
	 * Objeto encargado de gestionar las consultas de la base de datos.
	 */
	private final QueriesMaker queriesMaker;

	/**
	 * Constructor del controlador de la base de datos que tiene una inyección de
	 * dependencias para QueriesMaker
	 * 
	 * @param queriesMaker Objeto encargado de gestionar las consultas de la base de
	 *                     datos.
	 */
	@Autowired
	public HLSController(QueriesMaker queriesMaker) {
		this.queriesMaker = queriesMaker;
	}

	/**
	 * Obtiene el archivo .m3u8 donde se indexan los tramos de las canciones y lo
	 * devuelve.
	 * 
	 * @param cancion Nombre del archivo que contiene el índice.
	 * @return
	 */
	@GetMapping("{cancion}.m3u8")
	public ResponseEntity<Resource> obtenerPlaylist(@PathVariable String cancion) {
		cancion = URLDecoder.decode(cancion, StandardCharsets.UTF_8);

		String sql = "SELECT * FROM songs WHERE title = ?";
		List<Map<String, Object>> resultado = queriesMaker.ejecutarConsultaSegura(sql, cancion);

		if (resultado.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		Path path = Paths.get((String) queriesMaker.ejecutarConsultaSegura(sql, cancion).getFirst().get("url"));

//		System.out.println("Intentando obtener " + path);
		return obtenerArchivo(path, "application/vnd.apple.mpegurl");
	}

	@GetMapping("{segmento}.ts")
	public ResponseEntity<Resource> obtenerSegmento(@PathVariable String segmento) {
//		System.out.println("Intentando obtener segmento: " + segmento);
		// Formo el PATH donde se encuentran los segmentos .ts de la canción

		String sql = "SELECT * FROM songs WHERE title = ?";
		List<Map<String, Object>> resultado = queriesMaker.ejecutarConsultaSegura(sql,
				segmento.substring(0, segmento.length() - 4));

		if (resultado.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		// Aquí obtengo la URL base donde se encuentran los archivos .ts
		String basePath = (String) resultado.getFirst().get("url");

		// Eliminamos el último fragmento (el nombre del archivo)
		basePath = basePath.substring(0, basePath.lastIndexOf("/"));

//		System.out.println("basePath : " + basePath);
		// Formo el path del segmento .ts
		Path path = Paths.get(basePath, segmento + ".ts");

//		System.out.println("Intentando obtener segmento: " + path);

		// Verificamos si el archivo existe
		if (!Files.exists(path)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		// Devuelvo el archivo .ts
		return obtenerArchivo(path, "video/MP2T");
	}

	/**
	 * Función genérica para leer los archivos.
	 * 
	 * @param path        Sitio donde se encuentra la canción.
	 * @param contentType Tipo del contenido que se está leyendo.
	 * @return Retorna la petición con un cuerpo y un código de éxito o no éxito.
	 */
	private ResponseEntity<Resource> obtenerArchivo(Path path, String contentType) {
		try {
			Resource recurso = new UrlResource(path.toUri());
			if (recurso.exists() || recurso.isReadable()) {
//				System.out.println("Devolviendo recurso " + path);
				return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(recurso);
			} else {
				// En caso de no existir el recurso, se devuelve NOT_FOUND al solicitante.
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
		} catch (MalformedURLException e) {
			// En caso de fallo, se retorna un INTERNAL_SERVER_ERROR al solicitante.
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}
