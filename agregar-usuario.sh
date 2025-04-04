#!/bin/bash

# Verificar si se han proporcionado los argumentos necesarios
if [ $# -ne 2 ]; then
  echo "Uso incorrecto. Se necesitan dos argumentos: correo y contraseña."
  echo "Uso: $0 <correo> <contraseña>"
  exit 1
fi

# Asignar los argumentos a variables
correo=$1
password=$2

# Imprimir las variables para ver si están correctamente asignadas
echo "Correo: $correo"
echo "Contraseña: $password"

# URL del endpoint de registro
url="http://localhost:8080/api/register"

# Hacer la solicitud POST usando cURL con autenticación básica y obtener el código HTTP
respuesta=$(curl -s -w "%{http_code}" -X POST $url \
    -u "admin:admin" \
    -H "Content-Type: application/json" \
    -d '{"email": "'"$correo"'", "password": "'"$password"'"}')

# Imprimir la respuesta completa de cURL para ver lo que devuelve
echo "Respuesta completa de cURL: $respuesta"

# Extraer el código HTTP (últimos 3 caracteres de la respuesta)
codigo_http="${respuesta: -3}"

# Extraer el cuerpo de la respuesta (todo excepto los últimos 3 caracteres)
cuerpo_respuesta="${respuesta:0:${#respuesta}-3}"

# Mostrar el resultado según el código de estado HTTP
echo "Código HTTP: $codigo_http"
echo "Cuerpo de la respuesta: $cuerpo_respuesta"

if [ "$codigo_http" -eq 200 ]; then
    echo "¡Éxito: $cuerpo_respuesta!"
else
    echo "¡Error: $cuerpo_respuesta!"
fi
