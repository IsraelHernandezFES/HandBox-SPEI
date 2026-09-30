# Handbox SPEI - API de Validación & Sandbox Visual

Una plataforma robusta desarrollada en Spring Boot y JavaScript puro para la simulación, validación y procesamiento de transferencias tipo SPEI (Sistema de Pagos Electrónicos Interbancarios).

Este proyecto está diseñado para manejar operaciones transaccionales seguras (T2T y VNT) aplicando reglas de negocio estrictas, manejo de idempotencia y orquestación de contenedores para un despliegue portable, acompañado ahora de una Interfaz Gráfica de Sandbox Dinámica para pruebas en tiempo real.

## ⚙️ Características Principales

* **Interfaz Visual Dinámica (Sandbox):** Cliente ligero integrado con autogeneración de datos válidos (UUIDs, CLABEs con algoritmo oficial módulo 10, referencias alfanuméricas) y consola de logs en tiempo real.
* **Procesamiento de Operaciones (T2T y VNT):** Creación y validación de órdenes de pago con un flujo de estados simulado (ej. `S03` para liquidado, `S04` para devuelto).
* **Idempotencia:** Implementación de caché mediante el header `Clave-Idempotencia` para evitar la duplicidad de transacciones ante reintentos de red.
* **Validación de Reglas de Negocio Estrictas:**
  * Validación matemática del dígito verificador de la cuenta CLABE (Error `PRX-002`).
  * Verificación de existencia y estado operativo de instituciones bancarias (Errores `PRX-003`, `PRX-011`).
  * Validación estructural de divisas, importes y folios.
* **Testing Automatizado en UI:** Capacidad de forzar más de 15 escenarios de error (ej. fondos insuficientes `PRX-020`) a través de un menú desplegable en la interfaz gráfica que inyecta el header `X-Escenario-Forzado` o muta el payload estratégicamente.
* **Manejo de Errores Global:** Respuestas estandarizadas con códigos HTTP 422 (Unprocessable Entity) estructuradas de forma limpia.

## 🛠️ Stack Tecnológico

**Backend:**

* **Lenguaje:** Java 17
* **Framework:** Spring Boot (Web, Data JPA, Validation)
* **Base de Datos:** MySQL 8.0
* **Dependencias Core:** Maven, Lombok, Jackson

**Frontend (Sandbox):**

* HTML5 semántico
* Tailwind CSS (framework de diseño de utilidades)
* JavaScript Vanilla (peticiones asíncronas con Fetch API)

**Infraestructura:**

* **Despliegue:** Docker & Docker Compose
* **Testing:** JUnit 5, MockMvc

---

## 🚀 Guía de Instalación Rápida

Sigue estos pasos para montar el proyecto completo (Base de Datos, API e Interfaz Visual) en tu entorno local en cuestión de minutos.

### 📋 Prerrequisitos

* **Docker Desktop** instalado y ejecutándose (asegúrate de tener la virtualización VT-x habilitada en tu BIOS).
* **Java 17 (JDK)** configurado en tus variables de entorno.
* **Git** para clonar el repositorio.

### 1️⃣ Clonar el repositorio

Abre tu terminal y ejecuta:

```bash
git clone https://github.com/IsraelHernandezFES/HandBox-SPEI
cd HandBox-SPEI
```

### 2️⃣ Generar el ejecutable (.jar)

Utiliza el wrapper de Maven incluido en el proyecto para compilar el código fuente, omitiendo las pruebas para mayor velocidad:

```bash
# En Windows (PowerShell)
.\mvnw clean package -DskipTests

# En Linux / Mac
./mvnw clean package -DskipTests
```

### 3️⃣ Levantar los contenedores con Docker

Inicia la base de datos MySQL y la aplicación Spring Boot de forma orquestada:

```bash
docker compose up --build -d
```

> **Nota:** La bandera `-d` levanta los servicios en segundo plano. El sistema incluye un `healthcheck` automático que asegura que la API espere a que MySQL termine de inicializarse.

### 4️⃣ ¡Probar el Sandbox Visual!

Una vez que los contenedores estén corriendo, abre tu navegador web y visita:

👉 **http://localhost:8080** (o la ruta raíz configurada para tu `index.html`).

#### ✅ Ejecutar un Escenario de Éxito (Flujo Normal)

1. Al cargar la página, el formulario se autocompletará con datos 100% válidos.
2. Asegúrate de que el menú **"Simular Escenario"** esté en *Ninguno (Flujo Normal)*.
3. Presiona el botón **Ejecutar Operación T2T**.
4. Observarás los badges de éxito (`HTTP 201 OK` y `S03 Liquidado`), y la transacción aparecerá registrada en la consola de logs inferior.

#### ❌ Simular un Escenario de Error (Testing)

1. En la parte superior de la interfaz, abre el menú **Simular Escenario**.
2. Selecciona un error de validación (ej. `PRX-006 (Divisa distinta a MXN)`).
3. El frontend adaptará automáticamente el formulario (cambiando la divisa a USD).
4. Al presionar **Ejecutar Operación**, la API interceptará la falla y el Sandbox mostrará la tarjeta roja con el error `HTTP 422 Error` y el detalle exacto devuelto por Spring Boot en el visor JSON.
5. Puedes alternar a la pestaña **JSON Crudo** si prefieres ver o editar el payload manualmente y validarlo con el botón **"Formatear JSON"**.

## 🧪 Ejecución de Pruebas Automatizadas

El proyecto incluye una suite de pruebas de integración para validar la lógica de negocio a nivel backend. Para ejecutarlas localmente sin Docker:

```bash
.\mvnw test
```
