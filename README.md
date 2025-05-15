# ¡Bienvenido al servidor dedicado de SoundBeat!

> [!NOTE]
> Si has acabado aquí significa que estás interesado en el proyecto SoundBeat o que estas buscando el proyecto prinicipal. En caso de que estes aquí de forma conciente: ¡Felicidades y bienvenido al servidor dedicado de Soundbeat!
> En caso de que esta no sea la razón principal por la que te encuentras aquí, entonces te recomiendo visitar el repositorio principal de la aplicación donde te explico qué es SoundBeat y donde explico ciertos aspectos a tomar en
> cuenta. Puedes acceder a él haciendo click [aquí](https://github.com/ax14n/soundbeat-test).

# ¿Qué es SoundBeat Server?

SoundBeat Server es un servidor de streaming desarrollado en Java con Spring Boot, cuya función principal es transmitir canciones a la aplicación SoundBeat utilizando HLS (HTTP Live Streaming). Este protocolo permite segmentar el 
audio en fragmentos y entregarlos de forma eficiente a través de HTTP, logrando una reproducción fluida y adaptable en el cliente. Además de servir contenido multimedia, el servidor se comunica internamente con una base de datos 
PostgreSQL, donde se almacena información de usuarios, canciones, playlists y otros datos relevantes del sistema.

> [!WARNING]
> Actualmente, SoundBeat Server está desacoplado de la base de datos, que se encuentra en un contenedor Docker independiente. Sin embargo, tengo previsto crear una imagen que integre tanto la API como la base de datos en un mismo
entorno, facilitando así el despliegue y mantenimiento del sistema. Por ahora, para solucionar este problema subiré el Docker File como medida de contramedida a problema. La estrucutura de tablas y asociaciones vendrán incluidas.
Si quieres visualizar los datos de la base de datos, siempre puedes abrir el servidor y acceder por HTTP, o ingresar en PgAdmin y hacerlo por tí mismo.

```yaml
version: "3.8"

services:
  # Servicio para PostgreSQL
  db:
    image: postgres:latest
    container_name: postgres_db
    environment:
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: adminpassword
      POSTGRES_DB: streaming_db
    volumes:
      - pgdata:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    networks:
      - streaming_network
    restart: always

  # Servicio para pgAdmin4
  pgadmin:
    image: dpage/pgadmin4:latest
    container_name: pgadmin
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@admin.com
      PGADMIN_DEFAULT_PASSWORD: adminpassword
    ports:
      - "5050:80"  # Cambié el puerto a 5050
    volumes:
      - pgadmin_data:/var/lib/pgadmin
    networks:
      - streaming_network
    restart: always

  # Red de Docker para comunicación entre los servicios
networks:
  streaming_network:
    driver: bridge

# Volúmenes persistentes para PostgreSQL y pgAdmin
volumes:
  pgdata:
  pgadmin_data:

```

# FAQ

- **¿El servidor puede ser utilizado en mi SO Windows?**

En un principio, no. Una de las metas de pasar el servidor a un contenedor de Docker es solucionar este problema. No es que el proyecto no funcione fuera de sistemas UNIX, sino que cuenta actualmente con dos herramientas de
utilidades escritas en BASH Script con recursos necesarios para agregar canciones o agregar usuarios de forma manual. El segundo script no es totalmente necesario, pero el primer mencionado definitivamente lo es para agregar
canciones.

- **¿Cómo agrego canciones?**

El proyecto incluye dentro un fichero escrito en BASH Script que cubre esta tarea. El fichero es llamado [agregar-cancion.sh](./agregar-cancion.sh) y acepta un fichero en formato mp3 que usará para convertirlo en el formato
deseado y agregarlo al servidor. Ojo, las canciones no son almacenadas en la base de datos por temas de optimización, sino que convierte la canción a un formato deseado y la almacena en 
`$HOME/Documentos/SoundBeat-Server/extras/canciones-hls`. La base de datos almacena los metadatos de las canciones y la ruta donde encontrar los ficheros `.m3u8` y `.ts` generados.  Si no tienes esta estrucutra de 
directorios no te preocupes, el Script la creará por tí. 

- **¿Por qué hacer un servidor propio y no utilizar un servicio de terceros alojado en la nube?**

*Considero que tener un servidor propio y libre de terceros es una forma real de autonomía y de propiedad verdadera sobre lo que uno crea.*

En un mundo donde todo depende de servicios externos, APIs cerradas y plataformas que cambian sus reglas sin previo aviso, montar tu propio backend es una manera de recuperar el control. SoundBeat Server me permite decidir cómo
se sirve la música, cuándo y desde dónde. Y eso, aunque suene exagerado, vale más que cualquier feature de moda.

Construir tus bases en un servidor de terceros es como vivir de alquiler: estás allí hasta que al dueño se le
prenda la bombilla de vender la casa o cambiar su politíca de alquiler.

- **¿Tienes pensado seguir mejorando el servidor para agregar nuevas características a la aplicación?**

Sí, pienso seguir agregando funcionabilidades. 

Considero que este servidor está muy verde y no sigue muchas de las prácticas que hay que seguir, tanto de seguridad como de manipulación de datos, o incluso de propias formas recomendadas de interactuar con Spring Boot. Pero como
he escuchado alguna vez: "Es mejor construir algo que esté mal y funcione, a no tener nada construido". El desarrollo aún no ha terminado, y aunque las bases sean las que son, todavía es un proyecto pequeño y cambiar cosas no es 
complicado de hacer.

- **¿Piensas mantener el servidor en local?**

Esto es algo que he pensado y me ha dado problemas a la hora de programar la aplicación final de SoundBeat. Conseguir un dominio facilitaría mucho las cosas en diversos aspectos como el acceso a consultas de la API, 
certificaciones, y la conexión entre API y aplicación final. En estos momentos, la única forma de conectarse con la API desde la aplicación final es modificando el código para que apunte a una dirección IP fija asociada al servidor. 
Disponer de un dominio facilitaría esta tarea ya que, si no has reservado una dirección IP dentro de tu red, esta puede cambiar, generando problemas a la hora de autentificarse o cargar canciones y reproducir canciones en línea. 
