CREATE TABLE Users (
    user_id SERIAL PRIMARY KEY,      -- Id del usuario.
    username VARCHAR(255) NOT NULL UNIQUE,    -- Nombre de usuario.
    email VARCHAR(255) NOT NULL UNIQUE,    -- Correo del usuario
    password VARCHAR(255) NOT NULL,     -- Contraseña hasheada del usuario.
    date_joined TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- Fecha en la que se ha unido.
);

CREATE TABLE Songs (
    song_id SERIAL PRIMARY KEY,      -- Id de la canción.
    title VARCHAR(255) NOT NULL,      -- Título de la canción.
    artist VARCHAR(255) NOT NULL,     -- Artista de la canción.
    duration INTEGER NOT NULL,      -- Duración de la canción.
    url VARCHAR(500) NOT NULL,      -- Url de la canción.
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- Fecha que se ha agregado la canción.
);

CREATE TABLE Playlists (
    playlist_id SERIAL PRIMARY KEY,         -- Id de la playlist.
    name VARCHAR(255) NOT NULL,          -- Nombre de la playlist.
    user_id INTEGER NOT NULL,           -- Id del usuario que la ha creado.
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,     -- Fecha de creación de la playlist.
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE -- Asociación.
);

CREATE TABLE Playlist_Songs (
    playlist_id INTEGER NOT NULL,             -- Id de la playlist.
    song_id INTEGER NOT NULL,              -- Id de la canción.
    PRIMARY KEY (playlist_id, song_id),           -- Ambos datos son la PK.
    FOREIGN KEY (playlist_id) REFERENCES Playlists(playlist_id) ON DELETE CASCADE, -- Asociación.
    FOREIGN KEY (song_id) REFERENCES Songs(song_id) ON DELETE CASCADE    -- Asociación.
);

CREATE TABLE Favorites (
 user_id INTEGER NOT NULL,           -- Id del usuario.
 song_id INTEGER NOT NULL,           -- Id de la canción.
 PRIMARY KEY (user_id, song_id),         -- Ambos son la PK. Un usuario no puede tener de favorita una canción más de una vez.
 FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE, -- Asociación.
 FOREIGN KEY (song_id) REFERENCES Songs(song_id) ON DELETE CASCADE -- Asociación.
);

CREATE TABLE History (
    history_id SERIAL PRIMARY KEY,   -- Identificador único del historial.
    user_id INTEGER NOT NULL,        -- Id del usuario que reprodujo la canción.
    song_id INTEGER NOT NULL,        -- Id de la canción reproducida.
    played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fecha y hora en la que se reprodujo la canción.
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE, -- Asociación con Users.
    FOREIGN KEY (song_id) REFERENCES Songs(song_id) ON DELETE CASCADE -- Asociación con Songs.
);
