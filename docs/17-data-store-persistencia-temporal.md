# 17 - Data Store y persistencia temporal

**Estado del tema:** ✅ Completado - documentación consolidada; solo falta la evidencia práctica de eliminación automática en 17.4
**Package:** `CPI Learning Lab`
**Última validación práctica:** 06/11/2026

## Objetivo

Persistir, consultar, seleccionar y eliminar mensajes en SAP Cloud Integration; diferenciar los ámbitos `Integration Flow` y `Global`; y verificar cómo los parámetros de retención y expiración determinan las fechas de las entradas.

## Conceptos

- **Data Store Write:** almacena el mensaje con un nombre de almacén y un Entry ID.
- **Data Store Get:** recupera una entrada identificada.
- **Data Store Select:** selecciona entradas y, en el escenario XML validado, devuelve un documento `<messages>` con los mensajes seleccionados.
- **Data Store Delete:** elimina una entrada identificada.
- **Visibility:** `Integration Flow` restringe el ámbito al iFlow; `Global` permite el acceso compartido, como se comprobó con dos iFlows.
- **Retention Threshold for Alerting:** período utilizado para calcular `Due At`.
- **Expiration Period:** período utilizado para calcular `Retain Until`. La fecha registrada no demuestra por sí sola la ejecución de la eliminación automática.
- **Overwrite Existing Message:** controla la sobrescritura de una entrada con el mismo identificador.

## Prerrequisitos

SAP Integration Suite / Cloud Integration, acceso a Design y Monitor, package `CPI Learning Lab` y Postman con autenticación configurada. En la colección `CPI Learning Lab`, carpeta `Lab 17 - Data Store`, se mantienen peticiones independientes por laboratorio. No almacenar tokens ni credenciales en el repositorio.

## Alcance y estado

| Laboratorio | Alcance | Estado |
|---|---|---|
| 17.1 | Data Store Write | ✅ Completado |
| 17.2 | Data Store Get y visibilidad Global | ✅ Completado |
| 17.3 | Select, Router y Delete | ✅ Completado |
| 17.4 | Retención y expiración | ✅ Completado: fechas verificadas; eliminación automática pendiente |
| 17.5 | Idempotencia y recuperación después de errores | ✅ Completado |

## 17.1 - Data Store Write

**Estado:** ✅ Completado

### Objetivo y procedimiento

1. Crear `LAB17-Data Store` (ID `LAB17-Data_Store`) en `CPI Learning Lab`.

   **Evidencia 17-01 - iFlow creado.** Artefacto de persistencia independiente.

   ![Evidencia 17-01](images/tema-17/17-01-iflow-data-store-creado.png)

2. Configurar HTTPS Sender: `Address = /lab17/datastore/write`, `Authorization = User Role`, `User Role = ESBMessaging.send`, CSRF desactivado.

   **Evidencia 17-02 - HTTPS Sender.** Endpoint de escritura.

   ![Evidencia 17-02](images/tema-17/17-02-https-sender-write-configurado.png)

3. Añadir `CM_Preparar_Mensaje` con `Message Body → Constant`. Un cuerpo fijo permite verificar el contenido persistido independientemente del body de Postman.

   ```json
   {"id":"ORD-17001","cliente":"Cliente Laboratorio","monto":150000,"moneda":"CLP","estado":"PENDIENTE"}
   ```

   **Evidencia 17-03 - Mensaje de prueba.** JSON configurado en Content Modifier.

   ![Evidencia 17-03](images/tema-17/17-03-content-modifier-mensaje-persistencia.png)

4. Configurar `DS_Write_Orden`: `Data Store Name = LAB17_ORDENES`, `Visibility = Integration Flow`, `Entry ID = ORD-17001`, umbral de alerta 2 días, expiración 30 días, cifrado activado, sobrescritura e inclusión de headers desactivadas.

   **Evidencia 17-04 - Write configurado.** Parámetros iniciales.

   ![Evidencia 17-04](images/tema-17/17-04-data-store-write-configurado.png)

5. Desplegar versión `1.0.0` y verificar `Started`.

   **Evidencia 17-05 - Despliegue.** iFlow iniciado.

   ![Evidencia 17-05](images/tema-17/17-05-iflow-data-store-started.png)

6. Ejecutar POST desde `17.1 - Data Store Write`: HTTP 200; comprobar después la entrada y el payload en Monitor.

   **Evidencia 17-06 - Postman.** Escritura satisfactoria.

   ![Evidencia 17-06](images/tema-17/17-06-postman-data-store-write.png)

   **Evidencia 17-07 - Monitor.** Persistencia de `ORD-17001`.

   ![Evidencia 17-07](images/tema-17/17-07-data-store-orden-persistida.png)

### Validación, problemas y aprendizajes

Se validaron despliegue, respuesta HTTP 200 y persistencia de `ORD-17001`. La visibilidad inicial `Integration Flow` se contrastó posteriormente con `Global` en 17.2.

## 17.2 - Data Store Get y acceso global

**Estado:** ✅ Completado

### Objetivo y procedimiento

1. Crear `LAB17-Data Store Get` (ID `LAB17-Data_Store_Get`), separado del escritor.

   **Evidencia 17-08 - Lector independiente.**

   ![Evidencia 17-08](images/tema-17/17-08-iflow-data-store-get-creado.png)

2. Configurar HTTPS Sender `/lab17/datastore/get`, `User Role = ESBMessaging.send`, CSRF desactivado; crear `17.2 - Data Store Get` en Postman.

   **Evidencia 17-09 - HTTPS Sender Get.**

   ![Evidencia 17-09](images/tema-17/17-09-https-sender-get-configurado.png)

3. Configurar Data Store Get con `LAB17_ORDENES`, `Global`, `ORD-17001`, `Delete On Completion = off`, `Throw Exception on Missing Entry = on`; desplegar versión `1.0.0`.

   **Evidencia 17-10 - Lector iniciado.**

   ![Evidencia 17-10](images/tema-17/17-10-iflow-data-store-get-started.png)

4. Ejecutar GET. HTTP 500; MPL: `No message found for id ORD-17001 in store LAB17_ORDENES.` Monitor confirmó que la entrada original estaba en el ámbito del escritor, no en el almacén global consultado.

   **Evidencia 17-12 - Error de lectura.**

   ![Evidencia 17-12](images/tema-17/17-12-mpl-get-entrada-no-encontrada.png)

   **Evidencia 17-13 - Entrada original en Monitor.**

   ![Evidencia 17-13](images/tema-17/17-13-monitor-data-store-entrada-existente.png)

5. Versionar el escritor `1.0.1` y cambiar visibilidad a `Global`. Reescribir `ORD-17001` falló por duplicado con qualifier `LAB17-Data_Store`. Cambiar la configuración no migró la entrada preexistente.

   **Evidencia 17-14 - Duplicado en MPL.**

   ![Evidencia 17-14](images/tema-17/17-14-mpl-write-entrada-duplicada.png)

6. Versionar escritor `1.0.2`, escribir `ORD-17002` en ámbito `Global` y actualizar el JSON para que coincida el ID. POST HTTP 200.

   **Evidencia 17-16 - Escritura global.**

   ![Evidencia 17-16](images/tema-17/17-16-postman-write-orden-17002-http-200.png)

7. Verificar `ORD-17002` en el Data Store global.

   **Evidencia 17-17 - Entrada global y payload.**

   ![Evidencia 17-17](images/tema-17/17-17-monitor-data-store-global-orden-17002.png)

8. Versionar lector `1.0.1`, configurar Get con Entry ID `ORD-17002`, desplegar y ejecutar GET. HTTP 200 y JSON esperado.

   **Evidencia 17-18 - Lectura desde otro iFlow.**

   ![Evidencia 17-18](images/tema-17/17-18-postman-data-store-get-global-orden-17002.png)

### Validación, problemas y aprendizajes

La lectura entre iFlows funcionó con visibilidad Global. Un mismo nombre de almacén no implica un mismo ámbito; la entrada inicial y la global se distinguieron en Monitor. La escritura duplicada fue rechazada con sobrescritura desactivada.

## 17.3 - Data Store Select y Delete

**Estado:** ✅ Completado

### Objetivo

Seleccionar mensajes almacenados, dirigir peticiones a Select o Delete mediante Router y comprobar la eliminación con una consulta posterior.

### Laboratorio paso a paso

1. Crear el iFlow independiente `LAB17-Data Store Select Delete` (ID `LAB17-Data_Store_Select_Delete`) y configurar HTTPS Sender `/lab17/datastore/select`, con autorización `ESBMessaging.send` y CSRF desactivado. Separar este artefacto evita alterar Write y Get.

   **Evidencia 17-19 - iFlow Select/Delete creado.**

   ![Evidencia 17-19](images/tema-17/17-19-iflow-data-store-select-delete-creado.png)

   **Evidencia 17-20 - Sender configurado.**

   ![Evidencia 17-20](images/tema-17/17-20-https-sender-select-configurado.png)

2. Incorporar `DS_Select_Ordenes` con `Data Store Name = LAB17_ORDENES`, `Visibility = Global`, `Number of Polled Messages = 1`, `Delete On Completion = off`. Desplegar y probar GET.

   **Evidencia 17-21 - Configuración inicial de Select.**

   ![Evidencia 17-21](images/tema-17/17-21-data-store-select-configurado.png)

3. Diagnosticar el fallo inicial: HTTP 500 y MPL `Unexpected character '{' (code 123) in prolog; expected '<'`. El paso Select identificó `ORD-17002`, cuyo payload almacenado era JSON. La prueba evidenció un intento de procesamiento XML de contenido que comenzaba por `{`. No se comprobó que todos los usos de Select con JSON fallen en cualquier configuración.

4. Preparar una entrada XML para aislar el formato de la prueba. En una versión nueva del escritor se configuró el Content Modifier con XML y Entry ID `ORD-17003`. La versión `1.0.3` se desplegó y se comprobó la escritura. El almacén de la prueba Select se separó como `LAB17_ORDENES_XML`, `Global`, y el lector Select se apuntó al mismo almacén. Esta separación evita que Select vuelva a recuperar la entrada JSON anterior.

   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <orden>
     <id>ORD-17003</id>
     <cliente>Cliente Laboratorio</cliente>
     <monto>150000</monto>
     <moneda>CLP</moneda>
     <estado>PENDIENTE</estado>
   </orden>
   ```

5. Ejecutar POST para escribir XML y comprobar `ORD-17003` en `LAB17_ORDENES_XML`. Ejecutar GET Select: HTTP 200, respuesta XML con `<messages><message id="ORD-17003">...`.

6. Crear una versión nueva de `LAB17-Data Store Select Delete` y añadir `DS_Delete_Ordenes`, apuntando a `LAB17_ORDENES_XML`, `Global`, Entry ID `ORD-17003`. Incorporar Router antes de las operaciones para que la eliminación sea una ruta explícita, no un efecto de Select.

7. Configurar Router: ruta Delete con `Expression Type = Non-XML`, condición `${header.OperacionDataStore} = 'DELETE'`; ruta Select como `Default Route`. Conectar ambas ramas al final del proceso y desplegar.

8. Mantener peticiones independientes en Postman:
   - `17.3 - Data Store Select`: GET `/lab17/datastore/select`, header `OperacionDataStore: SELECT`, sin body. HTTP 200 y XML con `ORD-17003`.
   - `17.3 - Data Store Delete`: DELETE a la misma dirección, header `OperacionDataStore: DELETE`, sin body. HTTP 200 y respuesta vacía.

   **Evidencia 17-37 - Router y Select correctos.** La respuesta contiene `ORD-17003` dentro de `<messages>`.

   ![Evidencia 17-37](images/tema-17/17-37-postman-router-select-exitoso.png)

   **Evidencia 17-38 - Delete ejecutado.** HTTP 200 con header `OperacionDataStore = DELETE`.

   ![Evidencia 17-38](images/tema-17/17-38-postman-data-store-delete-exitoso.png)

9. Verificar en Monitor que `LAB17_ORDENES_XML` deja de aparecer tras eliminar su única entrada, mientras el almacén original conserva sus entradas.

   **Evidencia 17-39 - Almacén XML sin entradas.** Vista posterior al Delete.

   ![Evidencia 17-39](images/tema-17/17-39-monitor-data-store-xml-eliminado.png)

10. Ejecutar nuevamente GET Select. HTTP 200 y body `<?xml version="1.0" encoding="UTF-8"?><messages/>`. El resultado confirma una selección vacía.

    **Evidencia 17-40 - Select sin entradas.**

    ![Evidencia 17-40](images/tema-17/17-40-postman-select-data-store-vacio.png)

### Resultado esperado y validación

Se comprobaron selección XML, separación de rutas por header, eliminación y respuesta de selección vacía. La prueba inicial con JSON falló y se resolvió en este escenario con un almacén independiente de mensajes XML. Véase el KBA del capítulo 17.

### Aprendizajes

Select y Get no son operaciones equivalentes: Get recuperó un JSON por ID en 17.2, mientras Select produjo un error de parseo en el escenario inicial y funcionó con la entrada XML. `Delete On Completion` permaneció desactivado en Select; la eliminación se ejecutó exclusivamente mediante la ruta Delete.

## 17.4 - Retención y expiración

**Estado:** 🟨 En curso - cálculo de fechas validado; eliminación automática no verificada.

### Objetivo

Comparar las fechas de retención de dos entradas almacenadas bajo diferentes configuraciones, sin eliminar la entrada inicial ni confundir fecha de expiración con eliminación efectiva.

### Laboratorio paso a paso

1. Reutilizar `LAB17-Data Store`, preservando su historial de versiones. En versión `1.0.5`, configurar `DS_Write_Orden` con `LAB17_EXPIRACION`, `Global`, Entry ID `ORD-17004`, umbral de alerta 2 días, expiración 30 días y cifrado activado. Mantener el identificador del XML del Content Modifier alineado con el Entry ID.

   **Evidencia 17-42 - Configuración inicial de expiración.** Write con períodos 2 y 30 días.

   ![Evidencia 17-42](images/tema-17/17-42-data-store-expiracion-configuracion-inicial.png)

2. Desplegar, ejecutar POST y comprobar en Monitor que `ORD-17004` está almacenada como XML en `LAB17_EXPIRACION`.

   **Evidencia 17-43 - Entrada persistida.** Payload XML de `ORD-17004`.

   ![Evidencia 17-43](images/tema-17/17-43-monitor-data-store-expiracion-orden-persistida.png)

3. Inspeccionar las fechas de `ORD-17004`:
   - `Created At`: 05/11/2026 18:51:58
   - `Due At`: 07/11/2026 18:51:58
   - `Retain Until`: 05/12/2026 18:51:58

   **Evidencia 17-44 - Fechas de la configuración 2/30.**

   ![Evidencia 17-44](images/tema-17/17-44-monitor-data-store-fechas-retencion-expiracion.png)

4. Crear versión `1.0.6` del mismo escritor. Cambiar únicamente los períodos y el identificador de la entrada: umbral de alerta 1 día, expiración 2 días, Entry ID `ORD-17005`, y `<id>ORD-17005</id>` en el XML. Mantener `LAB17_EXPIRACION`, visibilidad Global, cifrado y sobrescritura desactivada. La interfaz rechazó la combinación 1/1 con el mensaje `'Expiration Period' should be greater than 'Retention Threshold for Alerting'`; por ello se adoptó 1/2.

5. Guardar, desplegar versión `1.0.6`, ejecutar POST y consultar ambas entradas en Monitor. Se observaron los siguientes valores:

   | Campo | `ORD-17004` (2/30) | `ORD-17005` (1/2) |
   |---|---|---|
   | Created At | 05/11/2026 18:51:58 | 05/11/2026 19:02:57 |
   | Due At | 07/11/2026 18:51:58 | 06/11/2026 19:02:57 |
   | Retain Until | 05/12/2026 18:51:58 | 07/11/2026 19:02:57 |
   | Estado observado | Waiting | Waiting |

   **Evidencia 17-45 - Comparación de retención y expiración.** Las entradas conservan sus fechas propias pese al cambio de versión del escritor.

   ![Evidencia 17-45](images/tema-17/17-45-monitor-comparacion-retencion-expiracion.png)

### Resultado esperado y validación

La configuración 2/30 produjo fechas a +2 y +30 días; la configuración 1/2 produjo fechas a +1 y +2 días. La entrada previa conservó sus fechas después del nuevo despliegue. Se verificó además que el editor exige `Expiration Period > Retention Threshold for Alerting` para los valores probados.

### Limitación y comprobación pendiente

**No se ha validado la eliminación automática por expiración.** La entrada `ORD-17005` tiene `Retain Until = 07/11/2026 19:02:57` según Monitor. La comprobación debe realizarse después de esa fecha, revisando el almacén y, si corresponde, los registros de procesamiento. No debe interpretarse que el borrado ocurrirá exactamente a esa hora. Mientras no exista evidencia de la eliminación, 17.4 permanece `🟨 En curso`.

### Verificación de eliminación automática - evidencia provisional

La entrada `ORD-17005` tiene `Retain Until = 07/11/2026 19:02:57`, según Monitor. La comprobación de la eliminación automática se realizará después de esa fecha. La imagen siguiente es **un marcador documental, no una captura de SAP ni una prueba de eliminación**. La entrada debe verificarse en `Monitor → Manage Data Stores → LAB17_EXPIRACION` antes de sustituir la imagen. Si todavía aparece, registrar el resultado y volver a comprobar más adelante; no inferir que la eliminación se ejecuta exactamente a la hora `Retain Until`.

**Evidencia 17-60 - Verificación de eliminación automática de `ORD-17005` (pendiente).** Sustituir este archivo por la captura real una vez comprobado el resultado, conservando el mismo nombre y ruta.

![Evidencia 17-60 - Verificación de eliminación automática pendiente](images/tema-17/17-60-monitor-expiracion-automatica-ord-17005.png)

### Aprendizajes

La fecha `Due At` corresponde al umbral de alerta configurado; `Retain Until`, al período de expiración. La configuración se refleja en las entradas nuevas y no modificó las fechas ya registradas en la entrada anterior. La eliminación manual validada en 17.3 es distinta de la expiración automática.

## 17.5 - Idempotencia de órdenes y recuperación después de errores

**Estado:** ✅ Completado - pruebas funcionales y restauración del writer confirmadas el 06/11/2026.

### Objetivo

Evitar el reprocesamiento de órdenes con el mismo identificador, diferenciar el repositorio interno de Idempotent Process Call del Data Store de negocio y comprobar el reintento después de un error real de persistencia.

### Conceptos y prerrequisitos

Se reutilizaron HTTPS Sender, Content Modifier, Router, Data Store Write y Monitor/Trace. Se incorporó **Idempotent Process Call**, cuya clave de mensaje fue `${property.OrdenId}`. Se utilizó la carpeta Postman `Lab 17 - Data Store` y autorización OAuth previamente configurada. No registrar credenciales en las evidencias.

### Arquitectura y configuración

**iFlow:** `LAB17-Ordenes-Idempotencia`, package `CPI Learning Lab`.

`HTTPS Sender (/lab17/ordenes) → CM_Preparar_Orden → Idempotent Process Call → End`

El Content Modifier transfiere el header HTTP `OrdenId` a la Exchange Property `OrdenId`; el header figura en `Allowed Headers`. El Idempotent Process Call usa el proceso local `LIP_Procesar_Orden`, con Message ID `${property.OrdenId}` y **Skip Process Call for Duplicates desactivado**, para que los duplicados reciban una respuesta funcional propia.

Dentro de `LIP_Procesar_Orden`, el Router `RT_Validar_Duplicado` utiliza `${property.CamelDuplicateMessage} = 'true'` para la ruta de duplicados y una ruta predeterminada para órdenes nuevas:

- **Duplicada:** `CM_Respuesta_Orden_Duplicada` configura `CamelHttpResponseCode = 409`, `Content-Type = application/json` y Body **Expression** con `id = ${property.OrdenId}`, `estado = DUPLICADA`.
- **Nueva:** `DS_Write_Orden_Idempotente` escribe en `LAB17_ORDENES_IDEMPOTENTES`, `Global`, Entry ID `${property.OrdenId}`, sin sobrescritura y con cifrado. `CM_Respuesta_Orden_Nueva` configura HTTP 201, JSON y Body **Expression** con `id = ${property.OrdenId}`, `estado = REGISTRADA`.

La opción **Expression** es necesaria para evaluar el identificador dinámico; con **Constant** se observó que `${property.OrdenId}` aparecía literalmente en la respuesta.

**Evidencia 17-46 - Configuración inicial de Idempotent Process Call.** Message ID y proceso local.

![Evidencia 17-46](images/tema-17/17-46-idempotent-process-call-configuracion-inicial.png)

**Evidencia 17-47 - Arquitectura del iFlow de idempotencia.** Vista del diseño; la conexión efectiva del Idempotent Process Call se verificó posteriormente con Trace, porque la ubicación gráfica sobre una línea no garantizó inicialmente su inserción.

![Evidencia 17-47](images/tema-17/17-47-iflow-idempotencia-arquitectura-completa.png)

### Prueba 1 - Registro de una orden nueva

Enviar POST con header `OrdenId: ORD-17502` y body JSON de la orden. Se observó **HTTP 201**, `id = ORD-17502` y `estado = REGISTRADA`. Trace mostró el paso por Idempotent Process Call, Router y Data Store Write; Monitor confirmó una entrada persistida.

**Evidencia 17-48 - Registro inicial desde Postman.** HTTP 201 y respuesta de registro.

![Evidencia 17-48](images/tema-17/17-48-postman-idempotencia-orden-nueva.png)

**Evidencia 17-49 - Persistencia de la orden.** Entrada registrada en el Data Store global.

![Evidencia 17-49](images/tema-17/17-49-monitor-idempotencia-orden-persistida.png)

**Evidencia 17-50 - Trace de la ruta nueva.** Ejecución de Data Store Write.

![Evidencia 17-50](images/tema-17/17-50-trace-idempotencia-ruta-orden-nueva.png)

### Prueba 2 - Repetición de una orden procesada

Reenviar `ORD-17502` sin cambios. Se observó **HTTP 409**, `estado = DUPLICADA`. Trace mostró la ruta de duplicado sin ejecutar Data Store Write. El Data Store conservó una sola entrada con ese identificador.

**Evidencia 17-51 - Respuesta a duplicado.** HTTP 409.

![Evidencia 17-51](images/tema-17/17-51-postman-idempotencia-orden-duplicada.png)

**Evidencia 17-52 - Trace de duplicado.** Ruta sin nueva escritura.

![Evidencia 17-52](images/tema-17/17-52-trace-idempotencia-ruta-duplicado.png)

### Prueba 3 - Fallo de escritura y recuperación

Para provocar un conflicto real sin pasar previamente por el repositorio interno de Idempotent Process Call, se reutilizó temporalmente el writer independiente `LAB17-Data Store`. Se creó la versión 1.0.7, conservando la 1.0.6 de 17.4. El writer se configuró con Data Store `LAB17_ORDENES_IDEMPOTENTES`, ámbito Global, Entry ID `ORD-17503`, sobrescritura desactivada y un JSON literal con ese identificador. Se guardó y desplegó.

**Evidencia 17-53 - Writer temporal.** Parámetros del Data Store Write usados para preparar el conflicto.

![Evidencia 17-53](images/tema-17/17-53-writer-preparacion-conflicto-data-store.png)

El writer respondió **HTTP 200** y Monitor mostró `ORD-17503` junto a `ORD-17502`.

**Evidencia 17-54 - Creación de la entrada preexistente.** HTTP 200 del writer independiente.

![Evidencia 17-54](images/tema-17/17-54-postman-writer-preparacion-orden-17503.png)

**Evidencia 17-55 - Entrada preexistente.** Ambas órdenes en el Data Store global.

![Evidencia 17-55](images/tema-17/17-55-data-store-entrada-preexistente-17503.png)

Desde la petición independiente de Postman `17.5 - Excepcion al registrar orden` se envió `OrdenId: ORD-17503` al iFlow de idempotencia. La respuesta fue **HTTP 500**. El MPL registró: `An entry with id ORD-17503 does already exist in data store LAB17_ORDENES_IDEMPOTENTES`.

**Evidencia 17-56 - Error HTTP 500.** Petición que intenta registrar una entrada existente.

![Evidencia 17-56](images/tema-17/17-56-postman-idempotencia-error-http-500.png)

**Evidencia 17-57 - Diagnóstico en Monitor.** Error de entrada duplicada y MPL fallido.

![Evidencia 17-57](images/tema-17/17-57-monitor-idempotencia-error-entrada-duplicada.png)

Se eliminó exclusivamente la entrada de negocio `ORD-17503`, conservando `ORD-17502`, y se reenvió exactamente la misma petición. El reintento devolvió **HTTP 201**, `estado = REGISTRADA`; Monitor confirmó la nueva persistencia de `ORD-17503`. Un envío adicional devolvió **HTTP 409**, según la validación comunicada al cierre.

**Evidencia 17-58 - Reintento después del fallo.** HTTP 201 y orden registrada.

![Evidencia 17-58](images/tema-17/17-58-postman-idempotencia-reintento-tras-error.png)

**Evidencia 17-59 - Persistencia después del reintento.** `ORD-17503` reaparece junto a `ORD-17502`.

![Evidencia 17-59](images/tema-17/17-59-data-store-idempotencia-reintento-persistido.png)

### Restauración del writer

Después de la prueba, se utilizó **Version History → Revert → 1.0.6** en `LAB17-Data Store`. Se comprobó que regresaron los parámetros `LAB17_EXPIRACION`, Entry ID `ORD-17005`, umbral 1 día, expiración 2 días y el XML original. El usuario confirmó el despliegue final de la configuración restaurada. La versión temporal 1.0.7 queda como antecedente del ensayo, sin ser la configuración final del writer.

### Resultado y validación

| Escenario | Resultado observado |
|---|---|
| Primera orden `ORD-17502` | HTTP 201; persistencia confirmada |
| Repetición `ORD-17502` | HTTP 409; ruta sin Data Store Write |
| Entrada preexistente `ORD-17503` | HTTP 500; MPL confirma conflicto de escritura |
| Reintento tras retirar el conflicto | HTTP 201; persistencia confirmada |
| Repetición después del éxito | HTTP 409; confirmado por el usuario |
| Restauración de writer 17.4 | Configuración 1.0.6 recuperada y despliegue confirmado |

### Problemas encontrados y aprendizajes

- Colocar visualmente un componente sobre la línea del diagrama no demuestra que esté conectado: la conexión se validó con Trace.
- El Body **Constant** conserva literalmente `${property.OrdenId}`; el Body **Expression** resuelve la propiedad en ejecución.
- El Data Store de negocio y el repositorio interno de Idempotent Process Call tienen responsabilidades distintas. En una prueba anterior, eliminar la entrada de una orden procesada exitosamente no permitió reprocesarla; en la prueba de fallo, retirar el conflicto sí permitió el reintento.
- El comportamiento de reintento aquí documentado está demostrado para este iFlow y estas ejecuciones; no implica garantías universales sobre transacciones externas.

### Referencias

- [SAP Help Portal - Define Idempotent Process Call](https://help.sap.com/docs/cloud-integration/sap-cloud-integration/define-idempotent-process-call)
- [SAP Help Portal - Idempotent Process Call Handles Duplicates (With Alternative Response)](https://help.sap.com/docs/integration-suite/sap-integration-suite/idempotent-process-call-handles-duplicates-with-alternative-response)
- [SAP Help Portal - Define Data Store Write Operations](https://help.sap.com/docs/integration-suite/isuite-integrations-and-apis/define-data-store-write-operations)
- `docs/kba-y-hallazgos.md`

## Validación general

| Comprobación | Resultado |
|---|---|
| Escritura y persistencia | Validada |
| Lectura global entre iFlows | Validada |
| Select de XML | Validada |
| Delete explícito y consulta posterior vacía | Validada |
| Cálculo de fechas de retención y expiración | Validado |
| Eliminación automática por expiración | Pendiente de evidencia |
| Idempotencia 17.5: HTTP 201/409, fallo 500 y reintento 201 | Validada |

## Referencias

- [SAP Help Portal - SAP Integration Suite](https://help.sap.com/docs/integration-suite)
- `docs/METODOLOGIA-LABORATORIOS.md`
- `docs/kba-y-hallazgos.md`
