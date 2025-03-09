package com.ax14n.soundbeat.servidor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase que inicia el servidor.
 */
@SpringBootApplication
public class ServerInitializer {

	/**
	 * Función principal del programa, encarga de dar inicio a la ejecución del
	 * servidor y dar acceso a los clientes a sus canciones.
	 * 
	 * @param args NOT IMPLEMENTED.
	 */
	public static void main(String[] args) {
		// Inicia un hilo con el servidor y los argumentos dados (ninguno).
		SpringApplication.run(ServerInitializer.class, "NOT IMPLEMENTED");
	}

}
