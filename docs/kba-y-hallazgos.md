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
