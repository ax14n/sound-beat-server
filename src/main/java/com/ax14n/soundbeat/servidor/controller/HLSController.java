package com.ax14n.soundbeat.servidor.controller;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

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
	 * Ruta del directorio donde se encuentran los indices de las canciones a
	 * reproducir
	 */
	private final String RUTA_CANCIONES = "/home/me/Documentos/canciones-hls/";

	/**
	 * Obtiene el archivo .m3u8 donde se indexan los tramos de las canciones y lo
	 * devuelve.
	 * 
	 * @param nombrePlaylist Nombre del archivo que contiene el índice.
	 * @return
	 */
	@GetMapping("/{nombrePlaylist}.m3u8")
	public ResponseEntity<Resource> obtenerPlaylist(@PathVariable String nombrePlaylist) {
		// Formo el PATH donde se encuentra el índice de la canción.
		Path path = Paths.get(RUTA_CANCIONES + nombrePlaylist + ".m3u8");
		// Devuelvo el archivo ubicado en dicha ruta.
		System.out.println("Intentando obtener "+path);
		return obtenerArchivo(path, "application/vnd.apple.mpegurl");
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
