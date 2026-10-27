# 12 — Groovy Script: cuándo usarlo y cuándo evitarlo

**Estado del tema:** 🟨 En curso
**Package:** `CPI Learning Lab`
**Alcance documentado:** laboratorios 12.1 y 12.2, validados el 27-10-2026.

## Objetivo

Comprender el uso de Groovy Script en SAP Cloud Integration para consultar y modificar mensajes, distinguir Headers de Exchange Properties y decidir cuándo utilizar código y cuándo preferir componentes nativos.

## Prerrequisitos

- Acceso a SAP Integration Suite / Cloud Integration y al package `CPI Learning Lab`.
- Permisos para diseñar, versionar, desplegar y monitorizar iFlows.
- Postman con autenticación OAuth 2.0 Client Credentials operativa, sin publicar secretos.
- Conocimientos previos de HTTPS Sender, Content Modifier, Headers, Exchange Properties y expresiones Camel.

## Conceptos

- **Groovy Script:** paso de procesamiento programable para operaciones que no conviene o no es posible resolver mediante los componentes estándar.
- **Message Body:** contenido principal del mensaje, accesible con `getBody` y modificable con `setBody`.
- **Message Header:** metadato del mensaje, accesible con `getHeader` y modificable con `setHeader`. La disponibilidad de headers HTTP personalizados depende de la configuración de propagación.
- **Exchange Property:** dato de contexto interno del intercambio, accesible con `getProperty` y modificable con `setProperty`.
- **Content Modifier:** componente nativo preferible para asignaciones simples de headers, properties y bodies sin lógica programática.
- **Expression vs Constant:** en Message Body, `Expression` resuelve expresiones como `${property.Nombre}`; `Constant` conserva el texto literal.

## Índice y estado de microaprendizajes

| ID | Microaprendizaje | Estado | Validación |
|---|---|---|---|
| 12.1 | Fundamentos de Groovy | ✅ Completado | iFlow iniciado; longitud del body calculada; HTTP 200; 4/4 tests Postman |
| 12.2 | Headers, Properties y Body | ✅ Completado | iFlow iniciado; header personalizado, propiedades y transformación verificados; HTTP 200; 4/4 tests Postman |
| 12.3 | Procesamiento XML | ⬜ Pendiente | — |
| 12.3 (JSON) | Procesamiento JSON | ⬜ Pendiente | — |
| 12.4 | Reglas de negocio | ⬜ Pendiente | — |
| 12.5 | Alternativas nativas | ⬜ Pendiente | — |
| 12.6 | Buenas prácticas y validación | ⬜ Pendiente | — |

> La nomenclatura de Postman muestra dos peticiones 12.3 (XML y JSON); se conserva provisionalmente hasta acordar su numeración definitiva, sin alterar las peticiones ya creadas.

---

## 12.1 — Fundamentos de Groovy

**Estado:** ✅ Completado
**iFlow:** `LAB12_01_Groovy_Fundamentos`
**Versión desplegada:** `1.0.1`
**Endpoint relativo:** `/http/lab12/groovy/fundamentos`
**Postman:** `Lab 12 Groovy Script / 12.1 - Fundamentos de Groovy`

### Objetivo

Leer el body de una solicitud mediante Groovy, calcular su longitud, almacenarla como Exchange Property y devolverla en una respuesta JSON.

### Laboratorio paso a paso

#### 1. Crear el iFlow y configurar el HTTPS Sender

Crear el iFlow dentro de `CPI Learning Lab`, conectar Sender con Start mediante HTTPS y conservar Start → End.

| Parámetro | Valor |
|---|---|
| Address | `/lab12/groovy/fundamentos` |
| Authorization | `User Role` |
| User Role | `ESBMessaging.send` |
| CSRF Protected | Desactivado en este laboratorio |

**Motivo:** exponer un endpoint independiente para probar el procesamiento Groovy sin introducir otros componentes de integración.

**Evidencia 12-01 — Creación del iFlow**
Estructura inicial del artefacto.

![Evidencia 12-01 — iFlow inicial](images/tema-12/12-01-iflow-creado.png)

**Evidencia 12-02 — HTTPS Sender**
Endpoint y autorización de entrada.

![Evidencia 12-02 — HTTPS Sender](images/tema-12/12-02-https-sender-configuracion.png)

#### 2. Incorporar Groovy Script

Insertar un paso Groovy Script `Calcular Longitud Body`, crear el recurso `GRV_CalcularLongitudBody.groovy` y seleccionar la función `processData`.

```groovy
import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    String body = message.getBody(String)
    int longitud = body.length()
    message.setProperty("LongitudBody", longitud)
    return message
}
```

**Motivo:** `getBody(String)` obtiene el texto de entrada; `length()` cuenta sus caracteres y `setProperty` conserva el resultado para pasos posteriores sin sustituir el body original.

**Evidencia 12-05 — Recurso Groovy asociado**
Script `GRV_CalcularLongitudBody.groovy` y función `processData` configurados.

![Evidencia 12-05 — Script Groovy asociado](images/tema-12/12-05-groovy-codigo-editor.png)

#### 3. Construir la respuesta con Content Modifier

Insertar `CM_Respuesta_Longitud` después de Groovy. En **Message Body**, seleccionar `Type = Expression` y configurar:

```json
{
  "laboratorio": "12.1",
  "longitudBody": "${property.LongitudBody}",
  "resultado": "Procesado con Groovy"
}
```

En **Message Header**, crear `Content-Type`, `Source Type = Constant`, `Source Value = application/json`. Si `Data Type` no es editable, dejarlo sin modificar.

**Motivo:** la expresión recupera la propiedad creada por Groovy y el header identifica el formato HTTP de salida. El tipo `Constant` en el body devolvería la expresión como texto literal.

**Evidencia 12-06 — Respuesta con Expression**
Content Modifier con sustitución de `${property.LongitudBody}`.

![Evidencia 12-06 — Message Body Expression](images/tema-12/12-06-content-modifier-respuesta.png)

**Evidencia 12-07 — Header Content-Type**
Configuración del header de respuesta JSON.

![Evidencia 12-07 — Content-Type](images/tema-12/12-07-content-type-respuesta.png)

#### 4. Versionar y desplegar

Guardar la versión `1.0.1` y desplegar. En Manage Integration Content se comprobó el estado `Started` y el endpoint publicado.

**Evidencia 12-08 — Versión del iFlow**
Información de versión `1.0.1`.

![Evidencia 12-08 — Versión](images/tema-12/12-08-iflow-version-1-0-1.png)

**Evidencia 12-09 — Despliegue exitoso**
Artefacto iniciado en el runtime.

![Evidencia 12-09 — Started](images/tema-12/12-09-iflow-desplegado-started.png)

#### 5. Ejecutar y validar con Postman

Crear la petición independiente `12.1 - Fundamentos de Groovy` dentro de `Lab 12 Groovy Script`, método POST, autenticación OAuth 2.0 y `Content-Type: application/json`.

Body utilizado (con formato multilínea en Postman):

```json
{
  "mensaje": "Hola Groovy"
}
```

**Motivo:** el script mide el body recibido como texto, incluidos espacios y saltos de línea. La longitud observada fue `33`, correspondiente al payload realmente enviado en la prueba, no a una versión compactada del JSON.

**Evidencia 12-10 — Organización de Postman**
Carpeta del capítulo y petición independiente 12.1.

![Evidencia 12-10 — Carpeta Postman](images/tema-12/12-10-postman-carpeta-laboratorio.png)

**Evidencia 12-11 — Respuesta calculada**
Postman recibió HTTP 200 y `longitudBody = "33"`.

![Evidencia 12-11 — Respuesta Postman](images/tema-12/12-11-postman-body-json.png)

El test que compara la longitud de entrada con el valor devuelto es:

```javascript
pm.test("Groovy calcula correctamente la longitud", function () {
    const response = pm.response.json();
    const body = pm.request.body.raw;
    pm.expect(Number(response.longitudBody)).to.eql(body.length);
});
```

**Resultado validado:** HTTP `200 OK`; respuesta con `laboratorio = "12.1"`, `longitudBody = "33"` y `resultado = "Procesado con Groovy"`; **4/4 tests Postman exitosos**, incluido el cálculo de longitud.

**Evidencia 12-13 — Tests automáticos exitosos**
Comparación de la longitud real del request con la propiedad calculada por Groovy.

![Evidencia 12-13 — Validación longitud](images/tema-12/12-13-postman-validacion-longitud.png)

### Problemas encontrados y aprendizajes

- Para resolver `${property.LongitudBody}` dentro del body del Content Modifier fue necesario `Type = Expression`; `Constant` devolvía la referencia literalmente.
- El resultado depende del texto exacto enviado, incluidos espacios y saltos de línea.
- Para esta operación sencilla, Groovy es útil para aprender la API de mensajes, pero debe compararse con alternativas nativas antes de incorporarlo a un diseño productivo.

---

## 12.2 — Headers, Properties y Body

**Estado:** ✅ Completado
**iFlow:** `LAB12_02_Groovy_Headers_Properties_Body`
**Versión desplegada observada:** `1.0.1` (antes de ajustar Allowed Headers); la versión del despliegue posterior no quedó identificada en las capturas disponibles.
**Endpoint relativo:** `/http/lab12/groovy/headers-properties-body`
**Postman:** `Lab 12 Groovy Script / 12.2 - Headers, Properties y Body`

### Objetivo

Leer un header personalizado y el body de entrada, crear un header y dos Exchange Properties, transformar el body a mayúsculas y comprobar el resultado mediante una respuesta JSON.

### Laboratorio paso a paso

#### 1. Crear el iFlow y configurar HTTPS Sender

Crear `LAB12_02_Groovy_Headers_Properties_Body` con Start y End. Conectar HTTPS Sender:

| Parámetro | Valor |
|---|---|
| Address | `/lab12/groovy/headers-properties-body` |
| Authorization | `User Role` |
| User Role | `ESBMessaging.send` |
| CSRF Protected | Desactivado en este laboratorio |

**Motivo:** aislar la práctica de manipulación de mensajes sin modificar el iFlow del laboratorio 12.1.

**Evidencia 12-14 — iFlow inicial**
Nuevo artefacto antes de configurar el procesamiento.

![Evidencia 12-14 — iFlow inicial](images/tema-12/12-14-iflow-groovy-headers-properties-body.png)

**Evidencia 12-15 — HTTPS Sender**
Dirección y permisos del endpoint 12.2.

![Evidencia 12-15 — HTTPS Sender](images/tema-12/12-15-https-sender-headers-properties-body.png)

#### 2. Crear el recurso Groovy

Insertar el paso `Procesar Headers`, crear `GRV_ProcesarHeadersPropertiesBody.groovy` y asociarlo mediante la función `processData`.

```groovy
import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    String bodyOriginal = message.getBody(String)
    String origen = message.getHeader("X-Origen", String)
    if (!origen) {
        origen = "No informado"
    }
    message.setHeader("X-Procesado-Por", "Groovy")
    message.setProperty("OrigenPeticion", origen)
    message.setProperty("LongitudOriginal", bodyOriginal.length())
    message.setBody(bodyOriginal.toUpperCase())
    return message
}
```

**Motivo:** demostrar en un mismo paso la lectura y escritura de headers, el almacenamiento de datos internos en properties y la modificación del body.

**Evidencia 12-16 — Paso Groovy incorporado**
Componente entre Start y End.

![Evidencia 12-16 — Paso Groovy](images/tema-12/12-16-groovy-componente-headers-properties-body.png)

**Evidencia 12-17 — Código Groovy**
Script que procesa headers, properties y body.

![Evidencia 12-17 — Código Groovy](images/tema-12/12-17-groovy-headers-properties-body-codigo.png)

#### 3. Construir la respuesta JSON

Agregar `CM_Respuesta_Headers_Properties` después del script. En **Message Body** seleccionar `Type = Expression`:

```json
{
  "origen": "${property.OrigenPeticion}",
  "longitudOriginal": "${property.LongitudOriginal}",
  "procesadoPor": "${header.X-Procesado-Por}",
  "bodyTransformado": "${body}"
}
```

En **Message Header** crear `Content-Type = application/json` con `Source Type = Constant`.

**Motivo:** mostrar los datos internos en una respuesta HTTP sin añadir un segundo script. La interpolación directa de `${body}` es apropiada para el texto simple usado en esta prueba; para contenido arbitrario se necesita una serialización JSON segura.

**Evidencia 12-18 — Content Modifier de respuesta**
Body con expresiones para properties, header y mensaje transformado.

![Evidencia 12-18 — Respuesta JSON](images/tema-12/12-18-content-modifier-headers-properties-respuesta.png)

**Evidencia 12-19 — Header de respuesta**
Content-Type configurado como application/json.

![Evidencia 12-19 — Content-Type](images/tema-12/12-19-content-type-headers-properties.png)

#### 4. Desplegar y permitir el header de entrada

El primer despliegue de la versión `1.0.1` quedó en `Started`. Para que el header HTTP personalizado `X-Origen` fuera accesible al script, se configuró en **Integration Flow → Runtime Configuration → Allowed Header(s)** el valor `X-Origen` y se comprobó nuevamente la ejecución.

**Motivo:** los headers HTTP personalizados no deben suponerse automáticamente disponibles en el contexto de mensaje; su propagación debe configurarse y verificarse.

**Evidencia 12-20 — Despliegue del iFlow**
Artefacto `LAB12_02_Groovy_Headers_Properties_Body` iniciado.

![Evidencia 12-20 — iFlow Started](images/tema-12/12-20-iflow-headers-properties-deployed.png)

**Evidencia 12-21 — Procesamiento validado desde Postman**
Tras permitir el header `X-Origen`, la respuesta devuelve `origen = "Postman"`, longitud 15 y body en mayúsculas.

![Evidencia 12-21 — Respuesta Postman](images/tema-12/12-21-postman-headers-properties-body-validado.png)

#### 5. Verificar con Postman

Enviar POST al endpoint 12.2, autenticado con OAuth 2.0, con:

| Elemento | Valor |
|---|---|
| Header `X-Origen` | `Postman` |
| Header `Content-Type` | `text/plain` |
| Body raw/Text | `Hola Groovy CPI` |

Respuesta observada:

```json
{
  "origen": "Postman",
  "longitudOriginal": "15",
  "procesadoPor": "Groovy",
  "bodyTransformado": "HOLA GROOVY CPI"
}
```

Los tests automáticos comprobaron el HTTP 200, el tiempo de respuesta inferior a 1000 ms, la igualdad entre `Number(response.longitudOriginal)` y `pm.request.body.raw.length`, además de los valores exactos de `origen`, `procesadoPor` y `bodyTransformado`.

**Resultado validado:** HTTP `200 OK` y **4/4 tests Postman exitosos**.

**Evidencia 12-22 — Tests Postman exitosos**
Aserciones de estado, latencia, longitud y valores de respuesta.

![Evidencia 12-22 — Tests Postman](images/tema-12/12-22-postman-tests-headers-properties-body.png)

### Problemas encontrados y aprendizajes

- Sin incluir `X-Origen` en **Allowed Header(s)**, la ejecución devolvió `origen = "No informado"` pese a que Postman enviaba el header. Tras configurar la lista permitida, el valor observado fue `Postman`.
- Un HTTP 200 no demuestra que todos los campos del mensaje sean correctos: las aserciones deben comprobar los valores funcionales.
- Los Headers permiten transportar metadatos; las Exchange Properties conservan información durante el intercambio sin exponerse automáticamente al cliente HTTP.
- `Content Modifier` es suficiente para construir esta respuesta simple; el script se reservó para la manipulación programática que se quería estudiar.

---

## Validación acumulada del capítulo

| Laboratorio | iFlow | Validación práctica | Estado |
|---|---|---|---|
| 12.1 | `LAB12_01_Groovy_Fundamentos` | Started, HTTP 200, longitud 33, 4/4 tests | ✅ Completado |
| 12.2 | `LAB12_02_Groovy_Headers_Properties_Body` | Started, HTTP 200, header permitido, longitud 15, transformación, 4/4 tests | ✅ Completado |

**Estado del tema 12:** 🟨 En curso. Los laboratorios siguientes aún no están validados.

## Referencias

- [SAP Help Portal — Cloud Integration](https://help.sap.com/docs/integration-suite/sap-integration-suite/cloud-integration)
- [Apache Camel — Simple Language](https://camel.apache.org/components/latest/languages/simple-language.html)
