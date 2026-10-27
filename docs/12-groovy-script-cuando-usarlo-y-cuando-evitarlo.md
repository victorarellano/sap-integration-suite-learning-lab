# 12 — Groovy Script: cuándo usarlo y cuándo evitarlo

**Estado del tema:** 🟨 En curso
**Package:** `CPI Learning Lab`
**Alcance documentado:** fundamentos, headers/properties/body y procesamiento XML y JSON con Groovy, validados el 27-10-2026. Los siguientes laboratorios permanecen pendientes.

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
| 12.3 (XML) | Procesamiento XML | ✅ Completado | iFlow Started 1.0.2; HTTP 200; total 67500; 4/4 tests Postman |
| 12.3 (JSON) | Procesamiento JSON | ✅ Completado | iFlow Started 1.0.0; HTTP 200; total numérico 67500; tests Postman aprobados |
| 12.4 | Reglas de negocio | ⬜ Pendiente | — |
| 12.5 | Alternativas nativas | ⬜ Pendiente | — |
| 12.6 | Buenas prácticas y validación | ⬜ Pendiente | — |

> Los ejercicios XML y JSON utilizan la identificación 12.3 en los artefactos y peticiones existentes. Se distinguen como **12.3 (XML)** y **12.3 (JSON)** en este documento para no alterar los nombres ya validados. Los siguientes ejercicios conservan su numeración prevista.

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

## 12.3 (XML) — Procesamiento XML con Groovy

**Estado:** ✅ Completado
**iFlow:** `LAB12_03_Groovy_Procesamiento_XML`
**Versión runtime final observada:** `1.0.2`
**Postman:** petición independiente de procesamiento XML en `Lab 12 Groovy Script`.

### Objetivo y conceptos

Procesar un pedido XML mediante Groovy, leer elementos repetidos y calcular el importe total. Se utiliza `XmlSlurper` para recorrer la estructura y se devuelve una respuesta JSON. El ejercicio permite comparar el acceso a nodos XML con el acceso a objetos JSON del laboratorio siguiente.

### Laboratorio y resultado comprobado

1. Crear y configurar un iFlow independiente con HTTPS Sender autenticado, sin modificar los laboratorios anteriores.
2. Incorporar un paso Groovy con el recurso de procesamiento XML, utilizando `XmlSlurper` para leer `Pedido`, `Id`, `Cliente` y los elementos `Producto`.
3. Para cada producto, multiplicar `Precio` por `Cantidad` y acumular el resultado. Construir una respuesta JSON con `idPedido`, `cliente` y `totalPedido`.
4. Guardar y desplegar; comprobar el estado `Started` del artefacto. La versión final observada fue `1.0.2`.
5. Enviar desde Postman un pedido XML con identificador `1001`, cliente `Empresa Demo` y tres productos: 15000 × 2, 8500 × 3 y 12000 × 1.
6. Verificar respuesta HTTP 200, total calculado y aserciones automatizadas.

**Entrada utilizada:**

```xml
<Pedido>
  <Id>1001</Id>
  <Cliente>Empresa Demo</Cliente>
  <Productos>
    <Producto><Precio>15000</Precio><Cantidad>2</Cantidad></Producto>
    <Producto><Precio>8500</Precio><Cantidad>3</Cantidad></Producto>
    <Producto><Precio>12000</Precio><Cantidad>1</Cantidad></Producto>
  </Productos>
</Pedido>
```

**Respuesta observada:**

```json
{
  "idPedido": "1001",
  "cliente": "Empresa Demo",
  "totalPedido": "67500"
}
```

**Validación:** despliegue `Started`, HTTP `200 OK` y **4/4 tests Postman exitosos**.

**Evidencias 12-32 y 12-33 — Corrección del script y despliegue XML**
Se verificaron la corrección del recurso Groovy y el despliegue final `Started`. Las referencias a imágenes de estos dos pasos se incorporarán cuando se confirme el nombre exacto de cada archivo en `docs/images/tema-12/`.

**Evidencia 12-34 — Respuesta XML procesada desde Postman**
La respuesta JSON contiene el pedido `1001`, el cliente y el total `67500`.

![Evidencia 12-34 — Respuesta Postman XML](images/tema-12/12-34-postman-procesamiento-xml-respuesta-correcta.png)

**Evidencia 12-35 — Tests de procesamiento XML**
Cuatro comprobaciones automatizadas exitosas.

![Evidencia 12-35 — Tests Postman XML](images/tema-12/12-35-postman-procesamiento-xml-tests.png)

### Problemas encontrados y aprendizajes

Durante el ajuste del script XML se requirió una corrección de lectura de los datos y un nuevo despliegue. El resultado final fue verificado con Postman. La estructura XML utiliza nodos repetidos para los productos y sus valores se leen inicialmente como texto; por ello, el cálculo requiere conversión numérica explícita. La respuesta validada presenta `totalPedido` como cadena JSON, a diferencia del ejercicio JSON posterior.

---

## 12.3 (JSON) — Procesamiento JSON con Groovy

**Estado:** ✅ Completado
**iFlow:** `LAB12_03_Groovy_Procesamiento_JSON`
**Versión runtime observada:** `1.0.0`
**Endpoint relativo:** `/http/lab12/groovy/procesamiento-json`
**Postman:** `Lab 12 Groovy Script / 12.3 - Procesamiento JSON`.

### Objetivo y conceptos

Procesar el mismo pedido del laboratorio XML, esta vez como JSON, utilizando `JsonSlurper`, cálculo decimal con `BigDecimal` y serialización mediante `JsonOutput`. Se compara la preservación de tipos de datos con los resultados de los conversores estándar del capítulo 11.

### Laboratorio paso a paso

1. Crear el iFlow independiente `LAB12_03_Groovy_Procesamiento_JSON` dentro de `CPI Learning Lab`, sin alterar el iFlow XML.

   **Evidencia 12-36 — iFlow JSON inicial**
   Artefacto creado, todavía sin desplegar.

   ![Evidencia 12-36 — iFlow JSON](images/tema-12/12-36-iflow-groovy-procesamiento-json.png)

2. Configurar HTTPS Sender con `Address = /lab12/groovy/procesamiento-json`, `Authorization = User Role`, `User Role = ESBMessaging.send` y `CSRF Protected` desactivado para esta prueba autenticada.

   **Motivo:** exponer un endpoint separado para comparar formatos sin afectar los ejercicios anteriores.

   **Evidencia 12-37 — HTTPS Sender JSON**
   Dirección, autorización y protección CSRF configuradas.

   ![Evidencia 12-37 — HTTPS Sender JSON](images/tema-12/12-37-https-sender-procesamiento-json.png)

3. Insertar el componente Groovy Script `Procesar Pedido JSON` entre Start y End y crear el recurso `GRV_ProcesarPedidoJSON.groovy`.

   **Evidencia 12-38 — Componente Groovy JSON**
   Paso de procesamiento incorporado al flujo.

   ![Evidencia 12-38 — Componente Groovy](images/tema-12/12-38-groovy-componente-procesamiento-json.png)

4. Implementar el procesamiento con lectura del body mediante `Reader`, cálculo de importes con `BigDecimal` y generación de JSON con `JsonOutput`:

   ```groovy
   import com.sap.gateway.ip.core.customdev.util.Message
   import groovy.json.JsonSlurper
   import groovy.json.JsonOutput

   def Message processData(Message message) {
       def pedido = new JsonSlurper().parse(
           message.getBody(java.io.Reader)
       )

       BigDecimal total = 0
       pedido.productos.each { producto ->
           BigDecimal precio = new BigDecimal(producto.precio.toString())
           BigDecimal cantidad = new BigDecimal(producto.cantidad.toString())
           total += precio * cantidad
       }

       def respuesta = [
           idPedido: pedido.id.toString(),
           cliente: pedido.cliente,
           totalPedido: total
       ]

       message.setBody(JsonOutput.toJson(respuesta))
       message.setHeader("Content-Type", "application/json")
       return message
   }
   ```

   **Motivo:** `Reader` evita crear explícitamente una cadena intermedia antes del parseo; `JsonSlurper` sigue construyendo el objeto JSON en memoria. `BigDecimal` permite el cálculo decimal y `JsonOutput` serializa el resultado sin convertir el total a texto.

   **Validación de diseño:** el editor dejó de mostrar la observación de `General Script Checks` tras utilizar `Reader`.

   **Evidencia 12-39 — Código Groovy JSON**
   Recurso de script y lógica de cálculo.

   ![Evidencia 12-39 — Código Groovy JSON](images/tema-12/12-39-groovy-codigo-procesamiento-json.png)

5. Asociar `/script/GRV_ProcesarPedidoJSON.groovy` al paso con `Script Function = processData`.

   **Evidencia 12-40 — Recurso asociado**
   Asociación del archivo y de la función ejecutable.

   ![Evidencia 12-40 — Recurso Groovy asociado](images/tema-12/12-40-groovy-recurso-asociado-json.png)

6. Guardar y desplegar el iFlow. Se observó `Started`, versión `1.0.0`, sin necesidad de un Content Modifier adicional, pues Groovy construye el body y define el header de respuesta.

   **Evidencia 12-41 — Despliegue JSON**
   Estado `Started`, versión y endpoint del artefacto.

   ![Evidencia 12-41 — Despliegue JSON](images/tema-12/12-41-iflow-json-desplegado-started.png)

7. Crear la petición Postman `12.3 - Procesamiento JSON` con método POST, autenticación OAuth 2.0 Client Credentials y `Content-Type: application/json`. Enviar:

   ```json
   {
     "id": 1001,
     "cliente": "Empresa Demo",
     "productos": [
       {"precio": 15000, "cantidad": 2},
       {"precio": 8500, "cantidad": 3},
       {"precio": 12000, "cantidad": 1}
     ]
   }
   ```

   **Respuesta observada:** HTTP `200 OK`.

   ```json
   {
     "idPedido": "1001",
     "cliente": "Empresa Demo",
     "totalPedido": 67500
   }
   ```

   **Evidencia 12-42 — Respuesta Postman JSON**
   Pedido procesado y total `67500` devuelto como número JSON.

   ![Evidencia 12-42 — Respuesta JSON](images/tema-12/12-42-postman-json-respuesta.png)

8. Ejecutar las pruebas de Postman sobre el código HTTP, los campos esperados y los tipos. La ejecución inicial aprobó **4/4 tests**. Después se comprobó también el valor exacto del total, el identificador y el cliente mediante un test adicional, con resultado **4/4** en la configuración final (sustituyendo la prueba de tiempo de respuesta).

   **Evidencia 12-43 — Tests Postman JSON**
   Validación de respuesta y aserciones automáticas. Esta evidencia se conserva como única captura de tests; no se duplica la imagen de la comprobación posterior.

   ![Evidencia 12-43 — Tests Postman JSON](images/tema-12/12-43-postman-json-tests.png)

### Resultado, problemas encontrados y aprendizajes

El procesamiento JSON quedó validado con HTTP 200, respuesta con los tres campos esperados, `totalPedido = 67500` como número JSON y tests Postman exitosos. Se atendió una observación del editor sobre el uso de `JsonSlurper` con `String` antes del despliegue, cambiando a `Reader`. No se requirió una nueva versión ni se produjo un error de runtime durante la validación final. A diferencia del procesamiento XML documentado, el total del JSON se devolvió como número, lo que permite verificar su tipo directamente.

---

## Validación acumulada del capítulo

| Laboratorio | iFlow | Validación práctica | Estado |
|---|---|---|---|
| 12.1 | `LAB12_01_Groovy_Fundamentos` | Started, HTTP 200, longitud 33, 4/4 tests | ✅ Completado |
| 12.2 | `LAB12_02_Groovy_Headers_Properties_Body` | Started, HTTP 200, header permitido, longitud 15, transformación, 4/4 tests | ✅ Completado |
| 12.3 (XML) | `LAB12_03_Groovy_Procesamiento_XML` | Started 1.0.2, HTTP 200, total 67500, 4/4 tests | ✅ Completado |
| 12.3 (JSON) | `LAB12_03_Groovy_Procesamiento_JSON` | Started 1.0.0, HTTP 200, total numérico 67500, tests Postman aprobados | ✅ Completado |

**Estado del tema 12:** 🟨 En curso. Los laboratorios de reglas de negocio, alternativas nativas y buenas prácticas todavía no están validados.

## Referencias

- [SAP Help Portal — Cloud Integration](https://help.sap.com/docs/integration-suite/sap-integration-suite/cloud-integration)
- [Apache Camel — Simple Language](https://camel.apache.org/components/latest/languages/simple-language.html)
