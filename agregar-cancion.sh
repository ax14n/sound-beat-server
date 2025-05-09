#!/bin/bash

# Verifica que se pase un argumento
if [ "$#" -ne 1 ]; then
    echo "Uso: $0 <ruta_del_archivo_mp3>" 
    exit 1
fi

# Archivo de entrada
ARCHIVO_ENTRADA="$1"

# Verifica si el archivo existe
if [ ! -f "$ARCHIVO_ENTRADA" ]; then
    echo "Error: El archivo '$ARCHIVO_ENTRADA' no existe."
    exit 1
fi

# Nombre base sin extensión
NOMBRE_BASE=$(basename -- "$ARCHIVO_ENTRADA")
NOMBRE="${NOMBRE_BASE%.*}"
DIRECTORIO_BASE="$HOME/Documentos/SoundBeat-Server/extras/canciones-hls"
DIRECTORIO_SALIDA="$DIRECTORIO_BASE/$NOMBRE"  # Usa el mismo nombre del archivo dentro del directorio especificado

# Verifica si el directorio base existe, si no, lo crea
if [ ! -d "$DIRECTORIO_BASE" ]; then
    printf "\tCreando el directorio base en %s\n", "$DIRECTORIO_BASE"
    mkdir -p "$DIRECTORIO_BASE"
fi

# Verifica si el directorio de salida existe, si no, lo crea
if [ ! -d "$DIRECTORIO_SALIDA" ]; then
    printf "\tCreando el directorio donde se guardará la canción en %s\n", "$DIRECTORIO_SALIDA"
    mkdir -p "$DIRECTORIO_SALIDA"
fi

# Extrae metadatos del archivo MP3
AUTOR=$(ffprobe -v error -show_entries format_tags=artist -of default=noprint_wrappers=1:nokey=1 "$ARCHIVO_ENTRADA")
DURACION=$(ffprobe -i "$ARCHIVO_ENTRADA" -show_entries format=duration -v quiet -of csv="p=0")

echo "Convirtiendo $ARCHIVO_ENTRADA. Por favor, espere un momento..."
# Ejecuta ffmpeg para la conversión
ffmpeg -loglevel quiet -i "$ARCHIVO_ENTRADA" \
       -codec:a aac -b:a 128k -vn \
       -hls_time 10 -hls_list_size 0 \
       -hls_segment_filename "$DIRECTORIO_SALIDA/${NOMBRE}_%03d.ts" \
       "$DIRECTORIO_SALIDA/${NOMBRE}.m3u8" > /dev/null

echo "Conversión completada. Archivos generados en '$DIRECTORIO_SALIDA'"

echo "Envíando datos al servidor Spring Boot..."
curl -X POST http://localhost:8080/api/import \
    -u "admin:admin" \
    -H "Content-Type: application/json" \
    -d "{
        \"titulo\": \"$NOMBRE\",
        \"autor\": \"$AUTOR\",
        \"duracion\": $DURACION,
        \"url\": \"$DIRECTORIO_SALIDA/${NOMBRE}.m3u8\"
        }"
printf "\n"
