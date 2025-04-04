package com.ax14n.soundbeat.servidor.controller;

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
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Clase que hice con el único propósito de recibir peticiones POST para luego
 * subirlas a la base de datos. La ruta para acceder y envíar peticiones POST a
 * este endpoint es localhost:8080/api/<servicio>
 */
@RestController
@RequestMapping("/api")
public class DbController {

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
	public DbController(QueriesMaker queriesMaker) {
		this.queriesMaker = queriesMaker;
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
	 * Solicita la información de las canciones almacenadas en el servidor. Puede
	 * especificarse un género por parámetros a través de la petición POST para
	 * hacer una búsqueda más específica.
	 * 
	 * Para consulta general: curl "http://localhost:8080/songs"
	 * 
	 * Para búscar con género: curl "http://localhost:8080/songs?genre=rock"
	 * 
	 * @param genre Género de la canción.
	 * @return Canciones almacenadas en el servidor, por género o en general.
	 */
	@GetMapping("/songs")
	public List<Map<String, Object>> getSongs(@RequestParam(required = false) String genre) {
		if (genre == null || "null".equalsIgnoreCase(genre.trim())) {
			return queriesMaker.ejecutarConsulta("SELECT * FROM songs");
		}
		String sql = "SELECT * FROM songs WHERE genres IS NOT NULL AND ? = ANY(genres)";
		return queriesMaker.ejecutarConsultaSegura(sql, genre);
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
		String username = "undefined";
		String email = (String) userData.get("email");
		String rawPassword = (String) userData.get("password");

		// --- { Validación de datos } --- //
		if (email == null || rawPassword == null) {
			return "Error: email y password son obligatorios.";
		}

		// --- { Cifrar la contraseña antes de guardarla } --- //
		String hashedPassword = passwordEncoder.encode(rawPassword);

		// --- { Creación de la consulta de inserción } --- //
		String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";

		// --- { Ejecución de la consulta } --- //
		int rowsAffected = queriesMaker.ejecutarActualizacion(sql, username, email, hashedPassword);

		// --- { Envío del resultado } --- //
		return (rowsAffected > 0) ? "Usuario registrado exitosamente!" : "Error al registrar el usuario.";
	}

	/**
	 * Verifica si un usuario ya existe en la base de datos.
	 * 
	 * curl "http://localhost:8080/userExists?email=usuario@example.com"
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

		// --- { Creación de la consulta de selección } --- //
		String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

		// --- { Ejecución de la consulta } --- //
		boolean exists = !queriesMaker.ejecutarConsultaSegura(sql, email).isEmpty();

		// --- { Retorno del resultado } --- //
		return exists;
	}

	/**
	 * Inicia sesión verificando la contraseña cifrada.
	 *
	 * curl -X POST http://localhost:8080/login \ -H "Content-Type:
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
		List<Map<String, Object>> resultados = queriesMaker.ejecutarConsultaSegura(sql, email);

		if (resultados.isEmpty()) {
			return "Error: usuario no encontrado.";
		}

		String storedHashedPassword = (String) resultados.get(0).get("password");

		// --- { Verificación de la contraseña } --- //
		if (passwordEncoder.matches(rawPassword, storedHashedPassword)) {
			return "Inicio de sesión exitoso!\n";
		} else {
			return "Error: contraseña incorrecta.\n";
		}
	}

	private static final String ERROR_PATH = "/error";

	/**
	 * Manejador de rutas no encontradas (404).
	 *
	 * @return Respuesta JSON con mensaje de error.
	 */
	@RequestMapping(value = ERROR_PATH)
	public ResponseEntity<Map<String, Object>> handleError() {
		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("status", 404);
		errorResponse.put("error", "No encontrado");
		errorResponse.put("message", "La ruta invocada no existe en los caminos del backend sagrado.");
		errorResponse.put("timestamp", System.currentTimeMillis());
		return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
	}

}
