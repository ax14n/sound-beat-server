package com.ax14n.soundbeat.servidor.controller;

import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.jdbc.core.JdbcTemplate;
import com.ax14n.soundbeat.servidor.dto.SongDTO;
import com.ax14n.soundbeat.servidor.dto.UserDTO;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.*;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Clase que hice con el único propósito de recibir peticiones POST para luego
 * subirlas a la base de datos. La ruta para acceder y envíar peticiones POST a
 * este endpoint es localhost:8080/api/<servicio>
 */
@RestController
@RequestMapping("/api")
public class DbController {

	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	/**
	 * Objeto encargado de gestionar las consultas de la base de datos.
	 */
	private final QueriesMaker queriesMaker;

	/**
	 * Objeto encargado de cifrar y descifrar contraseñas.
	 */
	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	/**
	 * Constructor del controlador de la base de datos que tiene una inyección de
	 * dependencias para QueriesMaker
	 * 
	 * @param queriesMaker Objeto encargado de gestionar las consultas de la base de
	 *                     datos.
	 */
	@Autowired
	public DbController(QueriesMaker queriesMaker, JdbcTemplate jdbcTemplate) {
		this.queriesMaker = queriesMaker;
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * Recibe información desde HTTP mediante peticion POST.
	 * 
	 * curl -X POST http://localhost:8080/import \ -H "Content-Type:
	 * application/json" \ -d '{ "titulo": "Bohemian Rhapsody", "autor": "Queen",
	 * "duracion": 5.55, "url": "https://example.com/bohemian-rhapsody.mp3" }'
	 * 
	 * @param songData Información recibida a través de la petición POST.
	 * @return Éxito de la petición o fallo de la misma.
	 */
	@PostMapping("/import")
	public String importSongs(@RequestBody Map<String, Object> songData) {

		System.out.println("Datos recibidos: " + songData);

		// --- { Extracción de los parámetros recibidos } --- //
		String songName = (String) songData.get("titulo");
		String artist = (String) songData.get("autor");
		double duration = (double) songData.get("duracion");
		String url = (String) songData.get("url");

		// --- { Creación de la consulta de inserción } --- //
		String sql = "INSERT INTO songs (title, artist, duration, url) VALUES (?, ?, ?, ?)";

		// --- { Ejecución de la consulta } --- //
		int rowsAffected = queriesMaker.ejecutarActualizacion(sql, songName, artist, duration, url);

		// --- { Envío del resultado de la consulta a CURL } --- //
		if (rowsAffected > 0) {
			return "Canción importada exitosamente!";
		} else {
			return "Error al importar la canción.";
		}

	}

	/**
	 * Devuelve una lista de playlists asociadas a un usuario identificado por su
	 * email.
	 * 
	 * Este método recibe una petición GET con un parámetro `email` y realiza una
	 * consulta a la base de datos para obtener todas las playlists cuyo `user_id`
	 * coincida con el del usuario correspondiente al email proporcionado.
	 *
	 * @param email Dirección de correo electrónico del usuario.
	 * @return Lista de mapas con los datos de cada playlist, o null si el email es
	 *         nulo.
	 */
	@GetMapping("/userPlaylists")
	public List<Map<String, Object>> userPlaylists(@RequestParam String email) {
		if (email == null) {
			return null;
		}
		String sql = "SELECT * FROM PLAYLISTS WHERE user_id = (SELECT user_id FROM users u where email like ?);";
		return queriesMaker.ejecutarConsultaSegura(sql, email);
	}

	private final JdbcTemplate jdbcTemplate;

	/**
	 * Devuelve una lista de canciones que pertenecen a una playlist específica.
	 *
	 * Este método maneja solicitudes HTTP GET y espera recibir un parámetro
	 * `playlistId`. Realiza una consulta a la base de datos para recuperar todas
	 * las canciones asociadas a la playlist con el ID proporcionado. Si el ID es
	 * menor que 0, devuelve `null`.
	 *
	 * @param playlistId ID de la playlist cuyas canciones se desean recuperar.
	 * @return Una lista de mapas que representan las canciones de la playlist, o
	 *         `null` si el ID de la playlist no es válido.
	 */
	@SuppressWarnings("deprecation")
	@GetMapping("/getPlaylistSongs")
	public List<SongDTO> getPlaylistSongs(@RequestParam int playlistId) {
		if (playlistId < 0)
			return null;

		String sql = "SELECT * FROM SONGS WHERE song_id IN (SELECT song_id FROM PLAYLIST_SONGS WHERE playlist_id = ?);";

		return jdbcTemplate.query(sql, new Object[] { playlistId }, (rs, rowNum) -> {
			SongDTO song = new SongDTO();
			song.setSongId(rs.getInt("song_id"));
			song.setTitle(rs.getString("title"));
			song.setArtist(rs.getString("artist"));
			song.setUrl(rs.getString("url"));
			song.setDuration(rs.getInt("duration"));

			Array genresArray = rs.getArray("genres");
			if (genresArray != null) {
				String[] genres = (String[]) genresArray.getArray();
				song.setGenres(Arrays.asList(genres));
			}

			return song;
		});
	}

	/**
	 * Solicita la información de las canciones almacenadas en el servidor. Puede
	 * especificarse un género por parámetros a través de la petición POST para
	 * hacer una búsqueda más específica.
	 * 
	 * Para consulta general: curl "http://localhost:8080/api/songs"
	 * 
	 * Para búscar con género: curl "http://localhost:8080/api/songs?genre=rock"
	 * 
	 * @param genre Género de la canción.
	 * @return Canciones almacenadas en el servidor, por género o en general.
	 */
	@GetMapping("/songs")
	public List<SongDTO> getSongsByGenre(String genre) {
		String sql = (genre == null || "null".equalsIgnoreCase(genre.trim())) ? "SELECT * FROM songs"
				: "SELECT * FROM songs WHERE genres IS NOT NULL AND ? = ANY(genres)";

		List<Map<String, Object>> rawResults = (genre == null || "null".equalsIgnoreCase(genre.trim()))
				? queriesMaker.ejecutarConsulta(sql)
				: queriesMaker.ejecutarConsultaSegura(sql, genre);

		List<SongDTO> songs = new ArrayList<>();
		for (Map<String, Object> row : rawResults) {
			SongDTO dto = new SongDTO();

			dto.setSongId((Integer) row.get("song_id"));
			dto.setTitle((String) row.get("title"));
			dto.setArtist((String) row.get("author"));
			dto.setUrl((String) row.get("url"));
			dto.setDuration((Integer) row.get("duration"));

			Object pgArray = row.get("genres");
			if (pgArray instanceof java.sql.Array array) {
				try {
					Object[] elements = (Object[]) array.getArray();
					dto.setGenres(Arrays.stream(elements).map(Object::toString).toList());
				} catch (Exception e) {
					dto.setGenres(List.of("OTHER"));
				}
			} else {
				dto.setGenres(List.of("OTHER"));
			}

			songs.add(dto);
		}
		return songs;
	}

	/**
	 * Registra un nuevo usuario con contraseña cifrada.
	 *
	 * curl -X POST http://localhost:8080/register \ -H "Content-Type:
	 * application/json" \ -d '{ "email": "usuario@example.com", "password":
	 * "contraseña123" }'
	 *
	 * 
	 * @param userData Datos del usuario recibidos en la petición POST.
	 * @return Mensaje de éxito o error.
	 */
	@PostMapping("/register")
	public String registerUser(@RequestBody Map<String, Object> userData) {
		System.out.println("Datos recibidos: " + userData);

		// --- { Extracción de parámetros } --- //
		String email = (String) userData.get("email");
		String rawPassword = (String) userData.get("password");

		// --- { Validación de datos } --- //
		if (email == null || rawPassword == null) {
			return "Error: email y password son obligatorios.";
		}

		// --- { Cifrar la contraseña antes de guardarla } --- //
		String hashedPassword = passwordEncoder.encode(rawPassword);

		// --- { Creación de la consulta de inserción } --- //
		String sql = "INSERT INTO users (email, password) VALUES (?, ?)";

		// --- { Ejecución de la consulta } --- //
		int rowsAffected = queriesMaker.ejecutarActualizacion(sql, email, hashedPassword);

		// --- { Envío del resultado } --- //
		return (rowsAffected > 0) ? "Usuario registrado exitosamente!" : "Error al registrar el usuario.";
	}

	/**
	 * Verifica si un usuario ya existe en la base de datos.
	 * 
	 * curl "http://localhost:8080/api/userExists?email=usuario@example.com"
	 * 
	 * @param email Correo electrónico del usuario a verificar.
	 * @return true si el usuario existe, false en caso contrario.
	 */
	@GetMapping("/userExists")
	public boolean userExists(@RequestParam String email) {
		System.out.println("Verificando existencia del usuario con email: " + email);

		// --- { Validación de datos } --- //
		if (email == null || email.isEmpty()) {
			throw new IllegalArgumentException("Error: el email es obligatorio.");
		}
		String sql = "SELECT EXISTS (SELECT 1 FROM users WHERE email = ?) AS existe";
		List<Map<String, Object>> result = queriesMaker.ejecutarConsultaSegura(sql, email);

		boolean exists = false;
		if (!result.isEmpty()) {
			exists = Boolean.TRUE.equals(result.get(0).get("existe"));
		}

		System.out.println("Resultado = " + exists);
		return exists;

	}

	/**
	 * Solicita la información mínima del usuario para completar el perfil.
	 * 
	 * @param email Correo electrónico del usuario con el que se rellanará el
	 *              perfil.
	 * @return Mínima información necesaria del usuario para completar el perfil.
	 */
	@GetMapping("/userInfo")
	public UserDTO obtenerInfoUsuario(@RequestParam String email) {
		System.out.println("obteniendo información del usuario con email: " + email);

		if (email == null || email.isEmpty()) {
			throw new IllegalArgumentException("error: el email es obligatorio.");
		}

		String sql = "SELECT username, date_joined FROM users WHERE email = ?";
		List<Map<String, Object>> resultado = queriesMaker.ejecutarConsultaSegura(sql, email);

		if (resultado.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "usuario no encontrado.");
		}

		Map<String, Object> row = resultado.get(0);

		UserDTO dto = new UserDTO();
		dto.setUsername((String) row.get("username"));
		dto.setDateJoined(((java.sql.Timestamp) row.get("date_joined")).toLocalDateTime().toLocalDate());

		return dto;
	}

	/**
	 * Inicia sesión verificando la contraseña cifrada.
	 *
	 * curl -X POST http://localhost:8080/api/login \ -H "Content-Type:
	 * application/json" \ -d '{ "email": "usuario@example.com", "password":
	 * "contraseña123" }'
	 *
	 * @param userData Datos del usuario recibidos en la petición POST.
	 * @return Mensaje de éxito o error.
	 */
	@PostMapping("/login")
	public String loginUser(@RequestBody Map<String, Object> userData) {
		System.out.println("Intento de inicio de sesión: " + userData);

		// --- { Extracción de parámetros } --- //
		String email = (String) userData.get("email");
		String rawPassword = (String) userData.get("password");

		// --- { Validación de datos } --- //
		if (email == null || rawPassword == null) {
			return "Error: email y password son obligatorios.";
		}

		// --- { Creación de la consulta de selección } --- //
		String sql = "SELECT password FROM users WHERE email = ?";

		// --- { Ejecución de la consulta } --- //
		List<Map<String, Object>> results = queriesMaker.ejecutarConsultaSegura(sql, email);

		if (results.isEmpty()) {
			return "Error: usuario no encontrado.";
		}

		String storedHashedPassword = (String) results.get(0).get("password");

		// --- { Verificación de la contraseña } --- //
		if (passwordEncoder.matches(rawPassword, storedHashedPassword)) {
			return "Inicio de sesión exitoso!\n";
		} else {
			return "Error: contraseña incorrecta.\n";
		}
	}

	/**
	 * Crea una playlist. Espera que la petición POST llegue con datos compuesto por
	 * el nombre de la playlist a crear y el email del usuario a quién pertenecerá.
	 * 
	 * Petición POST de ejemplo: curl -X POST
	 * http://localhost:8080/api/createPlaylist \ -u "admin:admin" \ -H
	 * "Content-Type: application/json" \ -d "{ \"name\": \"THC\", \"email\":
	 * \"zelmar@gmail.com\"}"
	 * 
	 * 
	 * @param playlistData Datos enviados a través de la petición POST. Espera que
	 *                     contenga datos compuesto por el nombre de la playlist y
	 *                     el usuario a quien pertenecerá.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@PostMapping("/playlists/createPlaylist")
	public String createPlaylist(@RequestBody Map<String, Object> playlistData) {
		System.out.println("[DB] Datos recibidos para crear playlist: " + playlistData);

		String name = (String) playlistData.get("playlist_name");
		String email = (String) playlistData.get("user_email");
		List<Integer> songsId = (List<Integer>) playlistData.get("songs_id");

		if (name == null || email == null) {
			System.out.println("[DB] Faltan parámetros obligatorios: nombre o email");
			return "Error: nombre y email son obligatorios.";
		}

		try {
			System.out.println("[DB] Buscando user_id por email: " + email);
			String queryUser = "SELECT user_id FROM users WHERE email = ?";
			List<Map<String, Object>> userResults = queriesMaker.ejecutarConsultaSegura(queryUser, email);

			if (userResults.isEmpty()) {
				System.out.println("[DB] Usuario no encontrado: " + email);
				return "Error: No se encontró un usuario con ese email.";
			}

			Integer userId = (Integer) userResults.get(0).get("user_id");
			System.out.println("[DB] user_id encontrado: " + userId);

			System.out.println("[DB] Insertando nueva playlist con nombre: '" + name + "', para user_id: " + userId);
			String insertSql = "INSERT INTO playlists (name, user_id) VALUES (?, ?) RETURNING playlist_id";
			List<Map<String, Object>> insertResult = queriesMaker.ejecutarConsultaSegura(insertSql, name, userId);

			if (insertResult.isEmpty()) {
				System.out.println("[DB] Error: No se pudo insertar la playlist.");
				return "Error: Fallo al insertar la playlist.";
			}

			int playlistID = (int) insertResult.get(0).get("playlist_id");
			System.out.println("[DB] ID de la playlist creada: " + playlistID);

			if (songsId != null) {
				System.out.println("[DB] Insertando canciones asociadas a la nueva playlist...");
				String insertSql2 = "INSERT INTO playlist_songs (playlist_id, song_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
				for (int song = 0; song < songsId.size(); song++) {
					int result = queriesMaker.ejecutarActualizacion(insertSql2, playlistID, songsId.get(song));
					System.out.println("[DB] Añadida canción ID " + songsId.get(song) + " => "
							+ (result > 0 ? "OK" : "YA EXISTE"));
				}
			} else {
				System.out.println("[DB] No se proporcionaron canciones para insertar.");
			}

			System.out.println("[DB] Playlist insertada correctamente con ID: " + playlistID);
			return "Playlist creada exitosamente.";

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[DB] Excepción en la creación de la playlist: " + e.getMessage());
			return "Error en el servidor: " + e.getMessage();
		}
	}

	/**
	 * Agrega una canción a la tabla de favoritos. Se envía por el cuerpo el email
	 * del usuario y el ID de la canción que desea agregar.
	 * 
	 * @param data Cuerpo de la petición con email y playlist_id.
	 */
	@PostMapping("/favorites/addFavorite")
	public void addFavoriteSong(@RequestBody Map<String, Object> data) {
		Integer songId = (Integer) data.get("songId");
		String email = (String) data.get("email");

		if (songId < 0) {
			LOGGER.log(Level.SEVERE, "El identificador del a base de datos no puede ser inferior a 0.");
		} else if (email == null) {
			LOGGER.log(Level.SEVERE, "El correo electrónico no puede ser nulo.");
		} else if (!userExists(email)) {
			LOGGER.log(Level.SEVERE, "No se conoce la existencia de nadie con el correo proporcioando.");
		} else {

			// Encuentra y extrae el usuario con el correo proporcionado
			LOGGER.log(Level.INFO, "Intentando obtener el userId de %s".formatted(email));

			String querySql = "SELECT user_id FROM users where email = ?";
			List<Map<String, Object>> result = queriesMaker.ejecutarConsultaSegura(querySql, email);

			int userId = (Integer) result.get(0).get("user_id");

			// Agrega la canción a la tabla de favoritos al usuario indicado.
			LOGGER.log(Level.INFO,
					"Intentando agregar canción con ID %d a la dirección de correo %s".formatted(songId, email));

			String insertSql = "INSERT INTO favorites (user_id, song_id) VALUES (?, ?) ON CONFLICT DO NOTHING";

			queriesMaker.ejecutarActualizacion(insertSql, userId, songId);
		}

	}

	@GetMapping("/favorites/isFavorite")
	public String isFavorite(@RequestParam String email, @RequestParam int songId) {
		boolean exists = false;
		if (songId < 0) {
			LOGGER.log(Level.SEVERE, "El identificador del a base de datos no puede ser inferior a 0.");
		} else if (email == null) {
			LOGGER.log(Level.SEVERE, "El correo electrónico no puede ser nulo.");
		} else if (!userExists(email)) {
			LOGGER.log(Level.SEVERE, "No se conoce la existencia de nadie con el correo proporcioando.");
		} else {

			// Encuentra y extrae el usuario con el correo proporcionado
			LOGGER.log(Level.INFO, "Intentando obtener el userId de %s".formatted(email));

			String querySql = "SELECT user_id FROM users where email = ?";
			List<Map<String, Object>> result = queriesMaker.ejecutarConsultaSegura(querySql, email);

			int userId = (Integer) result.get(0).get("user_id");

			LOGGER.log(Level.INFO, "Intentando determinar si la canción con id %d esta dentro de favoritos de %s"
					.formatted(songId, email));

			String querySql2 = "SELECT * FROM favorites where user_id = ? AND song_id = ?";

			List<Map<String, Object>> result2 = queriesMaker.ejecutarConsultaSegura(querySql2, userId, songId);

			exists = !result2.isEmpty();

		}
		// --- { Verificación de la contraseña } --- //
		return exists ? "true" : "false";
	}

	/**
	 * Eliminar una canción a la tabla de favoritos. Se envía por el cuerpo el email
	 * del usuario y el ID de la canción que desea eliminar.
	 * 
	 * @param data Cuerpo de la petición con email y playlist_id.
	 */
	@PostMapping("/favorites/deleteFavorite")
	public void deleteFavoriteSong(@RequestBody Map<String, Object> data) {
		Integer songId = (Integer) data.get("songId");
		String email = (String) data.get("email");

		if (songId < 0) {
			LOGGER.log(Level.SEVERE, "El identificador del a base de datos no puede ser inferior a 0.");
		} else if (email == null) {
			LOGGER.log(Level.SEVERE, "El correo electrónico no puede ser nulo.");
		} else if (!userExists(email)) {
			LOGGER.log(Level.SEVERE, "No se conoce la existencia de nadie con el correo proporcioando.");
		} else {

			// Encuentra y extrae el usuario con el correo proporcionado
			LOGGER.log(Level.INFO, "Intentando obtener el userId de %s".formatted(email));

			String querySql = "SELECT user_id FROM users where email = ?";
			List<Map<String, Object>> result = queriesMaker.ejecutarConsultaSegura(querySql, email);

			int userId = (Integer) result.get(0).get("user_id");

			// Agrega la canción a la tabla de favoritos al usuario indicado.
			LOGGER.log(Level.INFO,
					"Intentando eliminar canción con ID %d a la dirección de correo %s".formatted(songId, email));

			String deleteSql = "DELETE FROM favorites WHERE user_id = ? AND song_id = ?";

			queriesMaker.ejecutarActualizacion(deleteSql, userId, songId);
		}

	}

	@GetMapping("/favorites/getFavorites")
	public List<SongDTO> getFavorites(@RequestParam String email) {
		if (email == null || email.isBlank()) {
			LOGGER.log(Level.SEVERE, "El correo electrónico no puede ser nulo.");
			return Collections.emptyList(); // Devuelve lista vacía si está mal
		}

		if (!userExists(email)) {
			LOGGER.log(Level.SEVERE, "No se conoce la existencia de nadie con el correo proporcionado.");
			return Collections.emptyList(); // También lista vacía si el user no existe
		}

		String sql = """
					SELECT *
					FROM songs
					WHERE song_id IN (
						SELECT song_id
						FROM FAVORITES
						WHERE user_id = (
							SELECT user_id
							FROM users
							WHERE email = ?
						)
					)
				""";

		List<Map<String, Object>> rows = queriesMaker.ejecutarConsultaSegura(sql, email);

		List<SongDTO> canciones = new ArrayList<>();

		for (Map<String, Object> row : rows) {
			SongDTO song = new SongDTO();
			song.setSongId((Integer) row.get("song_id"));
			song.setTitle((String) row.get("title"));
			song.setArtist((String) row.get("artist"));
			song.setUrl((String) row.get("url"));
			song.setDuration((Integer) row.get("duration"));

			Object genresObj = row.get("genres");
			if (genresObj instanceof java.sql.Array sqlArray) {
				try {
					String[] genresArray = (String[]) sqlArray.getArray();
					song.setGenres(Arrays.asList(genresArray));
				} catch (SQLException e) {
					LOGGER.log(Level.WARNING, "No se pudieron leer los géneros", e);
					song.setGenres(Collections.emptyList());
				}
			} else {
				song.setGenres(Collections.emptyList());
			}

			canciones.add(song);
		}

		return canciones;
	}

	@PostMapping("/configurations/changeUsername")
	public void changeUsername(@RequestBody Map<String, Object> data) {

		String email = (String) data.get("email");
		String newUsername = (String) data.get("newUsername");
		LOGGER.log(Level.INFO, "Email del usuario que quiere cambiar su nickname: " + email);
		LOGGER.log(Level.INFO, "Nombre que desea el nuevo usuario: " + email);

		String queryUser = "UPDATE users SET username = ? WHERE email = ?";

		queriesMaker.ejecutarActualizacion(queryUser, newUsername, email);
	}

	@PostMapping("/configurations/changeEmail")
	public void changeEmail(@RequestBody Map<String, Object> data) {

		String email = (String) data.get("email");
		String newEmail = (String) data.get("newEmail");
		LOGGER.log(Level.INFO, "Email del usuario que quiere cambiar su email: " + email);
		LOGGER.log(Level.INFO, "Email que desea el nuevo usuario: " + newEmail);

		String queryUser = "UPDATE users SET email = ? WHERE email = ?";

		int result = queriesMaker.ejecutarActualizacion(queryUser, newEmail, email);

		if (result > 0) {
			LOGGER.log(Level.INFO, "Cambio de email ejecutado con éxito. %s -> %s".formatted(email, newEmail));
		}
	}

	@PostMapping("/configurations/changePassword")
	public void changePassword(@RequestBody Map<String, Object> data) {

		String email = (String) data.get("email");
		String newPassword = (String) data.get("newPassword");
		LOGGER.log(Level.INFO, "Email del usuario que quiere cambiar su contrasña: " + email);

		String queryUser = "UPDATE users SET password = ? WHERE email = ?";

		String hashedPassword = passwordEncoder.encode(newPassword);

		int result = queriesMaker.ejecutarActualizacion(queryUser, hashedPassword, email);

		if (result > 0) {
			LOGGER.log(Level.INFO, "Cambio de contraseña para %s ejecutado con éxito".formatted(email));
		}
	}

	/**
	 * Elimina una playlist del sistema a partir de su identificador.
	 *
	 * Este endpoint espera recibir el ID de la playlist como parámetro en la URL
	 * bajo el nombre 'playlist_id'. Si no se encuentra ninguna playlist con ese ID,
	 * se devuelve un mensaje de error. Si la operación es exitosa, se informa al
	 * cliente. En caso de error de base de datos o excepciones inesperadas, se
	 * devuelve un mensaje de error del servidor.
	 *
	 * @param playlist_id ID numérico de la playlist que se desea eliminar.
	 * @return Mensaje indicando el resultado de la operación: - "Playlist creada
	 *         exitosamente." si la eliminación fue exitosa. - "Error: No se ha
	 *         encontrado la playlist." si no existe. - Mensajes de error si hay
	 *         fallos en la validación o en el servidor.
	 */
	@PostMapping("/playlists/deletePlaylist")
	public String deletePlaylist(@RequestBody Map<String, Object> data) {
		Integer playlist_id = (Integer) data.get("playlist_id");

		System.out.println("Datos recibidos para eliminar playlist: " + playlist_id);

		if (playlist_id < 0) {
			return "Error: El ID de la playlist es obligatorio.";
		}

		try {
			String queryUser = "SELECT 1 FROM playlists WHERE playlist_id = ?";
			List<Map<String, Object>> userResults = queriesMaker.ejecutarConsultaSegura(queryUser, playlist_id);

			if (userResults.isEmpty()) {
				return "Error: No se ha encontrado la playlist.";
			}

			Integer userId = (Integer) userResults.get(0).get("user_id");

			String deleteSql = "DELETE FROM playlists WHERE playlist_id = ?";
			int rowsAffected = queriesMaker.ejecutarActualizacion(deleteSql, playlist_id);

			return (rowsAffected > 0) ? "Playlist creada exitosamente." : "Error al crear la playlist.";

		} catch (Exception e) {
			e.printStackTrace();
			return "Error en el servidor: " + e.getMessage();
		}
	}

	/**
	 * Agrega canciones a una playlist existente dentro de la base de datos.
	 * 
	 * @param data Datos enviados a través de la petición POST. Se espera que
	 *             contenga el ID de la playlist y una colección de los ID de las
	 *             canciones que se desean agregar a ella.
	 * @return Log
	 */
	@PostMapping("/playlists/add-songs")
	public String addSongsToPlaylist(@RequestBody Map<String, Object> data) {
		System.out.println("Datos recibidos para agregar canciones: " + data);

		Integer playlistId = (Integer) data.get("playlist_id");
		@SuppressWarnings("unchecked")
		List<Integer> songIds = (List<Integer>) data.get("song_ids");

		if (playlistId == null || songIds == null || songIds.isEmpty()) {
			return "Error: playlist_id y una lista de song_ids son obligatorios.";
		}

		try {
			// Verifica que la playlist exista
			String checkPlaylist = "SELECT 1 FROM playlists WHERE playlist_id = ?";
			List<Map<String, Object>> exists = queriesMaker.ejecutarConsultaSegura(checkPlaylist, playlistId);

			if (exists.isEmpty()) {
				return "Error: La playlist con ID " + playlistId + " no existe.";
			}

			// Agrega cada canción
			String insertSql = "INSERT INTO playlist_songs (playlist_id, song_id) VALUES (?, ?) ON CONFLICT DO NOTHING";

			for (Integer songId : songIds) {
				queriesMaker.ejecutarActualizacion(insertSql, playlistId, songId);
			}

			return "Canciones agregadas exitosamente a la playlist.";

		} catch (Exception e) {
			e.printStackTrace();
			return "Error en el servidor: " + e.getMessage();
		}
	}

	/**
	 * Elimina una o varias canciones de una playlist específica.
	 *
	 * @param data Un JSON con los campos: - "playlist_id" (int): ID de la playlist.
	 *             - "song_ids" (List<Integer>): lista de IDs de canciones a
	 *             eliminar.
	 * @return Mensaje indicando si la operación fue exitosa o si ocurrió un error.
	 */
	@PostMapping("/playlists/delete-songs")
	public String deleteSongsFromPlaylist(@RequestBody Map<String, Object> data) {
		System.out.println("Datos recibidos para eliminar canciones: " + data);

		Integer playlistId = (Integer) data.get("playlist_id");
		@SuppressWarnings("unchecked")
		List<Integer> songIds = (List<Integer>) data.get("song_ids");

		if (playlistId == null || songIds == null || songIds.isEmpty()) {
			return "Error: playlist_id y una lista de song_ids son obligatorios.";
		}

		try {
			// Verifica que la playlist exista
			String checkPlaylist = "SELECT 1 FROM playlists WHERE playlist_id = ?";
			List<Map<String, Object>> exists = queriesMaker.ejecutarConsultaSegura(checkPlaylist, playlistId);

			if (exists.isEmpty()) {
				return "Error: La playlist con ID " + playlistId + " no existe.";
			}

			String deleteSql = "DELETE FROM playlist_songs WHERE playlist_id = ? AND song_id = ?";

			for (Integer songId : songIds) {
				queriesMaker.ejecutarActualizacion(deleteSql, playlistId, songId);
			}

			return "Canciones eliminadas exitosamente de la playlist.";

		} catch (Exception e) {
			e.printStackTrace();
			return "Error en el servidor: " + e.getMessage();
		}
	}

	/**
	 * Manejador de rutas no encontradas (404).
	 *
	 * @return Respuesta JSON con mensaje de error.
	 */
	@PostMapping("/error")
	public ResponseEntity<Map<String, Object>> handleError() {
		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("status", 404);
		errorResponse.put("error", "No encontrado");
		errorResponse.put("message", "La ruta invocada no existe en los caminos del backend sagrado.");
		errorResponse.put("timestamp", System.currentTimeMillis());
		return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
	}

}
