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


## KBA — `Premature end of file` al ejecutar Message Mapping por HTTPS

**Contexto:** Laboratorio 07.2, iFlow `Empleado Mapping HTTP`, mapping `MM_Empleado_Trabajador`.

**Síntoma:** POST desde Postman con `500 Internal Server Error`. En Monitor Message Processing, fallo en `Map Empleado to Trabajador` y mensaje `Premature end of file`.

**Diagnóstico:** se revisó el contenido de la solicitud y se detectó que no se había incluido el Body XML requerido. El error es compatible con entrada vacía o incompleta; no demuestra por sí solo un problema del XSD.

**Solución verificada:** configurar Postman con Body → raw → XML, enviar un documento `Empleado` válido y utilizar `Content-Type: application/xml`. La nueva respuesta contenía `Trabajador/Codigo=1001` y `Trabajador/NombreCompleto=Victor Arellano`.

**Alcance:** confirmado para esta ejecución del laboratorio. No generalizar como causa única de cualquier error `Premature end of file`.

**Evidencias:** `docs/images/tema-07/07-2-postman-error-http-500.png` y `docs/images/tema-07/07-2-postman-transformacion-correcta.png`.


## KBA-LAB-007 — `java.lang.String.call()` en Groovy MarkupBuilder

**Origen:** Laboratorio 08.2 — Content Enricher y Request-Reply.
**Estado:** Resuelto y validado con Postman.

**Síntoma:** el iFlow `Request Reply` se encontraba desplegado, pero el procesamiento del mensaje falló. En Monitor Message Processing se observó:

```text
No signature of method: java.lang.String.call() is applicable for argument types: (java.lang.String) values: [SOL-1001]
```

**Diagnóstico:** en `GRV_Enriquecer_Solicitud.groovy`, las variables locales `solicitudId`, `clienteId` y `origen` compartían nombres con los nodos que se pretendía crear mediante `MarkupBuilder`, por ejemplo `solicitudId(solicitudId)`. La coincidencia producía la invocación errónea de un `String` como función.

**Solución:** renombrar las variables a `vSolicitudId`, `vClienteId`, `vOrigen` y actualizar todas las referencias, conservando los nombres XML de salida.

**Validación:** nueva ejecución POST con JSON `Solicitud`, HTTP `200 OK` y respuesta `SolicitudEnriquecida` que contiene `SOL-1001`, `1001`, `POSTMAN`, `Ana`, `PREMIUM`.

**Alcance:** confirmado para el script y mensaje de este laboratorio; otros errores `String.call()` pueden tener causas diferentes.

**Evidencia:** `docs/images/tema-08/08-2-postman-enriquecimiento-200.png`.


---

## KBA-LAB-008 — Errores de compatibilidad de Groovy Script v2 y procesamiento XML

**Origen:** Laboratorio 08.2 — Content Enricher y Request-Reply.
**Artefacto:** `Request Reply`.
**Recurso:** `GRV_Enriquecer_Solicitud.groovy`.
**Estado:** Resuelto y validado con Postman (21-10-2026).

### Contexto

El iFlow recibe un JSON de solicitud por HTTPS, lo convierte a XML, conserva la solicitud original en la Exchange Property `SolicitudOriginal`, consulta los datos del cliente mediante Request Reply y construye una respuesta XML enriquecida con Groovy.

### Síntomas

1. El editor mostró una advertencia de procesamiento XML sin streaming al usar `message.getBody(String)` y `XmlSlurper.parseText(...)` para el body de la respuesta.
2. Tras modificar el código y utilizar el recurso Groovy Script v2, el monitor registró un error de compilación: `unable to resolve class groovy.util.XmlSlurper`.
3. En ejecuciones posteriores, el monitor registró otro error de compilación: `unable to resolve class com.sap.gateway.ip.core.customdev.util.Message`.
4. Las ejecuciones fallidas se reflejaron en *Monitor Message Processing*, aunque el artefacto había sido desplegado. En una captura posterior, la versión 1.0.2 figuraba temporalmente como `Starting`; ese estado por sí solo no confirma el resultado de ejecución.

### Diagnóstico

Las comprobaciones de compatibilidad del editor identificaron imports no compatibles con el recurso Groovy Script v2, asociado al runtime Groovy 4. En las versiones fallidas del código se utilizaron imports de la API anterior:

```groovy
import com.sap.gateway.ip.core.customdev.util.Message
import groovy.util.XmlSlurper
```

La advertencia de streaming era un problema diferente de los errores de import: recomendaba evitar la conversión previa del body completo a `String` para analizarlo con `XmlSlurper`.

**Límite del diagnóstico:** las capturas no permiten determinar por qué una ejecución seguía mostrando un import anterior mientras se estaban realizando correcciones. No se atribuye esa discrepancia a una causa específica del runtime o del despliegue.

### Solución aplicada

Se utilizaron los imports compatibles con el recurso Groovy Script v2 del laboratorio:

```groovy
import com.sap.it.script.v2.api.Message
import groovy.xml.MarkupBuilder
import groovy.xml.XmlSlurper
```

Se mantuvo el método de entrada:

```groovy
def Message processData(Message message) {
    // Procesamiento de la solicitud y de la respuesta del servicio
    return message
}
```

Para analizar el body XML recibido desde Request Reply se utilizó un `Reader`:

```groovy
def clientes = new XmlSlurper().parse(
    message.getBody(java.io.Reader)
)
```

La solicitud original, guardada previamente como propiedad, se recuperó y analizó por separado:

```groovy
String originalXml = message.getProperty("SolicitudOriginal") as String

def solicitud = new XmlSlurper().parseText(originalXml)
```

**Por qué:** el `Reader` permite entregar el body al parser sin convertirlo previamente en una cadena. La propiedad `SolicitudOriginal` conserva el contenido anterior a la llamada Request Reply, cuyo resultado pasa a ocupar el body del mensaje.

Después de las correcciones se volvió a cargar el recurso Groovy y se desplegó la versión 1.0.2 del iFlow.

### Validación práctica

- El editor mostró `No data` en *Upgrade Readiness Checks* después de actualizar los imports.
- Se observó el despliegue de la versión `1.0.2` del artefacto `Request Reply`.
- Una nueva solicitud POST desde Postman devolvió **HTTP 200 OK** y un XML enriquecido.

**Solicitud de validación:**

```json
{
  "Solicitud": {
    "solicitudId": "SOL-1002",
    "clienteId": "1001",
    "origen": "POSTMAN"
  }
}
```

**Respuesta observada:**

```xml
<SolicitudEnriquecida>
    <solicitudId>SOL-1002</solicitudId>
    <clienteId>1001</clienteId>
    <origen>POSTMAN</origen>
    <Cliente>
        <nombre>Ana</nombre>
        <segmento>PREMIUM</segmento>
    </Cliente>
</SolicitudEnriquecida>
```

La respuesta valida el recorrido funcional de la solicitud y la construcción del XML enriquecido. No se afirma que la captura de estado `Starting` demuestre por sí misma un estado final `Started`.

### Aprendizajes reutilizables

- La versión del paso Groovy Script condiciona las APIs e imports disponibles; no deben mezclarse imports de versiones distintas sin comprobar su compatibilidad.
- En este recurso Groovy Script v2 se utilizó `com.sap.it.script.v2.api.Message` y `groovy.xml.XmlSlurper`.
- La lectura del body con `java.io.Reader` evita una conversión previa innecesaria a `String`.
- `Started` o un despliegue iniciado no sustituyen una prueba de procesamiento real; la evidencia funcional fue el HTTP 200 y el XML esperado.
- Los errores del monitor deben conservarse como evidencia diagnóstica, sin atribuir causas que no estén demostradas.

**Alcance:** confirmado para el artefacto `Request Reply` y el recurso Groovy Script v2 utilizados en el laboratorio 08.2. No implica que los imports anteriores sean inválidos en todos los entornos o versiones.

**Evidencia:** capturas del editor Groovy, *Upgrade Readiness Checks*, *Monitor Message Processing*, despliegue de la versión 1.0.2 y respuesta HTTP 200 en Postman del laboratorio 08.2. Las rutas de estas capturas no se especifican porque no se ha confirmado su nombre definitivo en el repositorio.

**Relacionado con:** KBA-LAB-007 — `java.lang.String.call()` en Groovy MarkupBuilder; Laboratorio 08.2 — Content Enricher y Request-Reply.

## KBA-LAB-009 — 502 Bad Gateway por header Content/Type mal formado

### Hallazgo — Router no compatible con Gather en el diseño probado

**Fecha:** 2026-10-23
**Módulo:** 10.3 — Gather
**Estado:** Solución validada en laboratorio

### Síntoma

El validador del iFlow `CPI_LAB_10_Splitter_Gather` mostró:

`You cannot use Router with gather; use gather after splitter or join`

### Contexto

Se intentó conectar las salidas de un Router, tras sendos Content Modifiers, a un Gather común para agregar los mensajes generados por un General Splitter.

### Diagnóstico

El validador rechazó la topología Router → Gather utilizada en este laboratorio. El mensaje no demuestra que todas las combinaciones posibles de Router y Gather sean incompatibles.

### Solución validada

Se conservó la versión anterior del trabajo con Router y se sustituyó el diseño activo por:

`HTTPS Sender → Start → General Splitter → Gather → End`

Gather se configuró con `XML (Same Format)` y `Combine`. Tras desplegar, Postman devolvió `200 OK` y un XML `multimap:Messages` con los tres pedidos; Trace confirmó el payload consolidado.

### Alcance

Hallazgo observado en el editor y runtime del tenant utilizado para el laboratorio 10.3. No se ha evaluado la alternativa con Join ni otras topologías.

### Evidencias

Consultar `10-16-gather-router-incompatible.png`, `10-17-gather-arquitectura-corregida.png`, `10-18-gather-postman-resultado.png` y `10-19-gather-trace-consolidacion.png` en el documento del módulo 10.


## KBA-LAB-010 — 502 Bad Gateway por header Content/Type mal formado

**Origen:** Tema 11.4 — `LAB11_04_JSON_XML_Roundtrip`
**Estado:** Resuelto y validado (26-10-2026).

**Síntoma:** Postman devolvía `502 Bad Gateway`, mientras Monitor Message Processing registraba el mensaje como `Completed`.

**Diagnóstico:** el header de respuesta `X-Cf-Routererror` contenía `malformed MIME header line: "Content/Type: application/json; charset=UTF-8"`. La configuración del Content Modifier usaba `Content/Type` en lugar de `Content-Type`.

**Solución:** corregir el nombre del Message Header a `Content-Type`, conservando el valor `application/json; charset=UTF-8`, y volver a probar el endpoint.

**Validación:** Postman devolvió `200 OK` y `4/4` pruebas aprobadas. El JSON resultante conservó la estructura, aunque algunos valores numéricos pasaron a cadenas.

**Aprendizaje:** el procesamiento interno `Completed` no garantiza la entrega correcta de la respuesta HTTP. Revisar los headers y los mensajes del router ante errores 502.

**Alcance:** hallazgo confirmado en el laboratorio 11.4; no implica un defecto general del runtime ni de la región.


## KBA-LAB-011 — Header HTTP personalizado no disponible en Groovy sin Allowed Headers

**Origen:** Laboratorio 12.2 — Headers, Properties y Body.
**Estado:** Resuelto y validado mediante Postman (27-10-2026).
**Artefacto:** `LAB12_02_Groovy_Headers_Properties_Body`.

**Síntoma:** Postman enviaba el header `X-Origen: Postman`, pero la respuesta del iFlow mostraba `"origen": "No informado"`. El resto de la transformación funcionaba y la solicitud devolvía HTTP 200.

**Diagnóstico:** el script leía `message.getHeader("X-Origen", String)` y aplicaba el valor alternativo cuando el header no estaba disponible. Se revisó la configuración de **Integration Flow → Runtime Configuration → Allowed Header(s)**.

**Solución verificada:** incorporar `X-Origen` a **Allowed Header(s)**, aplicar la configuración y volver a ejecutar la petición. La respuesta posterior devolvió `"origen": "Postman"` y 4/4 pruebas Postman exitosas.

**Alcance:** validado en este iFlow y en el entorno de laboratorio. No se afirma que sea la única causa posible de headers ausentes en otros escenarios.

**Aprendizaje:** para consumir headers HTTP personalizados en el procesamiento del iFlow, revisar la lista de headers permitidos y comprobar su recepción con una aserción funcional.

**Evidencia:** `docs/images/tema-12/12-21-postman-headers-properties-body-validado.png` y `docs/images/tema-12/12-22-postman-tests-headers-properties-body.png`.

## KBA-LAB-012 — Error de sintaxis Groovy no advertido en el editor antes del despliegue

**Origen:** Capítulo 13, laboratorio 13.4 — Errores funcionales y técnicos.
**Artefacto:** `LAB13_01_Error_No_Controlado`.
**Recurso:** `GRV_ProvocarError_v2.groovy`.
**Estado:** Resuelto y validado mediante Postman (29-10-2026).

### Síntoma

El editor del iFlow no mostró una advertencia visible sobre una instrucción Groovy incompleta. El iFlow pudo desplegarse y alcanzó `Started`. Al ejecutar una solicitud funcional desde Postman, se activó el Exception Subprocess y se devolvió HTTP 500 en lugar del HTTP 400 esperado.

### Código observado

**Antes — instrucción incorrecta:**

```groovy
def body = new JsonSlurper().parse(message.getBody(java.io.Reader)
```

**Después — instrucción corregida:**

```groovy
def body = new JsonSlurper().parse(message.getBody(java.io.Reader))
```

La instrucción original carecía de un paréntesis de cierre. El compilador señaló la línea siguiente, al encontrar `if` donde esperaba completar la expresión anterior.

### Diagnóstico

En **Monitor Message Processing**, el detalle de ejecución del paso Groovy mostró:

```text
MultipleCompilationErrorsException: startup failed
GRV_ProvocarError_v2__Script.groovy: 9:
unexpected token: if @ line 9, column 5.
```

El fallo se produjo al compilar el script para procesar la solicitud. El Exception Subprocess capturó el error y construyó la respuesta técnica genérica HTTP 500, conforme a su configuración.

### Corrección y validación

Se cerró correctamente la llamada `parse(...)` y se ejecutaron nuevamente las peticiones de Postman. Los resultados observados fueron:

| Escenario | HTTP | Tests Postman |
|---|---|---|
| Solicitud válida | 200 | 4/4 |
| Error funcional | 400 | 7/7 |
| Error técnico | 500 | 4/4 |

### Aprendizaje y alcance

En esta ejecución, el editor no señaló un error sintáctico que luego fue identificado durante la compilación del script en tiempo de procesamiento. **El estado `Started` no garantiza que todos los scripts Groovy puedan ejecutarse sin errores de compilación.** Ante un HTTP inesperado, consultar el MPL y revisar también la instrucción anterior a la línea indicada por el compilador.

El hallazgo describe exclusivamente el comportamiento observado en este laboratorio; no demuestra que el editor de SAP Cloud Integration omita sistemáticamente estas validaciones.
