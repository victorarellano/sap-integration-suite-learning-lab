# 14 - Monitorización, MPL y trazabilidad

**Estado del tema:** 🟨 En curso
**Package:** `CPI Learning Lab`
**iFlow reutilizado:** `LAB13_01_Error_No_Controlado`
**Versión observada en Message Status Overview:** `1.0.6`
**Endpoint relativo:** `/http/lab13/error-no-controlado`

## Objetivo

Localizar ejecuciones de Cloud Integration, interpretar estados y registros de procesamiento (MPL), utilizar identificadores de búsqueda y analizar errores, actividades y tiempos a partir de evidencias reales. Los laboratorios posteriores ampliarán el estudio a Trace y trazabilidad detallada.

## Conceptos

- **Monitor Message Processing:** interfaz de consulta de ejecuciones y sus detalles.
- **Message Processing Log (MPL):** registro técnico de procesamiento de un mensaje. La interfaz presenta también información relacionada, como los detalles del artefacto.
- **Completed / Failed:** estados del procesamiento interno; no equivalen necesariamente al código de respuesta HTTP.
- **Message ID:** identifica un registro de procesamiento concreto.
- **Correlation ID:** permite buscar mensajes que comparten un contexto de correlación.
- **Segment / Activities:** información técnica registrada sobre etapas y operaciones de procesamiento.
- **Log Level Info:** permite observar estados, tiempos, identificadores, actividades y determinadas excepciones; en los registros analizados no se mostró el Body completo ni los valores de Exchange Properties.

## Prerrequisitos

- Acceso a SAP Integration Suite, Cloud Integration y Monitor Message Processing.
- Ejecuciones históricas del iFlow `LAB13_01_Error_No_Controlado` procedentes del capítulo 13.
- Conocimiento de Exception Subprocess, Message End, Error End y errores técnicos.
- No se requiere modificar, versionar ni desplegar el iFlow para los laboratorios 14.1 y 14.2.

## Estado de los microaprendizajes

| ID | Laboratorio | Estado | Validación |
|---|---|---|---|
| 14.1 | Monitor Message Processing y estados de ejecución | ✅ Completado | Overview, registros Completed y Failed, pestañas, logs y comparación MPL; evidencias 14-01 a 14-08 |
| 14.2 | MPL: identificadores, segmentos y tiempos | ✅ Completado | Búsqueda por Message ID y Correlation ID; segmentos, actividades, inspección de artefacto y cálculo de duración; evidencias 14-09 a 14-13 |
| 14.3 | Trace y seguimiento paso a paso | ⬜ Pendiente | — |
| 14.4 | Headers, Properties y Body durante la ejecución | ⬜ Pendiente | — |
| 14.5 | Diagnóstico de errores técnicos y funcionales | ⬜ Pendiente | — |
| 14.6 | Correlación y trazabilidad de mensajes | ⬜ Pendiente | — |

---

## 14.1 - Monitor Message Processing y estados de ejecución

**Estado:** ✅ Completado

### Objetivo

Acceder a las vistas de monitorización, encontrar ejecuciones del iFlow y distinguir los estados de procesamiento de la respuesta HTTP del consumidor.

### Laboratorio paso a paso

1. Abrir **Monitor → Integrations and APIs**, acceder a la vista **Message Status Overview** desde las tarjetas de monitorización y seleccionar el período **Past Week**.

   **Motivo:** identificar artefactos con ejecuciones recientes y sus estados agregados.

   **Resultado observado:** para `LAB13_01_Error_No_Controlado`, versión `1.0.6`, se mostraron **35 mensajes**, de los cuales **28 Completed** y **7 Failed**, dentro del período seleccionado.

   **Evidencia 14-01 — Vista inicial de monitorización.** Resumen de estados por artefacto y filtro temporal.

   ![Evidencia 14-01 — Vista inicial de monitorización](images/tema-14/14-01-monitor-message-processing-vista-inicial.png)

2. Seleccionar el contador **28** de la columna **Completed** del iFlow.

   **Motivo:** acceder al listado individual y verificar filtros, estado, fecha y duración.

   **Resultado observado:** lista de 28 ejecuciones Completed; una ejecución del 29 de octubre de 2026 a las 17:04:48 mostró duración de **13 ms** y nivel de log **Info**.

   **Evidencia 14-02 — Ejecuciones completadas.** Listado filtrado por artefacto y estado, con detalle de una ejecución.

   ![Evidencia 14-02 — Ejecuciones completadas](images/tema-14/14-02-monitor-iflow-ejecuciones-completed.png)

3. Abrir la pestaña **Properties** de esa ejecución.

   **Motivo:** distinguir metadatos de monitorización de las Exchange Properties utilizadas dentro del iFlow.

   **Resultado observado:** se visualizaron `Message ID`, `Correlation ID` y la sección `Retention Periods`. Estos campos de la interfaz no deben confundirse con la visualización automática de todas las Exchange Properties del mensaje.

   **Evidencia 14-03 — Propiedades del mensaje.** Identificadores y sección de retención de la ejecución seleccionada.

   ![Evidencia 14-03 — Propiedades del mensaje](images/tema-14/14-03-monitor-message-properties.png)

4. Abrir **Logs** y después **Open Text View**.

   **Motivo:** comprobar qué información diagnóstica está disponible en un MPL con nivel **Info**.

   **Resultado observado:** el panel indicó `Log Level = Info` e `Instance ID = 0`. La vista textual mostró `OverallStatus = COMPLETED`, `IntermediateError = true` y un `LastError` que menciona `IllegalStateException`, `GRV_ProvocarError_v2.groovy` y la línea 10. La ejecución finalizó Completed aunque el registro conservó información de una excepción intermedia.

   **Evidencia 14-04 — Logs en nivel Info.** Panel de logs de la ejecución completada.

   ![Evidencia 14-04 — Logs en nivel Info](images/tema-14/14-04-monitor-message-logs-info.png)

   **Evidencia 14-05 — MPL en vista textual.** Campos técnicos de la ejecución completada y detalle de excepción intermedia.

   ![Evidencia 14-05 — MPL en vista textual](images/tema-14/14-05-monitor-mpl-text-view.png)

5. Abrir **Artifact Details**.

   **Motivo:** relacionar el registro monitorizado con el artefacto y package que lo procesaron.

   **Resultado observado:** Name e ID `LAB13_01_Error_No_Controlado`, Type `Integration Flow`, Package `CPI Learning Lab`, con enlaces a la gestión, artefacto desplegado y editor.

   **Evidencia 14-06 — Detalles del artefacto.** Identificación del iFlow asociado al MPL.

   ![Evidencia 14-06 — Detalles del artefacto](images/tema-14/14-06-monitor-artifact-details.png)

6. Volver a **Message Status Overview** y seleccionar el contador **7 Failed** del mismo iFlow.

   **Motivo:** contrastar una ejecución fallida con otra completada.

   **Resultado observado:** siete ejecuciones Failed. La seleccionada, del 29 de octubre de 2026 a las 16:45:41, duró **12 ms**. La sección **Error Details** mostró `HttpResponseException`, con `IllegalStateException` y referencia al script `GRV_ProvocarError_v2.groovy`, línea 10.

   **Evidencia 14-07 — Ejecuciones fallidas.** Listado Failed y detalle técnico de una ejecución.

   ![Evidencia 14-07 — Ejecuciones fallidas](images/tema-14/14-07-monitor-iflow-ejecuciones-failed.png)

7. Abrir **Logs → Open Text View** de la ejecución fallida.

   **Motivo:** comparar directamente los campos del MPL con los de la ejecución completada.

   **Resultado observado:** `OverallStatus = FAILED`, `LogLevel = INFO`, `IntermediateError = true` y `LastError` con `HttpResponseException` que incluye la excepción original. `LastErrorModelStepId = CallActivity_5` también figuró en los registros examinados.

   **Evidencia 14-08 — MPL fallido en vista textual.** Estado final, excepción y metadatos de procesamiento.

   ![Evidencia 14-08 — MPL fallido en vista textual](images/tema-14/14-08-monitor-mpl-failed-text-view.png)

### Resultado esperado y validación

| Observación | Ejecución Completed | Ejecución Failed |
|---|---|---|
| Estado MPL | `COMPLETED` | `FAILED` |
| Log Level | `INFO` | `INFO` |
| IntermediateError | `true` | `true` |
| Error registrado | `IllegalStateException` | `HttpResponseException` con `IllegalStateException` |
| Duración observada | 13 ms | 12 ms |

**Aprendizaje:** un MPL `Completed` no implica ausencia de excepciones intermedias. La clasificación del procesamiento y el estado HTTP son dimensiones diferentes. El comportamiento observado es consistente con las pruebas de Exception Subprocess del capítulo 13; las capturas de monitorización por sí solas no prueban qué evento final específico ejecutó cada mensaje histórico.

---

## 14.2 - Message Processing Log: identificadores, segmentos y tiempos

**Estado:** ✅ Completado

### Objetivo

Localizar registros por identificadores, interpretar la información de segmentos y actividades, y comprender el alcance de los IDs técnicos y tiempos registrados.

### Laboratorio paso a paso

1. En **Monitor Message Processing**, buscar por el `Message ID` de la ejecución Failed del 29 de octubre de 2026 a las 16:45:41.

   **Motivo:** recuperar un registro concreto sin recorrer el historial manualmente.

   **Resultado observado:** el monitor indicó **Filtered by Message ID** y devolvió **1 mensaje**, correspondiente a `LAB13_01_Error_No_Controlado`, estado Failed, duración 12 ms.

   **Evidencia 14-09 — Búsqueda por Message ID.** Resultado individual y confirmación del criterio aplicado.

   ![Evidencia 14-09 — Búsqueda por Message ID](images/tema-14/14-09-monitor-busqueda-message-id.png)

2. Sustituir el criterio por el `Correlation ID` de la misma ejecución y repetir la búsqueda.

   **Motivo:** comprobar la búsqueda por correlación y distinguirla de la identificación individual del MPL.

   **Resultado observado:** **Filtered by Correlation ID** devolvió **1 mensaje**, el mismo registro Failed. El enlace del Correlation ID mostró la opción de buscar mensajes correlacionados. En este caso no se observaron otros mensajes con el mismo ID.

   **Evidencia 14-10 — Búsqueda por Correlation ID.** Resultado y enlace de búsqueda de mensajes relacionados.

   ![Evidencia 14-10 — Búsqueda por Correlation ID](images/tema-14/14-10-monitor-busqueda-correlation-id.png)

3. Abrir **Logs → Open Text View** y revisar la sección **Segment** y sus **Activities**.

   **Motivo:** comprender qué nivel de detalle técnico aporta un MPL con logging Info.

   **Resultado observado:** aparecen etapas de creación del Exchange, recepción del mensaje y procesamiento; entre las actividades se observan operaciones `setHeader`, `bean` y `direct`. En el procesamiento asociado a `CallActivity_5` se registra la excepción del script. Un segmento posterior muestra estado `FAILED`.

   **Evidencia 14-11 — Segmentos y actividades del MPL.** Tiempos, estados, ModelStepId, actividades y excepciones.

   ![Evidencia 14-11 — Segmentos y actividades del MPL](images/tema-14/14-11-monitor-mpl-segmentos-actividades.png)

4. Desde **Artifact Details**, seleccionar **View deployed Artifact**.

   **Motivo:** inspeccionar el diseño desplegado sin modificar el iFlow.

   **Resultado observado:** proceso principal `Start → Provocar Error → End` y Exception Subprocess `Error Start 1 → CM_Respuesta_Error → End 1`, con Sender HTTPS.

   **Evidencia 14-12 — Artefacto desplegado.** Estructura visible del iFlow asociado al mensaje.

   ![Evidencia 14-12 — Artefacto desplegado](images/tema-14/14-12-monitor-iflow-desplegado.png)

5. Seleccionar el componente **Provocar Error** y revisar su pestaña **General**.

   **Motivo:** investigar si `LastErrorModelStepId = CallActivity_5` se corresponde directamente con un identificador visible del componente.

   **Resultado observado:** la pestaña General mostró `Name = Provocar Error`, pero no presentó un campo ID que permitiera verificar la correspondencia con `CallActivity_5`. El MPL también menciona el recurso Groovy que falló, pero esa información no demuestra por sí sola la equivalencia exacta entre el ID técnico y el componente gráfico.

   **Evidencia 14-13 — Inspección del paso Groovy.** Nombre visible del componente y límite de identificación desde la interfaz utilizada.

   ![Evidencia 14-13 — Inspección del paso Groovy](images/tema-14/14-13-monitor-identificacion-callactivity.png)

6. Comparar los tiempos generales de la ejecución Failed en el MPL:

   - `StartTime`: 19:45:41.336 UTC.
   - `StopTime`: 19:45:41.348 UTC.
   - Diferencia: `348 − 336 = 12 ms`.
   - `Processing Time` mostrado: **12 ms**.

   **Motivo:** interpretar la duración registrada y evitar confundirla con el tiempo de respuesta extremo a extremo experimentado por Postman.

   **Validación:** la evidencia 14-08 ya contiene los valores necesarios; no se requiere una captura duplicada.

### Resultado esperado y validación

Se recuperó el mismo mensaje por dos identificadores distintos, se analizaron segmentos y actividades de su MPL, se inspeccionó el artefacto desplegado y se comprobó la duración de 12 ms a partir de los timestamps registrados.

### Limitación observada

Los identificadores técnicos `CallActivity_5` y `MessageFlow_4` aparecen en el MPL, pero **en la vista del artefacto desplegado utilizada no se pudo establecer una correspondencia directa y verificable con los componentes gráficos**. No se concluye que dicha correspondencia sea imposible mediante otras herramientas; queda fuera de lo comprobado en este laboratorio.

### Aprendizajes

- `Message ID` recuperó una ejecución concreta; `Correlation ID` permitió recuperar el mismo mensaje mediante un criterio de correlación.
- Los segmentos y actividades aportan información diagnóstica aun con `Log Level = Info`.
- Los identificadores internos del MPL no deben interpretarse automáticamente como nombres o IDs visibles en el editor.
- El tiempo del MPL no equivale necesariamente al tiempo total de ida y vuelta del cliente HTTP.

---

## Problemas encontrados y hallazgos

No se produjeron modificaciones de diseño ni errores de despliegue en estos dos laboratorios. Se identificó una **limitación de diagnóstico**: la vista inspeccionada del artefacto no permitió verificar el mapeo de `CallActivity_5` a un componente visual. Esta limitación no impide interpretar la excepción por su clase, script y línea reportados en el MPL.

## Continuidad del capítulo

Los laboratorios **14.3 a 14.6** siguen pendientes de implementación y evidencia. El capítulo se mantiene **🟨 En curso**. No se ha cambiado el nivel de logging ni modificado el iFlow en los laboratorios documentados.

## Referencias

- [Metodología de laboratorios](METODOLOGIA-LABORATORIOS.md)
- [Capítulo 13 — Manejo de errores y Exception Subprocess](13-manejo-errores-exception-subprocess.md)
- [SAP Help Portal — Cloud Integration](https://help.sap.com/docs/integration-suite/sap-integration-suite/cloud-integration)

> **Evidencias:** las referencias anteriores indican las rutas de destino previstas en `docs/images/tema-14/`. Las capturas deben incorporarse físicamente a esa carpeta y comprobarse antes de dar por verificada su disponibilidad en GitHub.
