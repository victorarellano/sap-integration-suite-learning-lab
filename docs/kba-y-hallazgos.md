# KBA y hallazgos del laboratorio

Este documento registra problemas y comportamientos detectados durante
el curso. No pretende reemplazar las SAP Knowledge Base Articles
oficiales; funciona como una base de conocimiento propia del
laboratorio.

## KBA-LAB-001 --- Acceso `/unauthorized` después de suscribirse a Integration Suite

**Estado:** Resuelto.

**Síntoma:** Integration Suite estaba suscrito, pero el acceso inicial
terminaba en una pantalla de autorización insuficiente.

**Diagnóstico aplicado:** se revisaron y asignaron las Role Collections
necesarias al usuario. Posteriormente se realizó una autenticación
nueva.

**Resultado:** el usuario pudo acceder a Integration Suite.

**Aprendizaje:** una suscripción activa no implica por sí sola que el
usuario tenga las autorizaciones necesarias.

------------------------------------------------------------------------

## KBA-LAB-002 --- Booster no utilizado durante la habilitación

**Estado:** Resuelto mediante configuración manual.

**Síntoma:** el flujo de habilitación basado en Booster no coincidía con
el entorno del laboratorio.

**Acción:** se habilitaron manualmente la suscripción, roles,
capabilities y servicios necesarios.

**Resultado:** Integration Suite y Cloud Integration quedaron operativos
en US East (VA), AWS.

**Aprendizaje:** no asumir que una limitación observada en un asistente
o tutorial implica que la región completa no soporte el servicio.
Verificar disponibilidad y resultado real del entorno.

------------------------------------------------------------------------

## KBA-LAB-003 --- `ErrorPersistingArtifact` al desplegar iFlows

**Estado:** No reproducido de forma estable / comportamiento transitorio
observado.

**Mensaje observado:**

``` text
[CONTENT][CONTENT_DEPLOY][ErrorPersistingArtifact]:
The artifact can't be deployed due to an internal issue.
Try again after some time.
```

**Pruebas realizadas:** se intentó desplegar un segundo iFlow mínimo.
Inicialmente presentó el mismo error. Audit Log confirmó que las
operaciones de deployment llegaban al backend. Posteriormente el iFlow
pudo pasar a `Started`.

**Conclusión del laboratorio:** no se confirmó un defecto del diseño ni
una incompatibilidad regional. El comportamiento desapareció
posteriormente.

**Aprendizaje:** cuando un error de plataforma no entrega causa
suficiente, aislar con un artefacto mínimo y evitar atribuir una causa
sin evidencia.

------------------------------------------------------------------------

## KBA-LAB-004 --- `401 Unauthorized` invocando el endpoint del iFlow

**Estado:** Resuelto.

**Síntoma:** el token OAuth se obtenía correctamente, pero el endpoint
devolvía `401 Unauthorized`.

**Validaciones realizadas:**

-   El token contenía autorización `ESBMessaging.send`.
-   Postman enviaba un único header `Authorization: Bearer ...`.
-   El HTTPS Sender estaba configurado con `Authorization = User Role`.
-   El `User Role` era `ESBMessaging.send`.
-   El endpoint correspondía al publicado por el runtime.

**Solución:** obtener un access token nuevo y utilizarlo en la petición.

**Causa práctica observada:** se estaba utilizando un token anterior/no
vigente.

**Aprendizaje:** ante un `401`, comprobar vigencia del token antes de
modificar scopes, roles o configuración del iFlow.

------------------------------------------------------------------------

## KBA-LAB-005 --- Exposición accidental de credenciales

**Estado:** Resuelto.

**Síntoma:** un `client_secret` quedó visible durante la configuración
del laboratorio.

**Acción:** la credencial fue tratada como comprometida y se rotó la
Service Key.

**Prevención:** no capturar ni subir a Git pantallas que contengan
secrets, tokens o service keys completas.

**Aprendizaje:** `client_id` identifica al cliente; `client_secret`
autentica al cliente y debe tratarse como secreto. Los access tokens
también deben ocultarse aunque sean temporales.

## KBA-LAB-006 — Diferencia entre Service Instance y runtime de Cloud Integration

**Origen:** Tema 00 — Arquitectura de SAP BTP y entorno Cloud Foundry
**Estado:** Resuelto — Aclaración conceptual
**Entorno:** SAP BTP Trial, AWS US East (VA), Cloud Foundry, SAP Integration Suite

### 1. Síntomas y observaciones

Durante el laboratorio se identificaron las siguientes condiciones:

- El space `dev` mostraba **0 aplicaciones iniciadas**.
- Cloud Integration mostraba **1 artefacto iniciado**.
- Existía una instancia llamada `cpi-trial-integration-flow`.
- La instancia utilizaba el servicio `SAP Process Integration Runtime`, plan `integration-flow`.
- La instancia tenía una Service Key asociada (`1 key`).
- La operación de creación de la instancia figuraba como `Creation Succeeded`.

Estas observaciones generaron una duda sobre la ubicación del motor que ejecuta los iFlows.

### 2. Diagnóstico

No se identificó un error de ejecución ni una contradicción entre Cloud Foundry y Cloud Integration.
Los contadores corresponden a recursos diferentes:

| Recurso | Responsabilidad |
|---|---|
| Cloud Foundry Space `dev` | Administra aplicaciones e instancias de servicios dentro del space. |
| Service Instance `cpi-trial-integration-flow` | Representa una instancia aprovisionada de SAP Process Integration Runtime, plan `integration-flow`. |
| Service Key | Proporciona información de conexión y credenciales según la configuración del servicio. |
| Cloud Integration Runtime | Ejecuta los iFlows desplegados y procesa sus mensajes. |

El nombre `SAP Process Integration Runtime` puede inducir a pensar que su instancia constituye el propio motor de ejecución de los iFlows dentro del space `dev`. Esta interpretación es incorrecta.

### 3. Explicación técnica

Cloud Integration proporciona un runtime gestionado por SAP.
La instancia del servicio `SAP Process Integration Runtime`, creada en Cloud Foundry, es un recurso técnico asociado al consumo del servicio.

Por tanto:

- La Service Instance no equivale al proceso que ejecuta físicamente los iFlows.
- Una Service Key no ejecuta integraciones; proporciona información técnica de acceso.
- Un iFlow iniciado en Cloud Integration no tiene que aparecer como aplicación iniciada en el space `dev`.
- La suscripción a Integration Suite y la instancia de SAP Process Integration Runtime son recursos distintos.

### 4. Solución y validación

No fue necesario modificar la configuración.
Se realizaron las siguientes comprobaciones:

1. Se verificó la existencia de la instancia en `Cloud Foundry → Spaces → dev → Services → Instances`.
2. Se confirmó el servicio, el plan y el estado `Creation Succeeded`.
3. Se comprobó la existencia de una Service Key, sin acceder a su contenido.
4. Se verificó la suscripción `Integration Suite`, plan `trial`, estado `Subscribed`.
5. Se accedió a `Integration Suite → Design` y `Monitor`.
6. Se confirmó la existencia del paquete `CPI Learning Lab` y de un artefacto iniciado.

**Resultado:** se validó que los contadores de aplicaciones Cloud Foundry y artefactos Cloud Integration representan recursos distintos.

### 5. Consideraciones de seguridad

La existencia de una Service Key no demuestra que sus credenciales hayan sido utilizadas correctamente en una llamada OAuth.
Para verificar esa relación sería necesario revisar la configuración de autenticación y las evidencias del laboratorio 03.

No se deben publicar:

- `client_secret`
- Access tokens
- Contraseñas
- Private keys
- Service keys completas

Si alguna credencial real queda expuesta, debe rotarse.

### 6. Alcance y limitaciones

Este hallazgo aplica a la interpretación de recursos de Cloud Foundry y Cloud Integration en el entorno estudiado.
Las capturas permiten confirmar la existencia y el estado visible de los recursos, pero no permiten determinar la topología física interna del runtime gestionado por SAP.
No se identificó una falla atribuible a SAP, a Cloud Foundry ni a la región utilizada.

### 7. Aprendizaje reutilizable

**Una Service Instance de SAP Process Integration Runtime aprovisionada en Cloud Foundry no significa que el motor de ejecución de los iFlows esté desplegado dentro del mismo space.**

La instancia representa un recurso del servicio, mientras que Cloud Integration proporciona el runtime gestionado por SAP donde se ejecutan los iFlows.

### Referencias

- [SAP Business Technology Platform — SAP Help Portal](https://help.sap.com/docs/btp)
- [SAP Integration Suite — SAP Help Portal](https://help.sap.com/docs/integration-suite)

**Relacionado con:** Tema 00 — Arquitectura de SAP BTP y entorno Cloud Foundry; Tema 03 — Primer iFlow HTTP + OAuth Client Credentials.
