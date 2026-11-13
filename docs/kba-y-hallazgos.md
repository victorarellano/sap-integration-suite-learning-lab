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

## KBA-LAB-006 - Diferencia entre Service Instance y runtime de Cloud Integration

**Origen:** Tema 00 - Arquitectura de SAP BTP y entorno Cloud Foundry
**Estado:** Resuelto - Aclaración conceptual
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

- [SAP Business Technology Platform - SAP Help Portal](https://help.sap.com/docs/btp)
- [SAP Integration Suite - SAP Help Portal](https://help.sap.com/docs/integration-suite)

**Relacionado con:** Tema 00 - Arquitectura de SAP BTP y entorno Cloud Foundry; Tema 03 - Primer iFlow HTTP + OAuth Client Credentials.


## KBA - `Premature end of file` al ejecutar Message Mapping por HTTPS

**Contexto:** Laboratorio 07.2, iFlow `Empleado Mapping HTTP`, mapping `MM_Empleado_Trabajador`.

**Síntoma:** POST desde Postman con `500 Internal Server Error`. En Monitor Message Processing, fallo en `Map Empleado to Trabajador` y mensaje `Premature end of file`.

**Diagnóstico:** se revisó el contenido de la solicitud y se detectó que no se había incluido el Body XML requerido. El error es compatible con entrada vacía o incompleta; no demuestra por sí solo un problema del XSD.

**Solución verificada:** configurar Postman con Body → raw → XML, enviar un documento `Empleado` válido y utilizar `Content-Type: application/xml`. La nueva respuesta contenía `Trabajador/Codigo=1001` y `Trabajador/NombreCompleto=Victor Arellano`.

**Alcance:** confirmado para esta ejecución del laboratorio. No generalizar como causa única de cualquier error `Premature end of file`.

**Evidencias:** `docs/images/tema-07/07-2-postman-error-http-500.png` y `docs/images/tema-07/07-2-postman-transformacion-correcta.png`.


## KBA-LAB-007 - `java.lang.String.call()` en Groovy MarkupBuilder

**Origen:** Laboratorio 08.2 - Content Enricher y Request-Reply.
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

## KBA-LAB-008 - Errores de compatibilidad de Groovy Script v2 y procesamiento XML

**Origen:** Laboratorio 08.2 - Content Enricher y Request-Reply.
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

**Relacionado con:** KBA-LAB-007 - `java.lang.String.call()` en Groovy MarkupBuilder; Laboratorio 08.2 - Content Enricher y Request-Reply.

## KBA-LAB-009 - 502 Bad Gateway por header Content/Type mal formado

### Hallazgo - Router no compatible con Gather en el diseño probado

**Fecha:** 2026-10-23
**Módulo:** 10.3 - Gather
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


## KBA-LAB-010 - 502 Bad Gateway por header Content/Type mal formado

**Origen:** Tema 11.4 - `LAB11_04_JSON_XML_Roundtrip`
**Estado:** Resuelto y validado (26-10-2026).

**Síntoma:** Postman devolvía `502 Bad Gateway`, mientras Monitor Message Processing registraba el mensaje como `Completed`.

**Diagnóstico:** el header de respuesta `X-Cf-Routererror` contenía `malformed MIME header line: "Content/Type: application/json; charset=UTF-8"`. La configuración del Content Modifier usaba `Content/Type` en lugar de `Content-Type`.

**Solución:** corregir el nombre del Message Header a `Content-Type`, conservando el valor `application/json; charset=UTF-8`, y volver a probar el endpoint.

**Validación:** Postman devolvió `200 OK` y `4/4` pruebas aprobadas. El JSON resultante conservó la estructura, aunque algunos valores numéricos pasaron a cadenas.

**Aprendizaje:** el procesamiento interno `Completed` no garantiza la entrega correcta de la respuesta HTTP. Revisar los headers y los mensajes del router ante errores 502.

**Alcance:** hallazgo confirmado en el laboratorio 11.4; no implica un defecto general del runtime ni de la región.


## KBA-LAB-011 - Header HTTP personalizado no disponible en Groovy sin Allowed Headers

**Origen:** Laboratorio 12.2 - Headers, Properties y Body.
**Estado:** Resuelto y validado mediante Postman (27-10-2026).
**Artefacto:** `LAB12_02_Groovy_Headers_Properties_Body`.

**Síntoma:** Postman enviaba el header `X-Origen: Postman`, pero la respuesta del iFlow mostraba `"origen": "No informado"`. El resto de la transformación funcionaba y la solicitud devolvía HTTP 200.

**Diagnóstico:** el script leía `message.getHeader("X-Origen", String)` y aplicaba el valor alternativo cuando el header no estaba disponible. Se revisó la configuración de **Integration Flow → Runtime Configuration → Allowed Header(s)**.

**Solución verificada:** incorporar `X-Origen` a **Allowed Header(s)**, aplicar la configuración y volver a ejecutar la petición. La respuesta posterior devolvió `"origen": "Postman"` y 4/4 pruebas Postman exitosas.

**Alcance:** validado en este iFlow y en el entorno de laboratorio. No se afirma que sea la única causa posible de headers ausentes en otros escenarios.

**Aprendizaje:** para consumir headers HTTP personalizados en el procesamiento del iFlow, revisar la lista de headers permitidos y comprobar su recepción con una aserción funcional.

**Evidencia:** `docs/images/tema-12/12-21-postman-headers-properties-body-validado.png` y `docs/images/tema-12/12-22-postman-tests-headers-properties-body.png`.

## KBA-LAB-012 - Error de sintaxis Groovy no advertido en el editor antes del despliegue

**Origen:** Capítulo 13, laboratorio 13.4 - Errores funcionales y técnicos.
**Artefacto:** `LAB13_01_Error_No_Controlado`.
**Recurso:** `GRV_ProvocarError_v2.groovy`.
**Estado:** Resuelto y validado mediante Postman (29-10-2026).

### Síntoma

El editor del iFlow no mostró una advertencia visible sobre una instrucción Groovy incompleta. El iFlow pudo desplegarse y alcanzó `Started`. Al ejecutar una solicitud funcional desde Postman, se activó el Exception Subprocess y se devolvió HTTP 500 en lugar del HTTP 400 esperado.

### Código observado

**Antes - instrucción incorrecta:**

```groovy
def body = new JsonSlurper().parse(message.getBody(java.io.Reader)
```

**Después - instrucción corregida:**

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

### Where-Used no mostró una referencia existente de User Credentials (capítulo 15)

**Contexto:** iFlow `LAB15_03_Basic_Authentication`, HTTP Receiver con `Authentication = Basic` y `Credential Name = BASIC_LAB15_HTTPBIN`.

**Síntoma:** `Where-Used` de `BASIC_LAB15_HTTPBIN` mostró `Artifacts (0)` / `No references found`.

**Evidencia de contraste:** el HTTP Receiver referenciaba el alias (15-23) y una ejecución con credenciales correctas devolvió HTTP 200 desde HTTPBin (15-25). En otro material, `OAUTH2_Consulta_Clientes`, Where-Used sí mostró una referencia (15-04).

**Diagnóstico:** discrepancia observada entre la vista de dependencias y el uso comprobado. No se determinó su causa; no atribuirla a SAP ni asumir que Where-Used excluye sistemáticamente referencias de Basic Authentication.

**Precaución:** no utilizar Where-Used como única verificación antes de cambiar o eliminar materiales. Revisar también los iFlows consumidores conocidos y su configuración.

**Estado:** causa pendiente de confirmar.

## KBA - Error HTTP 404 por configuración incorrecta de Token Service URL en OAuth2 Client Credentials

**Componente:** SAP Integration Suite - Cloud Integration
**Ámbito:** HTTP Receiver Adapter / OAuth2 Client Credentials / Security Material
**Laboratorio relacionado:** Tema 15 - Seguridad
**Estado:** Resuelto y validado

### 1. Síntoma

Durante la ejecución del iFlow `Request Reply`, que consume el endpoint de otro iFlow (`Consulta Clientes`) mediante un HTTP Receiver configurado con OAuth2 Client Credentials, el procesamiento finalizaba con error.

En el monitor de mensajes se observaba:

```text
Status code: 404
Reason: 404 Not Found
Requested route (...) does not exist.
```

El iFlow receptor `Consulta Clientes` estaba desplegado y en estado `Started`. Su endpoint respondía correctamente cuando se invocaba directamente desde Postman con autenticación válida.

La prueba de conectividad TLS al host del runtime también resultaba satisfactoria.

### 2. Causa identificada

El Security Material `OAUTH2_Consulta_Clientes`, de tipo **OAuth2 Client Credentials**, tenía configurado incorrectamente el campo `Token Service URL`.

Se había copiado el valor de `oauth.url` de la Service Key de SAP BTP, en lugar del valor de `oauth.tokenurl`.

Ambos campos tienen propósitos diferentes:

| Campo de Service Key | Propósito |
|---|---|
| `oauth.url` | URL base del servicio de integración |
| `oauth.tokenurl` | Endpoint del servicio de autorización para obtener tokens OAuth2 |

La configuración incorrecta hacía que la solicitud de obtención del token utilizara una dirección que no correspondía al endpoint OAuth2.

### 3. Solución aplicada

1. Acceder a **Monitor → Security Material**.
2. Editar el material `OAUTH2_Consulta_Clientes`.
3. Localizar el campo `Token Service URL`.
4. Sustituir el valor procedente de `oauth.url` por el valor completo de `oauth.tokenurl` obtenido de la Service Key correspondiente.
5. Desplegar nuevamente el Security Material.
6. Ejecutar la petición de prueba desde Postman.

No fue necesario modificar el Client ID ni el Client Secret para resolver esta incidencia.

### 4. Validación

Tras corregir `Token Service URL`, la petición POST al endpoint `/http/lab08/request-reply` se ejecutó correctamente.

La respuesta obtenida fue:

```xml
<SolicitudEnriquecida>
    <solicitudId>SOL-1001</solicitudId>
    <clienteId>1001</clienteId>
    <origen>POSTMAN</origen>
    <cliente>
        <nombre>Ana</nombre>
        <segmento>PREMIUM</segmento>
    </cliente>
</SolicitudEnriquecida>
```

Se confirmó el funcionamiento del flujo de integración, incluida la consulta autenticada al servicio receptor y el enriquecimiento de la solicitud.

### 5. Aprendizajes reutilizables

- No confundir `oauth.url` con `oauth.tokenurl` al utilizar las credenciales de una Service Key.
- Para configurar `Token Service URL`, utilizar el valor exacto de `oauth.tokenurl`.
- Una prueba TLS satisfactoria verifica conectividad y aspectos del certificado, pero no valida la configuración OAuth2.
- Ante errores de un HTTP Receiver con OAuth2 Client Credentials, revisar tanto la dirección del receptor como la configuración del servicio de obtención del token.
- Un error HTTP 404 no demuestra por sí solo que el endpoint del iFlow receptor esté mal configurado; es necesario identificar qué solicitud está fallando.

### 6. Alcance y limitaciones

Este hallazgo fue validado en el escenario descrito, utilizando dos iFlows de SAP Cloud Integration y autenticación OAuth2 Client Credentials.

No debe generalizarse que todos los errores HTTP 404 en este adaptador tienen la misma causa.

### 7. Referencia de evidencia

- Prueba funcional satisfactoria desde Postman después de corregir la URL OAuth2.
- Archivo de evidencia: `15-29-postman-request-reply-oauth-corregido.png`.

**Nota de seguridad:** no incorporar capturas de Service Keys con secretos visibles ni documentar valores reales de `clientsecret`.


## KBA - Alcance de Connectivity Tests TLS y certificado de cliente (15.5–15.6)

**Síntoma:** la prueba TLS a `httpbin.org:443` con alias `lab15_mtls_client` informó `client certificate was not used`. En `certauth.cryptomix.com:443`, la prueba con alias informó conexión exitosa y `Client Certificate Alias: lab15_mtls_client`; la prueba TLS sin alias también tuvo éxito. Un navegador sin certificado recibió `No TLS client certificate presented`.

**Diagnóstico:** las pruebas observadas no son equivalentes. La conectividad TLS puede resultar satisfactoria sin demostrar autorización HTTP de la aplicación. La selección de alias tampoco garantiza que todos los destinos soliciten certificado cliente. No se confirmó un problema de caché ni de sesiones del runtime.

**Criterio de validación:** seleccionar un destino compatible, activar `Authenticate with Client Certificate`, elegir el Key Pair y comprobar la salida de Connectivity Tests. Para demostrar autorización HTTP se necesitaría una prueba HTTP específica, no realizada en este laboratorio.

**Alcance:** configuración del Key Pair y conectividad TLS con alias informado; no autorización HTTP de CPI mediante mTLS. No generalizar a otros destinos.

**Evidencias:** 15-35 a 15-38 del capítulo 15.

## KBA-LAB-016 - Diferencia entre nombre y valor predeterminado al externalizar HTTPS Sender

**Origen:** capítulo 16, laboratorio 16.4.
**Artefacto:** `LAB16-Externalized Parameters`.
**Estado:** configuración corregida y endpoint validado mediante Postman.

### Síntoma y contexto

Al externalizar **HTTPS Sender → Connection → Address**, la interfaz mostró un campo para el identificador `{{P_HTTP_ADDRESS}}` y otro para el valor predeterminado. Durante la edición se observó una combinación de dirección base y sufijo no deseado en la vista Configure.

### Diagnóstico

Las capturas mostraron que el nombre del parámetro y su valor predeterminado son entradas distintas. El valor deseado del endpoint inicial era `/lab16/parameters`; no debía agregarse un sufijo adicional al predeterminado.

### Corrección

Se conservó el identificador `P_HTTP_ADDRESS` y se estableció el valor predeterminado `/lab16/parameters`. En Configure se verificó la dirección completa y posteriormente se modificó el valor a `/lab16/parameters-v2`.

### Validación

La petición GET a `/http/lab16/parameters-v2` devolvió HTTP `200 OK` y el JSON esperado con `"ambiente": "PRD"`.

### Alcance y aprendizaje

En este laboratorio, la distinción entre **identificador** y **valor** fue esencial para evitar una dirección publicada incorrecta. No se atribuye el comportamiento a un defecto de SAP ni se generaliza a otros adaptadores sin pruebas.

## KBA - Data Store: entrada existente no accesible desde un segundo iFlow con visibilidad Global

**Origen:** Capítulo 17, laboratorio 17.2.
**Estado:** Diagnóstico y alternativa de configuración validados (04/11/2026).

### Síntomas

- El iFlow lector `LAB17-Data Store Get`, con `Visibility = Global`, `Data Store Name = LAB17_ORDENES` y `Entry ID = ORD-17001`, devolvió HTTP 500.
- El MPL informó: `No message found for id ORD-17001 in store LAB17_ORDENES.`
- Monitor mostraba `ORD-17001` en el almacenamiento asociado a `LAB17-Data_Store`.
- Una nueva escritura de `ORD-17001`, con sobrescritura desactivada, falló con `An entry with id ORD-17001 does already exist in data store LAB17_ORDENES with qualifier LAB17-Data_Store.`

### Diagnóstico

Monitor permitió distinguir un almacén `LAB17_ORDENES` asociado al iFlow escritor y otro identificado como `Global`. La entrada inicial había sido escrita con visibilidad `Integration Flow`, mientras que el lector solicitaba visibilidad `Global`.

**Límite:** las pruebas no demuestran una migración automática de entradas al cambiar la configuración de visibilidad. Tampoco permiten generalizar el comportamiento a todos los escenarios de persistencia de SAP.

### Solución y validación

1. Crear una nueva versión del iFlow escritor y configurar `Visibility = Global` con un nuevo identificador `ORD-17002` tanto en Data Store Write como en el JSON generado.
2. Desplegar y ejecutar POST: HTTP 200.
3. Confirmar en Monitor que el payload `ORD-17002` está en el almacén Global.
4. Versionar el iFlow lector y cambiar `Entry ID` a `ORD-17002`, manteniendo `Visibility = Global` y `Delete On Completion` desactivado.
5. Ejecutar GET: HTTP 200 y JSON esperado.

### Aprendizaje y alcance

El nombre del Data Store no basta para identificar el ámbito de lectura: comprobar también la visibilidad y la ubicación real de la entrada en Monitor. Para probar comunicación entre iFlows, escribir y leer explícitamente en el ámbito global. No confundir la respuesta HTTP 200 con evidencia suficiente de persistencia: verificar el payload en Manage Data Stores.

**Evidencias:** `17-12-mpl-get-entrada-no-encontrada.png`, `17-13-monitor-data-store-entrada-existente.png`, `17-14-mpl-write-entrada-duplicada.png`, `17-17-monitor-data-store-global-orden-17002.png` y `17-18-postman-data-store-get-global-orden-17002.png`, referenciadas en el capítulo 17.


## KBA - Data Store Select: error de parseo XML al seleccionar una entrada JSON (17.3)

**Ámbito:** SAP Cloud Integration, Data Store Select.
**Estado:** escenario corregido y validado el 05/11/2026.

### Síntoma

La petición GET de `17.3 - Data Store Select` devolvió HTTP 500. En el MPL, el paso `DS_Select_Ordenes` registró:

```text
Unexpected character '{' (code 123) in prolog; expected '<'
at [row,col {unknown-source}]: [1,1]
```

El mismo MPL identificó el almacén `LAB17_ORDENES` y la entrada `ORD-17002`. En Monitor se verificó que el payload de esa entrada era JSON y comenzaba por `{`.

### Diagnóstico

El error demuestra que durante la operación Select se intentó interpretar como XML un contenido que empezaba por `{`, propio del JSON almacenado. La lectura individual mediante Data Store Get había funcionado con esa entrada en 17.2; por tanto, no corresponde concluir que el almacenamiento o la lectura de JSON estuvieran defectuosos.

**Límite:** no se verificó si existe otra configuración o transformación que permita utilizar Select con JSON. El resultado describe el escenario probado, no una prohibición universal del componente.

### Solución aplicada

1. Preparar un payload XML de prueba con ID `ORD-17003` mediante `CM_Preparar_Mensaje`.
2. Almacenar la entrada en un Data Store global separado, `LAB17_ORDENES_XML`, evitando mezclarla con entradas JSON.
3. Apuntar `DS_Select_Ordenes` al mismo almacén y mantener `Number of Polled Messages = 1`, `Delete On Completion = off`.
4. Ejecutar GET: HTTP 200 y XML `<messages><message id="ORD-17003">...</message></messages>`.
5. Incorporar una ruta independiente de Delete, comprobar HTTP 200 y verificar que la consulta posterior devuelve `<messages/>`.

### Aprendizaje reutilizable

Ante un error de parseo en Select, revisar el payload real de la entrada identificada por el MPL, no solamente la configuración del paso. Separar datos de prueba por formato permitió validar la operación sin alterar los registros JSON del laboratorio anterior.

**Evidencias:** MPL de error revisado durante 17.3; `17-37-postman-router-select-exitoso.png`, `17-38-postman-data-store-delete-exitoso.png`, `17-39-monitor-data-store-xml-eliminado.png`, `17-40-postman-select-data-store-vacio.png`.

---

## KBA - Restricción entre umbral de alerta y expiración en Data Store Write (17.4)

**Ámbito:** SAP Cloud Integration, Data Store Write.
**Estado:** configuración corregida y fechas validadas el 05/11/2026.

### Síntoma

Al configurar `Retention Threshold for Alerting = 1` y `Expiration Period = 1`, el editor mostró:

```text
'Expiration Period' should be greater than 'Retention Threshold for Alerting'
```

### Diagnóstico

La validación de la interfaz exige que `Expiration Period` sea estrictamente mayor que `Retention Threshold for Alerting` en la configuración ensayada. No se desplegó la combinación inválida.

### Solución y validación

Se conservó el umbral de 1 día y se cambió la expiración a 2 días. En la versión `1.0.6` del iFlow escritor se creó `ORD-17005` en `LAB17_EXPIRACION`. Monitor registró `Due At` un día después de `Created At` y `Retain Until` dos días después. La entrada previa `ORD-17004`, creada con 2/30 días, conservó sus fechas originales.

### Alcance

Se validaron las fechas calculadas y la coexistencia de entradas con configuraciones distintas. **No se ha verificado todavía la eliminación automática por expiración**; no debe declararse completada sin observación posterior.

**Evidencias:** `17-44-monitor-data-store-fechas-retencion-expiracion.png` y `17-45-monitor-comparacion-retencion-expiracion.png`.

## KBA - Data Store de negocio e historial interno de Idempotent Process Call

**Origen:** Capítulo 17.5 - Idempotencia de órdenes y recuperación ante errores.
**Artefactos:** `LAB17-Ordenes-Idempotencia`, `LAB17-Data Store`.
**Estado:** Comportamientos reproducidos y validados el 06/11/2026.

### Síntomas y contexto

Al eliminar del Data Store de negocio una orden que había sido procesada satisfactoriamente y volver a enviarla, el Idempotent Process Call continuó reconociéndola como duplicada (HTTP 409). El borrado de la entrada de negocio no reinició el historial interno de idempotencia.
En otra prueba, se preparó `ORD-17503` en `LAB17_ORDENES_IDEMPOTENTES` mediante un writer independiente. El primer intento desde el iFlow con Idempotent Process Call falló con HTTP 500 porque Data Store Write encontró el Entry ID ya existente. El MPL registró:

```text
An entry with id ORD-17503 does already exist in data store LAB17_ORDENES_IDEMPOTENTES
```

### Diagnóstico y diferenciación

El almacén de negocio `LAB17_ORDENES_IDEMPOTENTES` contiene los mensajes persistidos por Data Store Write. Idempotent Process Call mantiene por separado su control de identificadores procesados. Eliminar una entrada del primer almacén no constituye una operación de reinicio del segundo.
El primer intento de `ORD-17503` no completó satisfactoriamente el procesamiento. Tras eliminar la entrada preexistente que causaba el conflicto, el mismo identificador fue aceptado nuevamente.

### Corrección y validación

1. Preparar una entrada `ORD-17503` mediante writer independiente, sin ejecutar el Idempotent Process Call.
2. Enviar `ORD-17503` al iFlow de idempotencia: HTTP 500 y error de entrada existente en MPL.
3. Eliminar exclusivamente la entrada `ORD-17503` del Data Store de negocio.
4. Reintentar la misma petición: HTTP 201, `REGISTRADA`, y nueva entrada persistida.
5. Repetir la petición: HTTP 409, `DUPLICADA`.

### Alcance y precauciones

Los resultados corresponden al escenario ejecutado en SAP Cloud Integration. No se ha probado un rollback coordinado con sistemas externos ni el comportamiento ante fallos en todas las etapas posibles del iFlow. No utilizar el borrado de entradas de negocio como mecanismo general para reiniciar idempotencia. La reversión temporal del writer se efectuó a la versión 1.0.6 y se confirmó el despliegue restaurado.

**Evidencias:** `17-55` a `17-59`, junto a la validación de duplicado `17-51` y `17-52` del capítulo 17.

**Referencias:** [SAP - Define Idempotent Process Call](https://help.sap.com/docs/cloud-integration/sap-cloud-integration/define-idempotent-process-call); [SAP - Define Data Store Write Operations](https://help.sap.com/docs/integration-suite/isuite-integrations-and-apis/define-data-store-write-operations).

## KBA - Validar la conexión real del Idempotent Process Call y la evaluación del Body

**Origen:** Capítulo 17.5. **Estado:** Resuelto y validado.

### Síntomas

En el primer diseño, el componente Idempotent Process Call parecía estar situado sobre el conector del proceso principal, pero Trace mostraba una ejecución de cuatro pasos sin invocar el proceso local ni escribir en Data Store. La respuesta HTTP 200 devolvía el JSON de entrada. Tras corregir la conexión, el componente apareció en la traza y la ruta de registro escribió correctamente.
En los Content Modifiers de respuesta, el Body configurado como **Constant** devolvió el texto literal `${property.OrdenId}` en vez del identificador de la orden.

### Diagnóstico y solución

La representación visual no garantizaba que el paso estuviera insertado en el flujo de ejecución. Se corrigieron las conexiones y se comprobó la ejecución con Trace. Para generar respuestas JSON con un identificador dinámico, se cambió el Body de **Constant** a **Expression** y se validó el valor resuelto mediante Postman.

### Aprendizaje y alcance

Verificar la secuencia efectiva de pasos en Trace y no deducir ejecución únicamente de la posición de un icono en el editor. Diferenciar texto literal de expresión evaluada en Content Modifier. Ambos hallazgos fueron comprobados en este iFlow; no se atribuyen a una falla general del editor o del runtime.


## KBA - Respuesta HTTP binaria después de publicar correctamente en JMS

**Origen:** 18.2. **Estado:** diagnóstico abierto.

### Síntomas

Postman recibió HTTP `202 Accepted` con un body binario no legible tras enviar `ORD-18001` al productor `LAB18-JMS-Productor`. En la respuesta no se observó `Content-Type`; sí `Sap_use_data_compression: true` y `Transfer-Encoding: chunked`.

### Comprobaciones y diagnóstico

La cola `JMS_LAB18_JMS_Productor` quedó `Started` con una entrada `Waiting`; su payload conservó el JSON original. El MPL finalizó `Completed`, `IntermediateError = false`, 882 ms, y el identificador de mensaje permitió correlacionar las observaciones. **La causa del body binario no está confirmada.** El indicador de compresión no demuestra por sí solo causalidad.

### Solución y alcance

No se aplicó ninguna modificación: la publicación JMS fue validada. Si se requiere un cuerpo HTTP legible, deberá investigarse la generación de la respuesta y contrastarse con documentación oficial antes de alterar el diseño. No atribuir el síntoma a la compresión sin una prueba específica.


## KBA - Expresión `jsonpath` no reconocida al iniciar consumidor JMS

**Origen:** laboratorio 18.3, `LAB18-JMS-Consumidor` (06/11/2026). **Estado:** resuelto y validado en este escenario.

### Síntoma

El primer despliegue del consumidor mostró `The Integration Flow is not operational`. En **Status Details** apareció `Context startup failed` y `Unknown function: jsonpath:$.ordenId` al evaluar `SetProperty[OrdenId, simple{${jsonpath:$.ordenId}}]`.

### Diagnóstico

El Content Modifier intentaba crear la Exchange Property `OrdenId` mediante `${jsonpath:$.ordenId}`. El runtime no reconoció esa función en el contexto de expresión utilizado y no pudo iniciar la ruta. La captura no demuestra un problema con JMS ni con el Groovy; el fallo se localizó en la creación de la propiedad.

### Solución aplicada

Se eliminó la fila de Exchange Property problemática. En el recurso existente `GRV_RegistraMensajeJMS.groovy` se analizó el JSON directamente con `JsonSlurper` usando un `Reader`:

```groovy
import groovy.json.JsonSlurper

def json = new JsonSlurper().parse(message.getBody(java.io.Reader))
def ordenId = json.ordenId
message.setProperty("OrdenId", ordenId.toString())
```

El script también validó la presencia de `ordenId` y lo registró en el MPL con `messageLog.addCustomHeaderProperty("OrdenId", ordenId.toString())`. La lectura con `java.io.Reader` eliminó la advertencia del editor por análisis sin streaming; esta práctica ya estaba documentada en los antecedentes de los capítulos 08 y 13 y no constituye un hallazgo nuevo independiente.

### Validación y alcance

Tras guardar y desplegar, el MPL de `LAB18-JMS-Consumidor` mostró `Completed` (836 ms), Custom Header `OrdenId = ORD-18001` y correlación con el productor. La cola quedó con cero entradas. **La corrección está validada para este iFlow y esta expresión; no implica que toda variante de JSONPath sea incompatible con SAP Cloud Integration.**

**Evidencias:** `18-12-jms-consumidor-error-jsonpath.png`, `18-13-jms-consumidor-mpl-completed.png`, `18-14-jms-cola-vacia-despues-consumo.png`.


## KBA - Inserción de Exception Subprocess no persistía en el editor de CPI

**Origen:** laboratorio 19.5, `LAB19_Consumo_API_REST` (10/11/2026). **Estado:** resuelto en el escenario observado; causa raíz no confirmada.

### Síntoma

Al intentar insertar un Exception Subprocess en el Integration Process, el editor mostraba temporalmente un rectángulo durante la operación de arrastre, pero el elemento desaparecía al soltarlo. No se consiguió conservar el subproceso en el diagrama durante esos intentos.

### Diagnóstico y solución aplicada

Se recargó la página del editor. Después de la recarga, fue posible insertar el Exception Subprocess dentro del Integration Process y completar su configuración. La ejecución posterior validó el tratamiento de errores mediante Postman y MPL.

### Alcance y aprendizaje

La recarga de la página resolvió el síntoma en esta sesión. **No se determinó la causa técnica** y no existe evidencia suficiente para atribuirlo a un defecto general de SAP CPI, al navegador, al tenant o al runtime. Antes de reconstruir artefactos o modificar un diseño funcional por un problema de interacción del editor, comprobar si una recarga permite repetir la operación y validar luego el resultado guardado.

## KBA - HTTP 415 al consultar una entidad OData V2 con `Accept: application/xml`

**Origen:** capítulo 20.1, Northwind OData V2. **Estado:** corrección comprobada en Postman.

**Síntoma:** `GET /Customers('ALFKI')` devolvió `415 Unsupported Media Type` y `Microsoft.Data.OData.ODataContentTypeException` al utilizar `Accept: application/xml`.

**Diagnóstico:** el cuerpo del error indicó que el tipo MIME solicitado no coincidía con los formatos admitidos para la respuesta de entidad. El mismo `Accept` sí había permitido obtener el Service Document y `$metadata`; estos resultados no garantizan compatibilidad para entidades.

**Corrección y validación:** retirar el header `Accept` explícito y repetir la consulta. Se obtuvo HTTP 200 con una entrada Atom/XML y `CustomerID=ALFKI`. El servidor mencionó `application/atom+xml` entre los formatos admitidos, pero no se validó una ejecución independiente con ese header.

**Alcance:** comportamiento confirmado en el servicio público Northwind durante el laboratorio 20.1; no atribuirlo a CPI ni generalizarlo a todos los servicios OData.

**Evidencia:** capítulo 20, `20-04-postman-odata-customer-por-clave.png`; el HTTP 415 fue observado durante el diagnóstico.

## KBA - Diferencia observada en la estructura JSON de colecciones Northwind con `$top`

**Origen:** capítulo 20.2, Northwind OData V2. **Estado:** comportamiento observado; causa técnica no confirmada.

**Síntoma:** una respuesta de colección se presentó como `d.results`, mientras que una consulta con `$top` mostró un arreglo directamente bajo `d`. El usuario aisló `$top` como el cambio que coincidía con la diferencia en sus pruebas controladas.

**Comprobación:** la respuesta con `$top` declaró `Content-Type: application/json;odata=verbose;charset=utf-8`. Las capturas de las consultas con `$select` y `$orderby` muestran el arreglo bajo `d`.

**Diagnóstico:** no se ha establecido por qué el servicio devolvió estructuras distintas ni si intervienen otros factores. El header declarado y el cuerpo observado deben registrarse separadamente. No afirmar que `$top` elimina `results` por definición de OData V2.

**Mitigación:** inspeccionar el cuerpo y `Content-Type` reales antes de diseñar expresiones JSONPath, scripts o mappings que dependan de una ruta fija. No se implementó ni validó una normalización automática.

**Alcance:** observación limitada a las consultas Northwind ejecutadas en Postman durante 20.2.

**Evidencias:** `20-06-postman-odata-filter-country-germany.png`, `20-07-postman-odata-select-properties.png`, `20-08-postman-odata-orderby-desc-top5.png` y comprobación del header de respuesta.

## KBA-LAB-021-A - Clave privada de host SSH rechazada en Docker sobre Windows

**Estado:** Resuelto en el entorno del laboratorio.

**Síntomas:** `WARNING: UNPROTECTED PRIVATE KEY FILE!`, `Permissions 0777 ... are too open`, `Unable to load host key ... bad permissions`, y `chmod ... Read-only file system`.

**Diagnóstico:** la clave privada ED25519 montada desde el host Windows no tenía permisos aceptables para OpenSSH dentro del contenedor. La conexión inicial utilizó una clave RSA alternativa, por lo que el éxito de la conexión no validaba la clave ED25519 prevista.

**Corrección:** se configuró un script de inicialización ejecutado desde `/etc/sftp.d/` para instalar las claves de host con permisos apropiados dentro del contenedor Linux. Tras reiniciar se comprobó que desaparecía el aviso de permisos y que CPI negociaba `ssh-ed25519` con la misma huella SHA256 obtenida directamente del contenedor.

**Alcance:** configuración local Docker + atmoz/sftp + volúmenes Windows observada en capítulo 21. El script exacto debe conservarse en el repositorio para reproducibilidad.

## KBA-LAB-021-B - `Cannot access known hosts artifact` en SSH Connectivity Test

**Estado:** Resuelto.

**Síntoma:** `java.lang.IllegalArgumentException: Cannot access known hosts artifact` al usar `Host Key Verification = Against Tenant`.

**Diagnóstico:** no había entradas SSH Known Hosts registradas en el tenant para el servidor de laboratorio.

**Corrección:** se creó y desplegó un artefacto SSH Known Hosts con la clave pública ED25519 real y la entrada `[sftp-learning-lab.internal]:2222 ssh-ed25519 ...`. Se repitió la prueba con la credencial `LAB21_SFTP_BASIC` y `Against Tenant`.

**Validación:** `Successfully reached host`; tipo de clave `ssh-ed25519`; acceso al directorio `/upload` comprobado mediante `Check Directory Access`.

**Alcance:** autenticación/conectividad SSH y acceso al directorio. No demuestra aún escritura SFTP mediante un iFlow.

## KBA-LAB-021-C - Contraseña administrativa de SAP Cloud Connector olvidada

**Estado:** Incidente resuelto al recordar la contraseña; recuperación técnica no ejecutada.

**Síntoma:** no se recordaba la contraseña de administración de Cloud Connector.

**Resultado:** el administrador recordó la contraseña y accedió sin restauración de archivos ni reinstalación.

**Límite:** no se comprobó que reemplazar o importar un XML de usuarios permita restablecer la contraseña, ni se verificó el formato interno de almacenamiento de la credencial. No presentar la restauración del XML como método probado.

**Prevención:** conservar un respaldo soportado por la versión de Cloud Connector y las credenciales administrativas en un gestor de contraseñas; verificar con documentación oficial la recuperación aplicable a la versión instalada antes de modificar archivos internos.

## KBA-LAB-021-D - `Permission denied` al escribir mediante SFTP Receiver

**Estado:** Resuelto y reproducido en laboratorio 21.2.

**Contexto:** iFlow `LAB21_SFTP_Receiver`, SFTP Receiver con autenticación `LAB21_SFTP_BASIC`, proxy `On-Premise` y destino temporal `/upload/sin-permisos`.

**Síntoma:** la petición HTTPS POST devolvió `500 Internal Server Error`. El mensaje en Monitor quedó `Failed`; `Last Error` incluyó `Permission denied`.

**Diagnóstico comprobado:** el directorio `/home/labuser/upload/sin-permisos` existía, pertenecía a `root:root` y tenía permisos `555` (`dr-xr-xr-x`), que no permiten a `labuser` crear archivos. El mismo iFlow había escrito previamente en `/upload` con HTTP 200 y MPL Completed.

**Corrección:** restaurar `Directory = /upload` en SFTP Receiver, guardar y desplegar. La nueva petición con `ORD-21004` devolvió HTTP 200; la lectura del archivo en Docker confirmó el contenido.

**Alcance:** se verificó el rechazo de escritura por permisos y su propagación al endpoint HTTP y al MPL. No se probaron políticas de reintento ni gestión centralizada de errores.

**Evidencias:** `21-09-sftp-receiver-error-permisos-postman.png`, `21-10-sftp-receiver-mpl-failed-permission-denied.png`; restauración validada con `ORD-21004`.

**Aprendizaje:** el estado `Started` del iFlow y una conexión SFTP válida no garantizan permisos de escritura en todos los directorios remotos. Verificar permisos del destino y el `Last Error` antes de cambiar credenciales o configuración de Cloud Connector.
