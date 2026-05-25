# HealthTrack Mobile - Aplicación de Monitoreo de Salud Familiar

Bienvenido a **HealthTrack Mobile**, la versión móvil de la plataforma HealthTrack, desarrollada nativamente en **Kotlin** y **Jetpack Compose** para Android. Esta aplicación está diseñada bajo una paleta de colores institucional y premium (Guinda, Dorado, Gris Claro y Blanco), ofreciendo una interfaz accesible, moderna e interactiva para el cuidado preventivo y el monitoreo de la salud familiar.

HealthTrack Mobile se conecta de forma directa y bidireccional con una base de datos en la nube (Firebase Firestore) compartida con la aplicación de escritorio, permitiendo sincronización en tiempo real de métricas, citas, logros, alertas y recordatorios familiares.

---

## Características Principales

### 1. Panel de Control Interactivo (Dashboard)
* **Resumen Fisiológico:** Visualización de métricas críticas como el Índice de Masa Corporal (IMC), Frecuencia Cardíaca, niveles de Glucosa y el avance en las Metas de Salud diarias.
* **Integración del Clima y Alertas Sanitarias:** Muestra datos climáticos locales y alertas epidemiológicas oficiales de forma automática basadas en la ubicación del paciente.
* **Semaforización de Alerta:** Indicadores visuales en verde (normal), dorado (advertencia) y rojo (crítico) para evaluar el estado físico de forma rápida.

### 2. Gestión Filiar ("Mi Familia")
* **Búsqueda de Pacientes:** Localización de otros familiares en la base de datos de HealthTrack mediante búsqueda por nombre completo.
* **Vinculación Directa:** Permite agregar y mantener una lista activa de familiares.
* **Monitoreo Remoto:** Acceso al resumen de salud de los familiares vinculados, incluyendo su ficha clínica (alergias, tipo de sangre, antecedentes) y sus últimas métricas registradas (glucosa, presión, pulso).

### 3. Registro y Seguimiento de Métricas
* **Ingreso Manual:** Formulario optimizado para registrar peso, glucosa (ayunas/postprandial), presión arterial (sistólica/diastólica) y pulso cardíaco.
* **Gráficas de Tendencia:** Visualización en gráficos lineales interactivos con gradientes premium bajo las curvas para el análisis temporal del progreso del paciente.

### 4. Asistente de Prevención e Inteligencia Artificial
* **Sugerencias de Salud:** Recomendaciones personalizadas según las métricas corporales del paciente, el clima local y alertas sanitarias en su región.
* **Recomendaciones Nutricionales:** Guías y alertas alimentarias adaptadas a las patologías registradas en la ficha clínica (ej. diabetes, hipertensión).

### 5. Recordatorios de Medicamentos y Citas
* **Alarmas Locales Exactas:** Sincronización con el sistema Android (`AlarmManager`) para programar recordatorios de medicamentos, los cuales suenan y notifican de forma exacta incluso con la app cerrada o el dispositivo en reposo.
* **Directorio y Agenda Médica:** Buscador integrado de doctores, clínicas y hospitales de la red pública con función de marcado telefónico nativo y reserva/cancelación de citas médicas.

### 6. Descarga de Ficha y Reporte Clínico en PDF
* Generación local de un reporte en formato PDF altamente formateado con el diseño institucional, que incluye el resumen completo del historial médico, últimas métricas, diagnósticos y el progreso general del paciente.

---

## Manual de Uso y Operación

### 1. Registro e Inicio de Sesión
1. **Creación de Cuenta:**
   * En la pantalla inicial, haz clic en **Registrarse**.
   * Introduce tu Nombre Completo, Correo Electrónico, Teléfono, CURP y establece una contraseña.
   * *Nota: Los requerimientos de seguridad y validaciones de datos (como el formato de CURP y fortaleza de contraseña) están estrictamente alineados con la versión de escritorio.*
2. **Inicio de Sesión:**
   * Introduce tu correo y contraseña registrados para ingresar. La aplicación cuenta con inicio de sesión automático persistente para que no debas reingresar tus credenciales en cada apertura.

### 2. Onboarding y Ficha Clínica (Primer Ingreso)
* Al ingresar por primera vez, se te redirigirá automáticamente a la pantalla de **Ficha Clínica**. El acceso al resto de las secciones estará bloqueado hasta que completes este registro inicial.
* **Campos Requeridos:** Fecha de nacimiento, estatura, peso inicial, tipo de sangre, alergias y antecedentes patológicos/heredofamiliares.
* Guarda los cambios para desbloquear el Dashboard principal.

### 3. Registro de Métricas y Consulta de Tendencias
1. **Añadir Métrica:**
   * Desde la pestaña del menú inferior, ve a la sección de **Registro** (ícono de suma `+`).
   * Selecciona el tipo de métrica (Glucosa, Presión, Peso o Pulso) e introduce los valores numéricos correspondientes.
2. **Consultar Tendencias:**
   * Ve a la sección de **Tendencias** para ver el histórico de los últimos días en gráficas dinámicas de Canvas. 
   * Podrás cambiar entre métricas usando los chips interactivos en la parte superior para visualizar las curvas de comportamiento y sus gradientes de color.

### 4. Operación del Módulo "Mi Familia"
1. **Buscar y Agregar un Familiar:**
   * Despliega el menú lateral (Drawer) y selecciona la pestaña **Mi Familia**.
   * Escribe el nombre del familiar en la barra de búsqueda superior y presiona **Buscar**.
   * Haz clic en el botón de agregar (`+`) en la tarjeta del familiar encontrado.
2. **Visualizar el Estado de Salud de un Familiar:**
   * En tu lista de familiares vinculados, presiona la tarjeta del familiar que deseas consultar.
   * Se abrirá una pantalla de detalle con:
     * Ficha médica del familiar (tipo de sangre, alergias, etc.).
     * Cuadrícula de últimas métricas con su respectivo color de alerta (Verde = Normal, Dorado = Advertencia, Rojo = Crítico).
3. **Desvincular Familiar:**
   * Puedes presionar el ícono de bote de basura en la tarjeta del familiar en tu lista para removerlo del grupo familiar.

### 5. Gestión de Medicamentos
1. **Agregar Recordatorio:**
   * Ve a la pestaña **Medicamentos**.
   * Escribe el nombre del fármaco, dosis, intervalo de horas (ej. cada 8 horas) y la hora inicial de la toma.
   * Presiona **Guardar**. El sistema programará de manera interna las notificaciones repetitivas locales correspondientes.
2. **Eliminar Recordatorio:**
   * Presiona el ícono de eliminar junto al medicamento. Esto cancelará de forma inmediata las alarmas en el sistema operativo y removerá el registro del servidor.

### 6. Citas y Directorio Médico
1. **Directorio:**
   * Busca médicos por especialidad o clínicas por nombre en el buscador del directorio.
   * Si necesitas contactarlos de urgencia, presiona el botón del teléfono para abrir el marcador nativo de llamadas de tu dispositivo.
2. **Reservar Cita:**
   * Selecciona al doctor, elige la clínica u hospital público más cercano, e ingresa la fecha y hora deseadas.
   * Al confirmar, la cita se agendará y sincronizará con la base de datos central.

---

## Requisitos de Desarrollo e Instalación

### Requisitos Previos
* **Android Studio:** Jellyfish / Ladybug (o posterior).
* **SDK de Android:** API 26 (Android 8.0) como mínimo, API 34 (Android 14) recomendada para compilación.
* **Java Development Kit (JDK):** Versión 17 o posterior.

### Configuración del Proyecto
1. **Servicios de Google (Firebase):**
   * Registra la aplicación móvil en tu consola de Firebase (`com.example.healthtrackmobile`).
   * Descarga el archivo `google-services.json` y colócalo en el directorio del módulo: `app/`.
2. **Compilación Local:**
   * Abre el proyecto en Android Studio.
   * Ejecuta el comando de Gradle para compilar y comprobar las dependencias:
     ```bash
     ./gradlew assembleDebug
     ```
3. **Ejecución:**
   * Conecta un dispositivo físico con depuración USB habilitada o inicia un Emulador Android (API 26+).
   * Presiona **Run** en Android Studio o ejecuta:
     ```bash
     ./gradlew installDebug
     ```
