#!/usr/bin/env bash
# Arranca el servidor para la sustentacion con credenciales estables.
#
# No hay ninguna clave escrita en el repositorio: se piden al arrancar o se
# toman del entorno. Ejecutar desde la raiz: bash scripts/arrancar-demo.sh
set -euo pipefail

: "${ADMIN_CORREO:=admin@plazoleta.com}"

if [ -z "${ADMIN_CLAVE:-}" ]; then
  read -r -p "Clave para el ADMINISTRADOR de la demo: " ADMIN_CLAVE
  echo
fi

if [ -z "${ADMIN_CLAVE}" ]; then
  echo "Se necesita una clave. Aborta."
  exit 1
fi

# Secreto de firma aleatorio por ejecucion: los tokens duran lo que dure la demo.
: "${AUTH_SECRET:=$(head -c 32 /dev/urandom | base64 | tr -d '\n=' )}"
: "${PORT:=8080}"

export ADMIN_CORREO ADMIN_CLAVE AUTH_SECRET PORT

echo ">> Compilando..."
mvn -q compile

echo ">> Arrancando en el puerto ${PORT}"
echo ">> Administrador: ${ADMIN_CORREO}"
echo
java -cp target/classes Main
