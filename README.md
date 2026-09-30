# Handbox SPEI - API de Validación

Una API RESTful robusta desarrollada en Spring Boot para la simulación, validación y procesamiento de transferencias tipo SPEI (Sistema de Pagos Electrónicos Interbancarios).

Este proyecto está diseñado para manejar operaciones transaccionales seguras (T2T) aplicando reglas de negocio estrictas, manejo de idempotencia y orquestación de contenedores para un despliegue portable.

## ⚙️ Características Principales

* **Procesamiento de Operaciones (T2T):** Creación y validación de órdenes de pago con un flujo de estados simulado (ej. `S03` para liquidado).
* **Idempotencia:** Implementación de caché mediante el header `Clave-Idempotencia` para evitar la duplicidad de transacciones ante reintentos de red.
* **Validación de Reglas de Negocio:**
  * Validación matemática del dígito verificador de la cuenta CLABE (Error `PRX-002`).
  * Verificación de existencia de instituciones bancarias en el catálogo (Error `PRX-003`).
* **Testing en Sandbox:** Capacidad de forzar escenarios de error (ej. fondos insuficientes `PRX-020` con estado `S04`) mediante el header `X-Escenario-Forzado`.
* **Manejo de Errores Global:** Respuestas estandarizadas con códigos HTTP 422 (Unprocessable Entity) estructuradas de forma limpia.

## 🛠️ Stack Tecnológico

* **Lenguaje:** Java 17
* **Framework:** Spring Boot (Web, Data JPA, Validation)
* **Base de Datos:** MySQL 8.0
* **Dependencias Core:** Maven, Lombok, Jackson
* **Testing:** JUnit 5, MockMvc
* **Despliegue:** Docker & Docker Compose

---

## 🚀 Guía de Instalación Rápida

Sigue estos pasos para montar el proyecto completo (Base de Datos + API) en tu entorno local en cuestión de minutos.

### 📋 Prerrequisitos

* **Docker Desktop** instalado y ejecutándose (asegúrate de tener la virtualización VT-x habilitada en tu BIOS).
* **Java 17 (JDK)** configurado en tus variables de entorno.
* **Git** para clonar el repositorio.

### 1️⃣ Clonar el repositorio

Abre tu terminal y ejecuta:

```bash
git clone https://github.com/IsraelHernandezFES/HandBox-SPEI
cd Handbox-SPEI-Validation
```

### 2️⃣ Generar el ejecutable (.jar)

Utiliza el wrapper de Maven incluido en el proyecto para compilar el código fuente, omitiendo las pruebas para mayor velocidad:

```bash
# En Windows (PowerShell)
.\mvnw clean package -DskipTests

# En Linux / Mac
./mvnw clean package -DskipTests
```

Esto generará el archivo `Validation-0.0.1-SNAPSHOT.jar` dentro de la carpeta `target/`.

### 3️⃣ Levantar los contenedores con Docker

Inicia la base de datos MySQL y la aplicación Spring Boot de forma orquestada:

```bash
docker compose up --build -d
```

> **Nota:** La bandera `-d` levanta los servicios en segundo plano. El sistema incluye un `healthcheck` automático que asegura que la API espere a que MySQL termine de inicializarse.

### 4️⃣ ¡Probar la API!

<<<<<<< HEAD
La aplicación estará disponible en el puerto `8080`. Envía una petición `POST` a la ruta `http://localhost:8080/api/v1/operaciones`.

**Importante:** Recuerda incluir el header obligatorio `Clave-Idempotencia` (ej. `IDEMPOTENCIA-001`) en tus peticiones.

=======
La aplicación estará disponible en el puerto `8080`. Envía una petición `POST` a la ruta `http://localhost:8080/swagger-ui/index.html#/orden-pago-controller/crearOperacion`.

>>>>>>> 863777b10bcdccdbe06654dc4cc6173fb9d16b66
#### ✅ Ejemplo de Petición Válida (201 Created)

Este JSON pasará todas las validaciones matemáticas y de catálogo:

```json
{
  "tipoOperacion": "T2T",
<<<<<<< HEAD
  "referenciaSeguimiento": "TEST-EXITO-01",
  "concepto": "Pago de servicios",
  "folioNumerico": 1234,
  "importe": {
    "valor": 250.00,
=======
  "referenciaSeguimiento": "PRX20260929EXITO",
  "importe": {
    "valor": 1500.00,
>>>>>>> 863777b10bcdccdbe06654dc4cc6173fb9d16b66
    "divisa": "MXN"
  },
  "emisor": {
    "institucion": "801",
    "cuenta": "801180000118359717",
<<<<<<< HEAD
    "nombre": "Emisor Test"
=======
    "nombre": "Ana Ruiz Delgado",
    "identificacionFiscal": "RUDA900112HN4"
>>>>>>> 863777b10bcdccdbe06654dc4cc6173fb9d16b66
  },
  "receptor": {
    "institucion": "802",
    "cuenta": "802180000123456701",
<<<<<<< HEAD
    "nombre": "Receptor Test"
  }
=======
    "nombre": "Operacion Exitosa SA"
  },
  "concepto": "Prueba de operacion exitosa",
  "folioNumerico": 1001
>>>>>>> 863777b10bcdccdbe06654dc4cc6173fb9d16b66
}
```

#### ❌ Ejemplo de Petición Inválida (422 Unprocessable Entity)

<<<<<<< HEAD
Este JSON será rechazado (Error `PRX-003`) porque simula una institución emisora (`999`) que no existe en la base de datos:

```json
{
  "tipoOperacion": "T2T",
  "referenciaSeguimiento": "TEST-ERROR-01",
  "concepto": "Prueba de error de catálogo",
  "folioNumerico": 5678,
  "importe": {
    "valor": 100.00,
    "divisa": "MXN"
  },
  "emisor": {
    "institucion": "999",
    "cuenta": "999180000000000002",
    "nombre": "Emisor Falso"
=======
Este JSON será rechazado (Error `PRX-031`) porque la operacion tiene que ser T2T o VNT:

```json
{
  "tipoOperacion": "SPEI_DIRECTO",
  "referenciaSeguimiento": "PRX20260929PRX031",
  "importe": {
    "valor": 1500.00,
    "divisa": "MXN"
  },
  "emisor": {
    "institucion": "801",
    "cuenta": "801180000118359717",
    "nombre": "Ana Ruiz Delgado"
>>>>>>> 863777b10bcdccdbe06654dc4cc6173fb9d16b66
  },
  "receptor": {
    "institucion": "802",
    "cuenta": "802180000123456701",
<<<<<<< HEAD
    "nombre": "Receptor Test"
  }
=======
    "nombre": "Empresa Beneficiaria SA"
  },
  "concepto": "Prueba de operacion invalida",
  "folioNumerico": 9094
>>>>>>> 863777b10bcdccdbe06654dc4cc6173fb9d16b66
}
```

## 🧪 Ejecución de Pruebas Automatizadas

El proyecto incluye una suite de pruebas de integración para validar la lógica de negocio. Para ejecutarlas localmente sin Docker:

```bash
.\mvnw test
```
