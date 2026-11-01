# 15 - Seguridad: OAuth, Basic, certificados y Security Material

**Estado del tema:** 🟨 En curso
**Package:** `CPI Learning Lab`
**iFlows reutilizados:** `Request Reply` (`Request_Reply`) y `Consulta Clientes`
**Fecha de las validaciones:** 30–31/10/2026

## Objetivo

Comprender y validar los mecanismos de autenticación y autorización de entrada y salida en SAP Cloud Integration, la administración de credenciales mediante Security Material Basic Authentication y la administración de Security Material; los certificados y mecanismos de confianza se abordarán posteriormente.

## Conceptos

- **Inbound / HTTPS Sender:** controla la recepción de solicitudes y la autorización necesaria para invocar un endpoint de integración.
- **Outbound / HTTP Receiver:** configura la comunicación de CPI hacia otro servicio, incluida la autenticación que exige el receptor.
- **OAuth 2.0 Client Credentials:** permite a un cliente solicitar un token de acceso mediante sus credenciales, sin intervención de un usuario final.
- **Bearer token:** credencial de acceso que debe protegerse durante su vigencia.
- **Security Material:** almacena configuraciones de credenciales referenciables por componentes compatibles.
- **Where-Used:** identifica artefactos que referencian un material de seguridad.
- **MPL:** registro de procesamiento de mensajes; su ausencia en una búsqueda no identifica por sí sola la causa interna de un rechazo HTTP.

## Prerrequisitos

- Acceso a SAP Integration Suite, Cloud Integration, Design y Monitor.
- Package `CPI Learning Lab` e iFlows del capítulo 08 disponibles.
- Colección Postman `CPI Learning Lab`, con peticiones del capítulo 08.
- No publicar tokens, secretos, contraseñas, claves privadas ni service keys.

---

## 15.1 - Arquitectura de seguridad: autenticación y autorización

**Estado:** ✅ Completado

### Objetivo

Identificar las funciones de seguridad disponibles en el tenant, distinguir la seguridad de entrada y salida, y comprobar una dependencia real entre un iFlow y un Security Material desplegado.

### Laboratorio paso a paso y validación

1. Abrir **Monitor → Integrations and APIs → Manage Security**. Se observaron `Security Material` (1 artefacto), `Keystore` (13 entradas), `PGP Keys` (0), `Access Policies` (0), `User Roles` (2), además de `JDBC Material` y `Connectivity Tests` sin contador visible en la captura.

   **Motivo:** verificar las funciones visibles en el entorno antes de diseñar ejercicios dependientes de ellas.

   **Evidencia 15-01 — Secciones de Manage Security.** Vista general de las tarjetas disponibles.

   ![Evidencia 15-01 — Manage Security](images/tema-15/15-01-monitor-manage-security.png)

2. Abrir **Security Material**. Se observó `OAUTH2_Consulta_Clientes`, tipo `OAuth2 Client Credentials`, estado `Deployed`, con fecha de despliegue 20/10/2026.

   **Motivo:** identificar un artefacto real y evitar crear credenciales redundantes.

   **Evidencia 15-02 — Material OAuth existente.** Lista de Security Material y estado de despliegue.

   ![Evidencia 15-02 — Material OAuth](images/tema-15/15-02-security-material-oauth-existente.png)

3. Abrir la configuración del material, **sin guardar ni desplegar cambios**. Se observaron Token Service URL, Client ID, Client Secret, Grant Type `Send as Part of URL`, Client Authentication `Send as Request Header`, Scope vacío, Content Type `application/json`, Resource y Audience vacíos, y ausencia de Custom Parameters. Los valores sensibles fueron ocultados.

   **Motivo:** diferenciar el mecanismo de solicitud del token de la autenticación del cliente ante el servidor de autorización. Esta inspección no demuestra emisión exitosa del token desde el adaptador.

   **Evidencia 15-03 — Configuración OAuth.** Campos de OAuth2 Client Credentials con identificadores y secretos ocultos.

   ![Evidencia 15-03 — Configuración OAuth](images/tema-15/15-03-security-material-oauth-configuracion.png)

4. Utilizar **Actions → Where-Used** sobre `OAUTH2_Consulta_Clientes`. SAP mostró una referencia: iFlow `Request Reply`, ID `Request_Reply`, package `CPI Learning Lab`, componente `HTTP` de tipo `Adapter`.

   **Motivo:** localizar dependencias sin abrir todos los iFlows individualmente. El resultado demuestra una referencia de configuración, no una ejecución correcta.

   **Evidencia 15-04 — Where-Used.** Identificación del iFlow y del adaptador que referencian el material.

   ![Evidencia 15-04 — Where-Used](images/tema-15/15-04-security-material-where-used.png)

5. Abrir `Request Reply` en visualización y seleccionar **HTTP Receiver → Connection**. Se observó método `GET`, Proxy Type `Internet`, autenticación `OAuth2 Client Credentials`, Credential Name `OAUTH2_Consulta_Clientes` y destino `/http/lab08/consulta-clientes`.

   **Motivo:** verificar dónde se utiliza el material de seguridad en una comunicación saliente.

   **Evidencia 15-05 — HTTP Receiver y credencial OAuth.** Configuración del adaptador y referencia al material desplegado.

   ![Evidencia 15-05 — HTTP Receiver OAuth](images/tema-15/15-05-http-receiver-oauth-credential-reference.png)

6. Reutilizar en Postman la petición **08.2 - Content Enricher → POST Enriquecer Solicitud Cliente**. Se envió `solicitudId = SOL-1001`, `clienteId = 1001` y `origen = POSTMAN` al endpoint `/http/lab08/request-reply`. Se obtuvo **HTTP 200** y XML `SolicitudEnriquecida` con cliente `1001`, nombre `Ana` y segmento `PREMIUM`.

   **Motivo:** validar el resultado funcional del escenario existente sin crear otro iFlow.

   **Evidencia 15-06 — Respuesta enriquecida.** Petición y respuesta XML correctas en Postman.

   ![Evidencia 15-06 — Postman HTTP 200](images/tema-15/15-06-postman-oauth-request-reply.png)

7. Revisar **Monitor Message Processing**. Se observaron `Request Reply` con estado `Completed`, duración **2 s 600 ms** y actualización a las **18:27:44**, y `Consulta Clientes` con estado `Completed`, duración **8 ms** y actualización a las **18:27:43**, hora GMT-03:00.

   **Motivo:** contrastar la respuesta del consumidor con el registro de procesamiento de ambos iFlows.

   **Evidencia 15-07 — Ejecuciones Completed.** Monitor con ambos iFlows y sus tiempos observados.

   ![Evidencia 15-07 — Monitor OAuth](images/tema-15/15-07-monitor-oauth-request-reply-completed.png)

### Resultado esperado y validación

Se verificó la disponibilidad de las funciones de seguridad, la existencia del material OAuth, su dependencia en un HTTP Receiver, una respuesta enriquecida HTTP 200 y dos MPL Completed. **No se inspeccionó directamente el intercambio de tokens que realiza el adaptador.**

### Aprendizajes

- La seguridad de entrada y de salida se configura en componentes diferentes.
- Where-Used permite analizar dependencias antes de modificar materiales compartidos.
- La referencia de configuración, la respuesta funcional y el MPL son evidencias complementarias, pero no equivalentes.

---

## 15.2 - OAuth 2.0 Client Credentials

**Estado:** ✅ Completado (alcance práctico definido; prueba de permisos insuficientes no realizada)

### Objetivo

Verificar la emisión de un token Client Credentials, sus metadatos, el rechazo de solicitudes sin credenciales o con token inválido y el rol requerido por el HTTPS Sender.

### Laboratorio paso a paso y validación

1. Revisar la petición existente de Postman **08.2 → Get OAuth Token**. Configuración observada: método `POST`, URL `{{token_url}}`, Body `x-www-form-urlencoded` con `grant_type=client_credentials`, autenticación `Basic Auth` con `{{client_id}}` y `{{client_secret}}`, y encabezado `Content-Type: application/x-www-form-urlencoded`.

   **Motivo:** distinguir la autenticación del cliente OAuth mediante Basic de la autenticación Bearer ante el recurso final. Las capturas de configuración se utilizaron para inspección; no se duplicaron como evidencias numeradas.

2. Ejecutar **Get OAuth Token**. Postman devolvió **HTTP 200**, `token_type = bearer`, `expires_in = 3599`, `scope` con `ESBMessaging.send` y `API.invoke`, y campo `jti`. La duración anunciada equivale a `3599 / 60 = 59,98` minutos, aproximadamente 60 minutos desde la emisión. No se verificó renovación automática.

   **Motivo:** comprobar la emisión del token y distinguir su duración, tipo e información de permisos. `jti` identifica el token; no es el Message ID ni el Correlation ID de CPI.

   **Evidencia 15-08 — Respuesta del servidor OAuth.** HTTP 200 y metadatos del token; valor de `access_token` oculto.

   ![Evidencia 15-08 — Token OAuth](images/tema-15/15-08-postman-oauth-token-response.png)

3. Crear la carpeta de Postman **Lab 15 - Seguridad** y duplicar la petición de enriquecimiento como **15.2 - OAuth - Token inválido**. Confirmar `Authorization = Bearer Token` y uso original de `{{access_token}}`. Sustituir **solo en la petición duplicada** por `TOKEN_INVALIDO_LAB15` y ejecutar. Resultado: **HTTP 401 Unauthorized**.

   **Motivo:** comprobar rechazo de un token incorrecto sin modificar variables compartidas ni la petición histórica.

   **Evidencia 15-09 — Token inválido.** Valor de prueba literal y respuesta HTTP 401.

   ![Evidencia 15-09 — Token inválido](images/tema-15/15-09-postman-oauth-token-invalido.png)

4. Revisar **Monitor Message Processing** con filtro temporal `Past Hour`. Solo aparecieron las ejecuciones anteriores de `Request Reply` y `Consulta Clientes`, sin nueva ejecución visible correspondiente al intento HTTP 401.

   **Motivo:** evaluar si el rechazo se registró como procesamiento del iFlow. El resultado es coherente con rechazo previo al procesamiento, pero no permite identificar de manera concluyente el componente interno que lo produjo.

   **Evidencia 15-10 — Sin nuevo MPL visible.** Resultado de la búsqueda en el intervalo consultado.

   ![Evidencia 15-10 — Sin MPL nuevo](images/tema-15/15-10-monitor-token-invalido-sin-mpl.png)

5. Duplicar la petición negativa como **15.2 - OAuth - Sin token**, seleccionar `No Auth` y comprobar que Postman no genera un encabezado Authorization. Ejecutar: **HTTP 401 Unauthorized**.

   **Motivo:** diferenciar ausencia de credenciales y credenciales inválidas; ambas fueron rechazadas en este endpoint.

   **Evidencia 15-11 — Solicitud sin token.** Lista de headers sin Authorization y respuesta HTTP 401.

   ![Evidencia 15-11 — Sin token](images/tema-15/15-11-postman-oauth-sin-token-401.png)

6. Abrir **Manage Security → User Roles**. Se observaron dos roles administrados por SAP: `API.invoke` (descripción: rol predeterminado para invocar API y MCP Server) y `ESBMessaging.send` (rol predeterminado para enviar mensajes a integration flows).

   **Motivo:** relacionar los nombres de roles visibles en CPI con los scopes recibidos en la respuesta OAuth. Su presencia no demuestra asignación a una identidad específica.

   **Evidencia 15-12 — User Roles.** Lista de roles disponibles y propietarios SAP.

   ![Evidencia 15-12 — User Roles](images/tema-15/15-12-security-user-roles.png)

7. Abrir `Request Reply` y seleccionar **HTTPS Sender → Connection**. Se verificó Address `/lab08/request-reply`, `Authorization = User Role`, `User Role = ESBMessaging.send` y `CSRF Protected` desactivado.

   **Motivo:** identificar el requisito de autorización **de entrada**, distinto de la credencial OAuth del HTTP Receiver.

   **Evidencia 15-13 — HTTPS Sender y rol exigido.** Configuración de autorización de entrada.

   ![Evidencia 15-13 — User Role Sender](images/tema-15/15-13-https-sender-user-role.png)

### Resultado esperado y validación

| Escenario | Resultado observado | Validación |
|---|---|---|
| Obtención del token | HTTP 200; Bearer; `expires_in=3599`; scopes presentes | Confirmado |
| Solicitud con token válido (escenario reutilizado de 15.1) | HTTP 200; XML enriquecido | Confirmado |
| Solicitud con token inválido | HTTP 401 | Confirmado |
| Solicitud sin token | HTTP 401 | Confirmado |
| Nuevo MPL del rechazo por token inválido | No visible en la búsqueda realizada | Observación confirmada, causa interna no demostrada |
| HTTPS Sender requiere `ESBMessaging.send` | Configuración visible | Confirmado |
| Token válido sin el rol requerido | No ejecutado | No validado |

### Aprendizajes

- Basic Auth puede servir para autenticar al cliente ante el servidor de tokens, mientras que el endpoint de integración recibe un Bearer token.
- La presencia de scopes en el token y la exigencia de un User Role en el Sender son hechos distintos; no se probó un escenario de permisos insuficientes.
- HTTP 401 se observó tanto para ausencia de token como para token inválido.
- No todos los rechazos HTTP se reflejan necesariamente como un nuevo MPL visible en el monitor consultado.

---

## 15.3 — Basic Authentication

**Estado:** ✅ Completado

### Objetivo

Configurar una autenticación Basic saliente mediante un material de seguridad de tipo User Credentials, comprobar la ejecución correcta y diagnosticar un rechazo por contraseña incorrecta.

### Conceptos y prerrequisitos

- La autenticación Bearer del HTTPS Sender protege la entrada a CPI; la autenticación Basic del HTTP Receiver protege la salida hacia el servicio externo.
- `User Credentials` permite almacenar usuario y contraseña sin escribirlos en el iFlow.
- Package `CPI Learning Lab`, acceso a Design, Monitor, Security Material y Postman.

### Laboratorio paso a paso

1. Crear el iFlow `LAB15_03_Basic_Authentication` en `CPI Learning Lab`.

   **Motivo:** aislar el escenario Basic sin modificar los iFlows utilizados en laboratorios anteriores.

   **Evidencia 15-14 — iFlow creado.**

   ![Evidencia 15-14 — iFlow creado](images/tema-15/15-14-iflow-basic-authentication-creado.png)

2. Configurar HTTPS Sender con dirección `/lab15/basic-authentication`, `Authorization = User Role`, rol `ESBMessaging.send` y CSRF desactivado.

   **Motivo:** conservar la protección OAuth de entrada y distinguirla de la autenticación Basic de salida.

   **Evidencia 15-15 — HTTPS Sender configurado.**

   ![Evidencia 15-15 — HTTPS Sender](images/tema-15/15-15-https-sender-configuracion.png)

3. Incorporar `Request Reply` y conectar un Receiver mediante adaptador HTTP.

   **Motivo:** realizar una llamada síncrona al servicio externo y devolver su respuesta al consumidor.

   **Evidencia 15-16 — Request Reply.**

   ![Evidencia 15-16 — Request Reply](images/tema-15/15-16-iflow-request-reply.png)

   **Evidencia 15-17 — Receiver conectado.**

   ![Evidencia 15-17 — Receiver conectado](images/tema-15/15-17-iflow-http-receiver-conectado.png)

4. Revisar la configuración inicial del HTTP Receiver y cambiar `Authentication` a `Basic`, habilitando el campo `Credential Name`.

   **Motivo:** configurar explícitamente el mecanismo de autenticación requerido por el receptor.

   **Evidencia 15-18 — Configuración inicial.**

   ![Evidencia 15-18 — Configuración inicial](images/tema-15/15-18-http-receiver-configuracion-inicial.png)

   **Evidencia 15-19 — Basic Authentication seleccionado.**

   ![Evidencia 15-19 — Basic Authentication](images/tema-15/15-19-http-receiver-basic-authentication.png)

5. Crear en Security Material un `User Credentials` llamado `BASIC_LAB15_HTTPBIN`, utilizando exclusivamente credenciales de demostración para HTTPBin. Validar separadamente el desafío Basic de HTTPBin mediante navegador.

   **Motivo:** mantener las credenciales fuera del diseño del iFlow y comprobar el comportamiento del destino.

   **Evidencia 15-20 — Formulario de User Credentials.**

   ![Evidencia 15-20 — User Credentials](images/tema-15/15-20-security-material-user-credentials.png)

   **Evidencia 15-21 — Autenticación Basic directa en HTTPBin.** Respuesta con `authenticated: true` y `authorized: true`.

   ![Evidencia 15-21 — HTTPBin](images/tema-15/15-21-httpbin-basic-auth-validacion.png)

6. Desplegar `BASIC_LAB15_HTTPBIN` y referenciarlo en HTTP Receiver con dirección `https://httpbin.org/basic-auth/usuario/contrase%C3%B1a`, método `GET`, proxy `Internet`, `Authentication = Basic` y `Credential Name = BASIC_LAB15_HTTPBIN`.

   **Motivo:** recuperar las credenciales por alias durante la llamada saliente, sin incorporarlas al mensaje ni al diseño.

   **Evidencia 15-22 — Security Material desplegado.**

   ![Evidencia 15-22 — Security Material](images/tema-15/15-22-security-material-basic-deployed.png)

   **Evidencia 15-23 — HTTP Receiver configurado.**

   ![Evidencia 15-23 — HTTP Receiver](images/tema-15/15-23-http-receiver-basic-configurado.png)

7. Desplegar el iFlow. Se verificó estado `Started`, versión runtime `1.0.1` y endpoint `/http/lab15/basic-authentication`.

   **Motivo:** comprobar que la configuración está disponible en el runtime.

   **Evidencia 15-24 — iFlow Started.**

   ![Evidencia 15-24 — Despliegue](images/tema-15/15-24-iflow-basic-authentication-started.png)

8. Ejecutar desde la carpeta Postman `Lab 15 - Seguridad` la petición independiente `15.3 - Basic Authentication - Credenciales válidas`, con método GET y Bearer `{{access_token}}` para la entrada a CPI. Se obtuvo HTTP `200` y JSON `authenticated: true`, `user: usuario`, `authorized: true`.

   **Motivo:** verificar el recorrido completo: OAuth de entrada y Basic de salida.

   **Evidencia 15-25 — Prueba positiva HTTP 200.**

   ![Evidencia 15-25 — Postman HTTP 200](images/tema-15/15-25-postman-basic-auth-http-200.png)

9. Consultar `Where-Used` de `BASIC_LAB15_HTTPBIN`. La vista devolvió `Artifacts (0)` y `No references found`, pese a la referencia visible en HTTP Receiver y la ejecución correcta.

   **Motivo:** contrastar el índice de dependencias con la configuración y ejecución observadas.

   **Evidencia 15-26 — Where-Used sin referencias.**

   ![Evidencia 15-26 — Where-Used](images/tema-15/15-26-security-material-basic-where-used.png)

10. Modificar temporalmente la contraseña de demostración en `BASIC_LAB15_HTTPBIN`, sin modificar ni volver a desplegar el iFlow. Ejecutar la petición independiente `15.3 - Basic Authentication - Credenciales incorrectas` en Postman. CPI respondió HTTP `500`; la petición registró una aserción fallida porque esperaba HTTP `200`.

    **Motivo:** observar el rechazo de autenticación saliente y distinguir la respuesta del servicio externo de la respuesta propagada al consumidor.

    **Evidencia 15-27 — Material actualizado para prueba negativa.**

    ![Evidencia 15-27 — Material actualizado](images/tema-15/15-27-security-material-basic-actualizado.png)

    **Evidencia 15-28 — Prueba negativa en Postman.**

    ![Evidencia 15-28 — Postman HTTP 500](images/tema-15/15-28-postman-basic-auth-credenciales-incorrectas.png)

11. Inspeccionar el MPL `Failed`. El error `com.sap.it.rt.adapter.http.api.exception.HttpResponseException` identificó una operación HTTP fallida hacia HTTPBin con `statusCode: 401`. El consumidor recibió HTTP `500`.

    **Motivo:** identificar el código real devuelto por el receptor y evitar confundirlo con el error expuesto por CPI.

    **Evidencia 15-29 — Error Details del MPL.**

    ![Evidencia 15-29 — Error Details](images/tema-15/15-29-monitor-basic-auth-error-details.png)

12. Restaurar el material de demostración `BASIC_LAB15_HTTPBIN` y verificar su estado `Deployed`, sin modificar el iFlow. No se repitió la prueba positiva porque su resultado ya estaba demostrado y la repetición no era necesaria para el objetivo del ejercicio.

    **Evidencia 15-30 — Security Material restaurado.** La captura confirma el estado `Deployed`, pero no expone la contraseña ni prueba por sí sola su valor.

    ![Evidencia 15-30 — Material restaurado](images/tema-15/15-30-security-material-basic-restaurado.png)

### Resultado esperado y validación

| Escenario | Resultado observado | Estado |
|---|---|---|
| Credenciales Basic correctas | HTTP 200; `authenticated=true` y `authorized=true` | Confirmado |
| Credenciales Basic incorrectas | HTTPBin 401 identificado en MPL; CPI devolvió HTTP 500 | Confirmado |
| Dependencia Where-Used | Cero referencias mostradas pese al uso comprobado | Discrepancia observada; causa no confirmada |
| Material restaurado | Estado `Deployed` | Confirmado; sin nueva prueba funcional |

### Problemas encontrados y aprendizajes

- La respuesta HTTP `500` de CPI no equivale al código `401` recibido del servicio externo: el MPL permitió distinguirlos.
- `Where-Used` no mostró una referencia conocida en este caso. No se ha demostrado la causa; no debe utilizarse como única comprobación de dependencias antes de eliminar materiales.
- La separación de credenciales y diseño facilita actualizar Security Material sin editar el iFlow.

---

## 15.4 — Security Material: administración y dependencias

**Estado:** 🟦 Revisado

### Objetivo

Analizar el inventario, el uso y las precauciones de administración de Security Material a partir de evidencias obtenidas en los laboratorios 15.1 y 15.3.

### Conceptos y prerrequisitos

- Materiales `OAuth2 Client Credentials` y `User Credentials`.
- Referencias de adaptadores mediante `Credential Name`.
- Diferencia entre material desplegado, referencia visible en diseño y uso funcional comprobado.
- Evidencias 15-02, 15-04, 15-05, 15-22, 15-23, 15-25 y 15-26.

### Revisión de evidencias existentes

1. `OAUTH2_Consulta_Clientes`: material OAuth desplegado, referenciado en el HTTP Receiver de `Request Reply`; `Where-Used` mostró una referencia (evidencias 15-02, 15-04 y 15-05).
2. `BASIC_LAB15_HTTPBIN`: material `User Credentials` desplegado, referenciado en el HTTP Receiver de `LAB15_03_Basic_Authentication`, con ejecución HTTP 200 (evidencias 15-22, 15-23 y 15-25).
3. Para `BASIC_LAB15_HTTPBIN`, `Where-Used` mostró cero referencias (evidencia 15-26). La diferencia con el material OAuth es un hecho observado, **no una explicación confirmada de cómo funciona internamente Where-Used**.

### Resultado y validación

La revisión permitió contrastar referencias y resultados previamente demostrados, sin crear nuevas credenciales, repetir capturas ni modificar iFlows. **No se ejecutó un laboratorio independiente de administración o eliminación segura**; por ello el estado es `🟦 Revisado`, no `✅ Completado`.

### Problemas encontrados y aprendizajes

- No asumir que `Where-Used` proporciona un inventario exhaustivo en todas las situaciones: el caso Basic mostró una discrepancia no explicada.
- Antes de modificar o eliminar un material, contrastar referencias de diseño, consumidores conocidos y pruebas existentes.
- Evitar publicar secretos o capturas que expongan contraseñas y tokens.

---

## Problemas encontrados y hallazgos

- **Correlación entre iFlows no validada:** se intentó buscar por Correlation ID las ejecuciones `Request Reply` y `Consulta Clientes`, pero no se obtuvo una correlación conjunta verificable. No se atribuye causa ni se afirma propagación de identificadores. Se reserva para un escenario posterior de trazabilidad si corresponde.
- **Alcance de autorización:** no se generó un token válido carente de `ESBMessaging.send`; por ello no se documenta una respuesta HTTP 403 como resultado de laboratorio.
- **Rechazo sin MPL visible:** ante token inválido se obtuvo HTTP 401 y no apareció una nueva ejecución en el intervalo consultado. La observación puede reutilizarse como caso de diagnóstico, sin adjudicar la causa interna sin evidencia adicional.

## Aprendizajes consolidados

- El adaptador HTTPS Sender controla la autorización de entrada; el HTTP Receiver utiliza el material de seguridad para la salida.
- Where-Used ayuda a explorar dependencias, pero la discrepancia observada en 15.3 impide tratarlo como comprobación exhaustiva.
- Las pruebas positivas y negativas, los roles y el MPL permiten distinguir autenticación de entrada, autenticación de salida y errores del receptor.
- La documentación de seguridad debe excluir secretos y evitar duplicar evidencias.

## Continuidad del capítulo

**15.1, 15.2 y 15.3 están completados dentro del alcance validado; 15.4 está revisado sin laboratorio independiente.** El capítulo continúa **🟨 En curso**.

| Laboratorio | Estado | Alcance |
|---|---|---|
| 15.1 Arquitectura de seguridad | ✅ Completado | Evidencias 15-01 a 15-07 |
| 15.2 OAuth 2.0 Client Credentials | ✅ Completado | Evidencias 15-08 a 15-13 |
| 15.3 Basic Authentication | ✅ Completado | Evidencias 15-14 a 15-30 |
| 15.4 Security Material | 🟦 Revisado | Dependencias revisadas con evidencias previas; sin práctica independiente |
| 15.5 Certificados, Keystore y TLS | ⬜ Pendiente | Confianza y protección del transporte |
| 15.6 Autenticación mediante certificados | ⬜ Pendiente | Escenario compatible con el entorno disponible |
| 15.7 Validación integral | ⬜ Pendiente | Comparación y diagnóstico de mecanismos |

## Referencias

- [SAP Help Portal — Cloud Integration](https://help.sap.com/docs/cloud-integration)
- [RFC 6749 — OAuth 2.0 Authorization Framework](https://www.rfc-editor.org/rfc/rfc6749)
- [RFC 6750 — OAuth 2.0 Bearer Token Usage](https://www.rfc-editor.org/rfc/rfc6750)

> **Seguridad documental:** las referencias de imágenes indican las rutas previstas en el repositorio. Su incorporación física debe verificarse al publicar; no se afirma que ya estén subidas.
