# 20 - OData en Integration Suite

**Estado del tema:** 🟨 En curso - laboratorios 20.1 a 20.4 completados; 20.5 y 20.6 pendientes.
**Package de referencia:** `CPI Learning Lab`
**Servicio de pruebas:** Northwind OData V2 (`https://services.odata.org/V2/Northwind/Northwind.svc/`)

## Objetivo y contexto

Comprender el modelo de entidades y metadatos de OData y construir consultas verificables antes de consumir servicios OData desde SAP Cloud Integration. Se reutilizan Postman y los conceptos HTTP del capítulo 19, sin modificar sus iFlows.

## Alcance del capítulo

| Laboratorio | Propósito | Estado |
|---|---|---|
| 20.1 | Service Document, metadata, entidad, clave y EntitySet | ✅ Completado |
| 20.2 | `$filter`, `$select`, `$orderby`, `$top`, `$expand` y consulta combinada | ✅ Completado |
| 20.3 | OData Receiver V2 en CPI | ✅ Completado |
| 20.4 | Selección dinámica del recurso OData mediante header y property | ✅ Completado |
| 20.5 | OData V4 | ⬜ Pendiente |
| 20.6 | Paginación y operaciones de escritura, condicionadas al servicio | ⬜ Pendiente |

## Prerrequisitos

- Postman y acceso HTTPS al servicio público Northwind OData V2.
- Conocimientos de GET, headers y JSON/XML adquiridos en el capítulo 19.
- Para laboratorios posteriores: acceso a Design y Monitor de SAP Integration Suite.
- No registrar secretos ni credenciales en el repositorio.

## Arquitectura de los laboratorios 20.1 y 20.2

```mermaid
flowchart LR
  P["Postman"] -->|"GET HTTPS"| N["Northwind OData V2"]
  N --> M["Service Document / $metadata"]
  N --> E["Customers y Orders"]
  M --> P
  E --> P
```

## 20.1 - Fundamentos, entidades y metadata

**Estado:** ✅ Completado

### Objetivo y conceptos

Identificar el contrato autodescriptivo de un servicio OData V2. El **Service Document** publica colecciones; `$metadata` describe tipos (`EntityType`), conjuntos (`EntitySet`), claves (`Key`) y relaciones (`NavigationProperty`). `Customer` es el tipo; `Customers` es el conjunto publicado.

**Prerrequisitos:** Postman, servicio Northwind accesible; no se requiere iFlow.

### Procedimiento y evidencias

1. Crear la carpeta `20 - OData en Integration Suite` dentro de la colección `CPI Learning Lab` y la petición `20.1 - GET Service Document`: método `GET`, URL raíz `https://services.odata.org/V2/Northwind/Northwind.svc/`, `No Auth`, `Accept: application/xml`, sin body. La raíz permite descubrir colecciones sin conocer su estructura. Se recibió **HTTP 200**, XML con colecciones como `Categories`, `Customers`, `Employees` y `Order_Details`.

   **Evidencia 20-01 - Service Document.** URL, HTTP 200 y colecciones del servicio.

   ![Evidencia 20-01 - Service Document](images/tema-20/20-01-postman-odata-service-document.png)

2. Crear `20.1 - GET Metadata` con `GET https://services.odata.org/V2/Northwind/Northwind.svc/$metadata`, `No Auth` y `Accept: application/xml`. Se recibió **HTTP 200** con XML EDMX y tipos `Category`, `Customer`, `Employee`, `Order`, entre otros. El documento declara `DataServiceVersion="1.0"` y `MaxDataServiceVersion="2.0"`; el segundo valor indica la versión máxima declarada y no debe confundirse con la versión de cada entidad.

   **Evidencia 20-02 - Metadatos EDMX.** Estructura EDMX, namespaces y tipos de entidad.

   ![Evidencia 20-02 - Metadatos EDMX](images/tema-20/20-02-postman-odata-metadata.png)

3. En el XML, expandir `EntityType Name="Customer"`. La clave se define mediante `PropertyRef Name="CustomerID"`; `CustomerID` es `Edm.String`, `Nullable="false"`, `MaxLength="5"` y `FixedLength="true"`. `CompanyName` es obligatoria, de longitud máxima 40. También se identificaron `City`, `Country`, `Orders` y `CustomerDemographics`. Las relaciones permiten navegar a recursos relacionados.

   **Evidencia 20-03 - Estructura de Customer.** Clave, propiedades y relaciones de navegación.

   ![Evidencia 20-03 - Estructura de Customer](images/tema-20/20-03-postman-odata-customer-entitytype.png)

4. Crear `20.1 - GET Customer por clave`: `GET https://services.odata.org/V2/Northwind/Northwind.svc/Customers('ALFKI')`, `No Auth`, sin body y sin header `Accept` explícito en la ejecución satisfactoria. Las comillas corresponden a la clave `Edm.String`. Se recibió **HTTP 200**, una entrada Atom/XML con `CustomerID=ALFKI`, `CompanyName=Alfreds Futterkiste`, `City=Berlin` y `Country=Germany`. La propiedad `Region` se devolvió como nula.

   **Evidencia 20-04 - Consulta por clave.** Respuesta HTTP 200 y propiedades de `ALFKI`.

   ![Evidencia 20-04 - Consulta por clave](images/tema-20/20-04-postman-odata-customer-por-clave.png)

5. En `$metadata`, localizar el contenedor `NorthwindEntities` y comprobar `EntitySet Name="Customers" EntityType="NorthwindModel.Customer"`. Esta declaración conecta el nombre de la colección con el tipo que define sus propiedades y clave.

   **Evidencia 20-05 - Relación EntitySet/EntityType.** Contenedor y mapeo `Customers` → `NorthwindModel.Customer`.

   ![Evidencia 20-05 - Relación EntitySet/EntityType](images/tema-20/20-05-postman-odata-entityset-customers.png)

### Resultado y validación

Las cinco comprobaciones se realizaron en Postman. Se validó la publicación de colecciones, el contrato EDMX, la clave, las relaciones y la lectura de una entidad real.

### Problema técnico y solución comprobada

La consulta individual devolvió **HTTP 415 Unsupported Media Type** al solicitar `Accept: application/xml`. El cuerpo del error indicó incompatibilidad entre los tipos MIME aceptados y los formatos de entidad soportados. Al retirar el header `Accept` explícito, la misma consulta devolvió **HTTP 200** con Atom/XML. No se verificó por separado una petición con `Accept: application/atom+xml`.

### Aprendizajes

El Service Document y `$metadata` cumplen funciones diferentes. Los tipos, claves y relaciones se definen en el modelo, mientras que los recursos concretos se consultan mediante el EntitySet y su clave. El formato admitido para `$metadata` no garantiza que el mismo `Accept` sea válido para leer entidades.

## 20.2 - Consultas OData V2

**Estado:** ✅ Completado

### Objetivo y conceptos

Aplicar operadores estandarizados de consulta sobre `Customers` y navegar a `Orders`. `$filter` restringe registros; `$select` restringe propiedades; `$orderby` define el orden; `$top` limita la cantidad; `$expand` incorpora relaciones navegables. `$format=json` solicita JSON.

**Prerrequisitos:** laboratorio 20.1 completado, Postman y Northwind disponible.

### Procedimiento y evidencias

1. Crear `20.2 - GET Customers Filter` con `GET /Customers`, `No Auth`, sin `Accept` explícito y parámetros `$filter=Country eq 'Germany'`, `$format=json`. Se obtuvo **HTTP 200** y JSON con `d.results`; el primer cliente visible `ALFKI` tenía `Country=Germany`. La captura verifica ese registro, no la totalidad de la colección.

   **Evidencia 20-06 - Filtro por país.** Parámetros, HTTP 200 y cliente `ALFKI` con país Alemania.

   ![Evidencia 20-06 - Filtro por país](images/tema-20/20-06-postman-odata-filter-country-germany.png)

2. Crear `20.2 - GET Customers Select`: `GET /Customers` con `$select=CustomerID,CompanyName,Country`, `$top=3`, `$format=json`; retirar `$filter`. Se recibió **HTTP 200** y tres clientes (`ALFKI`, `ANATR`, `ANTON`) con las tres propiedades de negocio solicitadas y `__metadata` técnico. `$select` reduce el conjunto de propiedades transferidas.

   **Evidencia 20-07 - Proyección de propiedades.** Tres clientes y tres propiedades de negocio.

   ![Evidencia 20-07 - Proyección de propiedades](images/tema-20/20-07-postman-odata-select-properties.png)

3. Crear `20.2 - GET Customers OrderBy`: `GET /Customers` con `$select=CustomerID,CompanyName,Country`, `$orderby=CompanyName desc`, `$top=5`, `$format=json`. Se recibió **HTTP 200**. La captura permite comprobar el orden descendente de los primeros tres nombres visibles: `Wolski Zajazd`, `Wilman Kala` y `White Clover Markets`; el usuario confirmó cinco registros en la respuesta.

   **Evidencia 20-08 - Orden descendente y límite.** Parámetros y primeros resultados ordenados.

   ![Evidencia 20-08 - Orden descendente y límite](images/tema-20/20-08-postman-odata-orderby-desc-top5.png)

4. Crear `20.2 - GET Customer Expand Orders`: `GET /Customers('ALFKI')` con `$expand=Orders` y `$format=json`. Se obtuvo **HTTP 200**. `Orders.results` contenía pedidos, entre ellos `OrderID=10643`. Otras relaciones del pedido, como `Employee` y `Order_Details`, permanecieron como `__deferred`; `$expand=Orders` no implica expandir automáticamente todas las relaciones.

   **Evidencia 20-09 - Pedidos expandidos.** `Orders.results` y un pedido relacionado con `ALFKI`.

   ![Evidencia 20-09 - Pedidos expandidos](images/tema-20/20-09-postman-odata-expand-orders.png)

5. Crear `20.2 - GET Customers Combined Query`: `GET /Customers` con `$filter=Country eq 'Germany'`, `$select=CustomerID,CompanyName,Country`, `$orderby=CompanyName desc`, `$top=5`, `$format=json`. Se recibió **HTTP 200**; el usuario confirmó cinco registros de Alemania. Los tres visibles (`Toms Spezialitäten`, `QUICK-Stop`, `Ottilies Käseladen`) están en orden descendente. El sentido `desc` corresponde a la configuración realmente ejecutada.

   **Evidencia 20-10 - Consulta combinada.** Filtro, selección, orden y límite en una petición.

   ![Evidencia 20-10 - Consulta combinada](images/tema-20/20-10-postman-odata-combined-query.png)

### Resultado y validación

Se validaron respuestas HTTP 200 y datos visibles para los cinco escenarios. La combinación de operadores funcionó en Northwind; las capturas no prueban exhaustivamente cada elemento de las colecciones no visibles.

### Hallazgo de formato JSON

En pruebas controladas realizadas en Postman, el usuario observó `d.results` en consultas sin `$top` y un arreglo directamente bajo `d` al agregar `$top`. La respuesta con `$top` declaró `Content-Type: application/json;odata=verbose;charset=utf-8`. Esta diferencia se observó en el servicio de referencia, pero **no se ha determinado su causa técnica ni demostrado que sea una regla general de OData V2**. Los consumidores que procesen el JSON deben verificar la estructura real recibida antes de asumir una ruta fija.

### Aprendizajes

Los operadores permiten consultar de manera selectiva y recuperar relaciones sin construir endpoints particulares para cada filtro. Las referencias `__deferred` identifican relaciones no expandidas. Las diferencias de representación JSON deben contrastarse con el contenido y los headers reales.

## Arquitectura de los laboratorios 20.3 y 20.4

```mermaid
flowchart LR
  P["Postman"] -->|"GET HTTPS + OAuth"| S["HTTPS Sender"]
  S --> CM["Content Modifier - solo 20.4"]
  CM --> RR["Request Reply"]
  S -.->|"20.3: conexión directa"| RR
  RR -->|"OData V2 Receiver"| N["Northwind OData V2"]
  N --> RR
  RR --> P
```

En 20.3, HTTPS Sender se conecta directamente a Request Reply; en 20.4 se incorpora Content Modifier para preparar el recurso de consulta. El servicio externo responde sin autenticación saliente; el endpoint CPI se invoca con la autorización configurada en HTTPS Sender.

## 20.3 - Consulta de Customers mediante OData Receiver V2

**Estado:** ✅ Completado

### Objetivo y conceptos

Consumir una colección OData V2 desde Cloud Integration utilizando el adaptador nativo y una operación de lectura síncrona. `Query (GET)` obtiene una colección; `Request Reply` espera la respuesta del receptor y la devuelve al emisor HTTPS. El adaptador entrega en este escenario un XML de entidades, distinto de la representación Atom/XML observada al consultar directamente Northwind.

**Prerrequisitos:** 20.1 y 20.2, acceso al paquete `CPI Learning Lab`, Postman con OAuth 2.0 y acceso al monitor MPL.

### Procedimiento y evidencias

1. Crear el iFlow `LAB20_Consulta_OData_V2` en `CPI Learning Lab` y registrar una versión numerada inicial antes de configurar sus componentes. Incorporar `HTTPS Sender → Start → Request Reply → End` y un participante Receiver enlazado mediante **OData V2**. La separación entre emisor HTTPS y receptor OData permite exponer una URL propia de CPI sin acoplar al consumidor a la URL pública de Northwind.

2. Configurar **HTTPS Sender** con `Address: /lab20/odata-v2`, `Authorization: User Role`, `User Role: ESBMessaging.send` y CSRF desactivado. Se trata de un endpoint de lectura GET protegido por la autorización de CPI.

3. Configurar **OData Receiver V2 / Connection** con `Address: https://services.odata.org/V2/Northwind/Northwind.svc/`, `Proxy Type: Internet`, `Authentication: None`, `CSRF Protected: activado` y `Reuse Connection: activado`. El servicio público no requirió credenciales salientes durante la prueba.

4. En **Processing**, utilizar `Model Operation` para seleccionar `Query (GET)` sobre el EntitySet `Customers`; establecer `Resource Path: Customers` y `Query Options: $top=3`. Mantener `Enable Batch Processing: desactivado`, `Custom Query Options: vacío`, `Content Type: Atom`, `Attach Error Details on Failure: activado` y timeout de un minuto. `$top=3` limita la consulta para obtener una respuesta pequeña y verificable.

5. Guardar y desplegar el iFlow. El diseñador mostró `Deployed` y `Runtime Status: Started`.

   **Evidencia 20-11 - iFlow OData V2 desplegado.** Arquitectura HTTPS Sender, Request Reply y OData Receiver V2, con runtime iniciado.

   ![Evidencia 20-11 - iFlow OData V2 desplegado](images/tema-20/20-11-cpi-odata-v2-iflow-deployed.png)

6. Crear en Postman la petición independiente `20.3 - GET Customers OData V2`: `GET {{cpi_base_url}}/http/lab20/odata-v2`, OAuth 2.0, sin body. La ejecución devolvió **HTTP 200**, XML con raíz `<Customers>` y tres elementos `<Customer>` (cantidad confirmada en la validación). Entre los clientes identificados se encuentran `ALFKI` y `ANATR`.

   **Evidencia 20-12 - Consulta OData V2 desde Postman.** Endpoint CPI, HTTP 200 y respuesta XML con tres clientes.

   ![Evidencia 20-12 - Consulta OData V2 desde Postman](images/tema-20/20-12-postman-odata-v2-get-customers-xml.png)

7. Consultar el registro correspondiente en **Monitor Message Processing**. El MPL mostró `Completed`, `OverallStatus: COMPLETED`, `IntermediateError: false` y un tiempo de procesamiento de **198 ms**. Esta comprobación distingue la respuesta recibida por el consumidor del resultado del procesamiento en CPI.

   **Evidencia 20-13 - MPL de consulta OData V2.** Procesamiento completado del iFlow `LAB20_Consulta_OData_V2`.

   ![Evidencia 20-13 - MPL de consulta OData V2](images/tema-20/20-13-cpi-odata-v2-mpl-completed.png)

### Resultado esperado y validación

Se esperaba HTTP 200, tres clientes XML y un MPL Completed. Los tres resultados se comprobaron. El adaptador resolvió una consulta `Query (GET)` con `$top=3` sin requerir un cuerpo de entrada.

### Problemas encontrados y alcance

El diseñador mostró el warning `Http Sender Component may not pass XML or None message to OData Receiver. OData Receiver supports XML or None input only.`. No impidió el despliegue ni la ejecución satisfactoria de este GET sin body. Esto no demuestra compatibilidad universal para otras operaciones o formatos.

### Aprendizajes

El adaptador nativo permite consumir una colección OData sin construir manualmente la llamada HTTP al servicio. La validación requiere comprobar tanto el resultado de Postman como el MPL. La estructura XML observada debe tratarse como salida efectiva del adaptador, no como una copia literal de la respuesta Atom del backend.

## 20.4 - Selección dinámica de recurso OData V2 desde Postman

**Estado:** ✅ Completado, dentro del alcance de selección dinámica de EntitySet

### Objetivo y conceptos

Seleccionar el recurso OData en tiempo de ejecución mediante un header HTTP, un Content Modifier y una Exchange Property, sin modificar ni redesplegar el iFlow entre consultas. Se utilizan `Customers` y `Categories` como recursos del servicio Northwind.

**Prerrequisitos:** 20.3, HTTPS Sender, Content Modifier, headers, exchange properties y monitorización MPL.

### Procedimiento y evidencias

1. Crear un iFlow independiente `LAB20_Consulta_OData_Dinamica_V2` en el paquete `CPI Learning Lab`, con versión inicial `1.0.0`. La separación conserva intacto el iFlow de 20.3. Definir `HTTPS Sender` con `Address: /lab20/odata-dinamica-v2`, autorización `User Role / ESBMessaging.send` y CSRF desactivado. Construir `Start → Content Modifier → Request Reply → End` y conectar Request Reply al Receiver mediante **OData V2**.

2. Configurar el receptor con la misma conexión a Northwind del laboratorio 20.3. En Processing, seleccionar `Query (GET)` y `Resource Path: ${property.odataResourcePath}`, con `Query Options` vacío, Batch Processing desactivado y `Content Type: Atom`. La documentación SAP del adaptador admite expresiones de headers o properties en `Resource Path`; no se presupone soporte equivalente en `Query Options`.

3. En el Content Modifier, crear la Exchange Property `odataResourcePath`, inicialmente como constante `Customers` (`java.lang.String`). Desplegar y comprobar `Started`. Crear la petición Postman `20.4 - GET Customers Dynamic Resource Path`, método GET, OAuth 2.0, sin body y URL `{{cpi_base_url}}/http/lab20/odata-dinamica-v2`. Se recibió **HTTP 200** y XML `<Customers>` con clientes, lo que valida la resolución de la ruta desde una propiedad.

   **Evidencia 20-14 - Resource Path mediante Exchange Property.** Respuesta HTTP 200 y colección XML Customers con la propiedad inicialmente constante.

   ![Evidencia 20-14 - Resource Path mediante Exchange Property](images/tema-20/20-14-postman-odata-resource-path-dinamico.png)

4. Consultar el MPL de la ejecución satisfactoria. El registro seleccionado del iFlow `LAB20_Consulta_OData_Dinamica_V2` mostró **Completed** y **1 s 414 ms** de procesamiento (11/11/2026, 17:29:14). Este registro corresponde a la consulta de Customers con propiedad constante.

   **Evidencia 20-15 - MPL de Resource Path dinámico.** Registro seleccionado en estado Completed.

   ![Evidencia 20-15 - MPL de Resource Path dinámico](images/tema-20/20-15-cpi-odata-resource-path-dinamico-mpl-completed.png)

5. Registrar la versión `1.0.1` antes de incorporar la lectura del header externo. En el Content Modifier, cambiar la propiedad `odataResourcePath` a `Source Type: Header`, `Source Value: X-OData-Resource` y `Data Type: java.lang.String`. Mantener `Resource Path: ${property.odataResourcePath}` en el receptor. En **Runtime Configuration** del iFlow, agregar `X-OData-Resource` a **Allowed Header(s)**. Esta configuración permite que el header de entrada sea accesible para el procesamiento del mensaje. Guardar y desplegar el diseño modificado.

6. Crear la petición independiente `20.4 - GET Customers Header Dinamico`: `GET {{cpi_base_url}}/http/lab20/odata-dinamica-v2`, OAuth 2.0, sin body, header `X-OData-Resource: Customers`. Se obtuvo **HTTP 200** y XML `<Customers>`, con clientes como `ALFKI` y `ANATR`.

   **Evidencia 20-16 - Recurso Customers recibido por header.** Header personalizado, HTTP 200 y respuesta XML de clientes.

   ![Evidencia 20-16 - Recurso Customers recibido por header](images/tema-20/20-16-postman-odata-header-dinamico-customers.png)

7. En Postman, cambiar únicamente el valor del header a `X-OData-Resource: Categories`, sin modificar ni redesplegar el iFlow. Se obtuvo **HTTP 200** y XML `<Categories>` con categorías como `Beverages`, `Condiments`, `Confections` y `Dairy Products`. El contraste entre ambas ejecuciones demuestra la selección de un EntitySet diferente a partir del valor recibido.

   **Evidencia 20-17 - Recurso Categories recibido por header.** Mismo endpoint, header Categories y respuesta XML de categorías.

   ![Evidencia 20-17 - Recurso Categories recibido por header](images/tema-20/20-17-postman-odata-header-dinamico-categories.png)

8. Verificar la arquitectura desplegada y el estado `Started`. El flujo final contiene HTTPS Sender, Content Modifier, Request Reply y OData Receiver V2. Se mantiene visible el warning de compatibilidad del adaptador observado en 20.3, sin impedir estas consultas GET.

   **Evidencia 20-18 - Arquitectura final del iFlow dinámico.** Diagrama del iFlow y runtime Started.

   ![Evidencia 20-18 - Arquitectura final del iFlow dinámico](images/tema-20/20-18-cpi-odata-dinamico-iflow-final.png)

### Resultado esperado y validación

Se esperaba obtener dos colecciones distintas desde el mismo endpoint de CPI, cambiando solamente el header de Postman. Se validaron **HTTP 200 con Customers** y **HTTP 200 con Categories**, sin nuevo despliegue entre ambas solicitudes. La consulta inicial con Exchange Property constante también quedó validada con MPL Completed.

### Restricciones y seguridad

El laboratorio demuestra selección dinámica de recurso, **no** la parametrización de `$filter`, `$top` ni otras opciones de consulta. No se implementó una lista de recursos permitidos, por lo que la configuración debe considerarse **de laboratorio** y no una política segura de exposición de entidades para producción. Un diseño productivo requeriría validar explícitamente el recurso solicitado antes de invocar el receptor.

### Aprendizajes

`Allowed Header(s)` permite recibir el header personalizado en el procesamiento; Content Modifier lo transfiere a una Exchange Property; el OData Receiver resuelve esa propiedad como `Resource Path`. La operación permanece fija en `Query (GET)`; la selección de recurso no equivale a seleccionar dinámicamente el método HTTP.

## Estado de validación del capítulo

Los laboratorios **20.1, 20.2, 20.3 y 20.4** cuentan con resultados prácticos validados y están **✅ Completados** dentro de sus alcances. Los laboratorios **20.5 y 20.6** permanecen **⬜ Pendientes**; el capítulo continúa **🟨 En curso**. La parametrización de `$filter` y `$top` desde CPI y el control de recursos permitidos no forman parte de las capacidades validadas en 20.4.

## Referencias

- [OData V2 - URI Conventions](https://www.odata.org/documentation/odata-version-2-0/uri-conventions/)
- [OData V2 - JSON Format](https://www.odata.org/documentation/odata-version-2-0/json-format/)
- [OData.org - Reference Services](https://www.odata.org/odata-services/)
- [SAP Help - Configure the OData V2 Receiver Adapter](https://help.sap.com/docs/cloud-integration/sap-cloud-integration/configure-odata-v2-receiver-adapter)
- [SAP Help Portal - Cloud Integration](https://help.sap.com/docs/cloud-integration)
