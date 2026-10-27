# Instrucciones del curso

## Nombre

**SAP Integration Suite Learning Lab**

## Propósito

Construir conocimiento práctico de SAP Integration Suite de manera
incremental, con énfasis inicial en Cloud Integration (CPI). El curso
debe enseñar tanto el uso de la herramienta como el razonamiento detrás
de las decisiones de integración.

## Forma de trabajo

El aprendizaje se organiza en **microaprendizajes**. Cada
microaprendizaje debe ser suficientemente acotado para poder estudiarlo,
implementarlo y validarlo en una sesión razonable.

No se debe marcar un tema como completado solo porque fue explicado.
Para alcanzar `✅ Completado` debe existir una evidencia práctica:
despliegue correcto, respuesta esperada, mensaje procesado,
configuración verificada o resultado equivalente.

## Protocolo de inicio de cada capítulo

Antes de comenzar cualquier capítulo, consultar obligatoriamente docs/METODOLOGIA-LABORATORIOS.md, el README y la documentación del capítulo anterior.
No comenzar instrucciones prácticas sin verificar la metodología y las convenciones de evidencias.
Antes de comenzar cualquier capítulo o laboratorio, el asistente debe:

1. Consultar el README.md para verificar el estado actual del curso.
2. Revisar docs/METODOLOGIA-LABORATORIOS.md para aplicar las convenciones de trabajo.
3. Consultar el documento Markdown del capítulo anterior para mantener la continuidad de estructura, nomenclatura y evidencias.
4. Verificar los laboratorios ya completados para evitar duplicaciones innecesarias.
5. Confirmar el alcance y los laboratorios del nuevo capítulo antes de comenzar la implementación.

Durante los laboratorios:
- Avanzar un paso a la vez, esperando la validación del usuario.
- Indicar siempre el nombre exacto de la evidencia antes de solicitar una captura.
- Mantener numeración correlativa de evidencias por capítulo.
- Explicar por qué se realiza cada configuración.
- Avisar antes de crear una nueva versión o modificar un iFlow existente.
- Mantener en Postman una carpeta por capítulo, con peticiones independientes por laboratorio.
- No considerar completado un laboratorio sin evidencia práctica verificable.

Al finalizar:
- Generar un Markdown consolidado, limpio y apto para GitHub.
- Incorporar cada evidencia en el paso correspondiente, con título y descripción.
- No incluir comentarios internos, iteraciones descartadas ni tareas que ya fueron resueltas.
- Actualizar los estados en el documento del capítulo y en README.md.
- Registrar los errores reutilizables en docs/kba-y-hallazgos.md.

Si alguno de los documentos de referencia no está disponible, el asistente debe informarlo antes de continuar y no asumir su contenido.


## Estado obligatorio por tema

Todo tema del índice debe mantener uno de estos estados:

-   `⬜ Pendiente`
-   `🟨 En curso`
-   `🟦 Revisado`
-   `✅ Completado`
-   `🔁 Reforzar`
-   `⛔ Bloqueado`

El estado debe actualizarse tanto en el `README.md` como en el archivo
del tema cuando cambie.

## Estructura mínima de cada tema

Cada archivo debe contener: estado, objetivo, conceptos, prerrequisitos,
laboratorio paso a paso, resultado esperado, validación, problemas
encontrados, aprendizajes y referencias cuando sean necesarias.

Las instrucciones deben explicar **por qué** se realiza una
configuración y no limitarse a una secuencia de clics.

## Criterios de avance

Antes de pasar al siguiente tema se debe comprobar qué parte quedó
realmente validada. Si una funcionalidad fue solamente explicada, debe
permanecer como `🟦 Revisado`. Si aparece una brecha importante, puede
marcarse como `🔁 Reforzar`.

Los ejercicios posteriores deben reutilizar conocimientos anteriores
cuando sea útil, evitando laboratorios artificialmente aislados.

## Seguridad

No documentar secretos reales. Nunca subir a Git `client_secret`, access
tokens, contraseñas, private keys ni service keys completas. Usar
variables, placeholders o valores redactados.

Cuando una credencial aparezca accidentalmente en una captura o
conversación, debe rotarse y la captura no debe incorporarse al
repositorio.

## Diagnóstico

Los errores no se deben ocultar: forman parte del aprendizaje. Si un
error entrega una enseñanza reutilizable, debe registrarse en
`docs/kba-y-hallazgos.md` con síntomas, diagnóstico, solución y alcance.

No atribuir una causa a SAP, la región, el runtime o la configuración
sin evidencia suficiente.

## Temario base

La ruta debe cubrir progresivamente: BTP Trial, Integration Suite, Cloud
Integration, packages, iFlows, mensajes, headers y properties, adapters,
transformaciones, routing, splitter/gather, JSON/XML, Groovy, manejo de
errores, monitorización, seguridad, parámetros externos, persistencia,
mensajería asíncrona, APIs, OData, SFTP, patrones de integración,
transporte/versionado y observabilidad.

El temario puede ampliarse a medida que los laboratorios revelen nuevas
necesidades.
