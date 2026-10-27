# Metodología de laboratorios

**Proyecto:** SAP Integration Suite Learning Lab
**Aplicación:** capítulos y laboratorios del curso
**Documento complementario:** `INSTRUCCIONES.md`

## 1. Objetivo

Establecer un procedimiento reproducible para planificar, implementar, validar y documentar los laboratorios de SAP Integration Suite, con foco inicial en Cloud Integration (CPI). El avance se basa en resultados comprobables, no solo en explicaciones.

## 2. Preparación obligatoria de cada capítulo

Antes de proponer instrucciones prácticas:

1. Consultar `README.md` y verificar el estado real del curso.
2. Consultar `INSTRUCCIONES.md` y este documento.
3. Consultar el Markdown del capítulo anterior para conservar su estructura, nomenclatura y presentación de evidencias.
4. Revisar los laboratorios completados y evitar repeticiones innecesarias.
5. Acordar el alcance, los laboratorios, los prerrequisitos y las validaciones del nuevo capítulo.

Si algún archivo no está disponible, informarlo explícitamente. No asumir su contenido ni comenzar una implementación que dependa de él.

## 3. Secuencia de trabajo por laboratorio

1. Presentar el objetivo específico y explicar por qué se realizará el ejercicio.
2. Identificar el iFlow y los recursos que se utilizarán. Antes de modificar un iFlow existente o crear una nueva versión, avisar y esperar confirmación.
3. Indicar **un solo paso práctico a la vez**, con los valores exactos de configuración y su justificación.
4. Informar **antes de solicitar una captura** el nombre exacto del archivo de evidencia.
5. Esperar la captura o el resultado del usuario y verificar lo que efectivamente demuestra.
6. Registrar el resultado validado y avanzar al siguiente paso. Si hay un error, diagnosticarlo antes de continuar.

No afirmar que un despliegue, una prueba o una configuración funciona si todavía no existe evidencia suficiente.

## 4. Convenciones de evidencias

### 4.1. Nombres de archivos

Utilizar el formato:

`NN-XX-descripcion-breve.png`

Donde:

- `NN`: número de capítulo, con dos dígitos.
- `XX`: secuencia correlativa de evidencia dentro del capítulo, con dos dígitos.
- `descripcion-breve`: descripción específica, en minúsculas y separada por guiones.

Ejemplos del capítulo 12:

- `12-01-iflow-creado.png`
- `12-02-https-sender-configuracion.png`

La numeración continúa a lo largo de todo el capítulo y no se reinicia por laboratorio. Antes de asignar un número, revisar las evidencias ya registradas para evitar duplicaciones.

### 4.2. Captura y validación

- Solicitar capturas solo cuando aporten evidencia útil de configuración, ejecución o resultado.
- Verificar que el contenido de la captura respalde la afirmación documentada.
- Si una captura no demuestra el resultado esperado, solicitar la comprobación faltante sin marcar el paso como validado.
- No incluir capturas con contraseñas, tokens, client secrets, service keys o material sensible. Si se expone una credencial real, rotarla y excluir la captura.

### 4.3. Referencias en Markdown

Cada evidencia debe aparecer **en el paso al que corresponde**, no reunida sin contexto al final del laboratorio. Incluir título, descripción y referencia relativa a la imagen. La ruta concreta debe seguir la estructura real del repositorio, verificada antes de consolidar el capítulo.

Ejemplo de formato (la ruta debe adaptarse a la carpeta real de evidencias):

```markdown
**Evidencia 12-02 — Configuración de HTTPS Sender**

Se verifica el endpoint, la autorización y la configuración de CSRF del adaptador HTTPS.

![Evidencia 12-02 — Configuración de HTTPS Sender](RUTA-REAL/12-02-https-sender-configuracion.png)
```

No afirmar que la imagen está incorporada al repositorio hasta comprobar que el archivo existe en la ruta indicada.

## 5. Organización de Postman

- Mantener una colección general del curso, con **una carpeta por capítulo**.
- Crear **peticiones independientes por laboratorio**, identificadas por número y objetivo.
- Configurar método HTTP, URL, autenticación, headers, body y pruebas según cada caso.
- Utilizar variables de entorno para valores reutilizables y evitar secretos en archivos exportados.
- Cuando un laboratorio requiera casos positivos y negativos, crear peticiones diferenciadas si facilita su ejecución y validación.
- Validar cada petición individualmente antes de considerar pruebas agrupadas mediante Collection Runner.
- No confundir la independencia de peticiones Postman con la obligación de crear un iFlow diferente para cada una.

## 6. iFlows, cambios y versionado

- Antes de crear o reutilizar un iFlow, revisar los artefactos existentes.
- Informar previamente cualquier modificación de un iFlow existente, creación de versión o cambio que pueda afectar laboratorios anteriores.
- Explicar el motivo técnico de cada decisión de diseño y señalar las alternativas nativas cuando corresponda.
- Diferenciar claramente **Save**, **Deploy** y la versión de diseño/runtime cuando sean relevantes para la validación.
- No ejecutar cambios ni despliegues no acordados.

## 7. Errores y hallazgos

Los errores son parte del aprendizaje. Para cada error relevante:

1. Registrar el síntoma observable y el contexto.
2. Revisar la configuración, los mensajes y las trazas disponibles.
3. Diferenciar la causa comprobada de las hipótesis no verificadas.
4. Documentar la corrección y la prueba que demuestra su resultado.
5. Si el aprendizaje es reutilizable, incorporarlo a `docs/kba-y-hallazgos.md` con síntomas, diagnóstico, solución y alcance.

No atribuir causas a SAP, al runtime, a la región o a la cuenta Trial sin evidencia.

## 8. Estados y criterios de cierre

Estados permitidos:

| Estado | Criterio |
|---|---|
| ⬜ Pendiente | No iniciado |
| 🟨 En curso | Implementación o estudio en progreso |
| 🟦 Revisado | Conceptos explicados, sin validación práctica suficiente |
| ✅ Completado | Resultado práctico ejecutado y comprobado |
| 🔁 Reforzar | Existe una brecha que requiere práctica adicional |
| ⛔ Bloqueado | Una dependencia impide continuar |

Al cambiar el estado de un tema, actualizarlo tanto en `README.md` como en el Markdown del capítulo. No usar `✅ Completado` únicamente por haber explicado un concepto o preparado código sin probarlo.

## 9. Documentación final del capítulo

Generar un Markdown consolidado, limpio y apto para GitHub, que incluya:

- Estado del tema y objetivo.
- Conceptos y prerrequisitos.
- Laboratorios con pasos reproducibles y justificación de las configuraciones.
- Resultados esperados y resultados efectivamente validados.
- Evidencias tituladas y descritas junto al paso correspondiente.
- Problemas encontrados, soluciones verificadas y aprendizajes.
- Referencias técnicas pertinentes.

**No incluir** comentarios dirigidos al usuario, notas internas del proceso de redacción, iteraciones descartadas, pruebas fallidas ya corregidas sin valor pedagógico ni tareas que ya fueron resueltas.

Antes de dar por cerrado el capítulo:

1. Revisar la numeración y las rutas de todas las evidencias.
2. Comprobar que los resultados descritos coinciden con las pruebas realizadas.
3. Confirmar el estado final del tema.
4. Actualizar el `README.md` y el documento del capítulo.
5. Registrar los hallazgos reutilizables en el KBA.
6. Confirmar qué archivos se han incorporado realmente al repositorio y cuáles quedan por subir.

## 10. Seguridad

Nunca documentar ni publicar credenciales reales, tokens, contraseñas, claves privadas ni service keys completas. Usar placeholders y variables. Antes de subir capturas, scripts, colecciones Postman o documentación, revisar que no contengan secretos.

## 11. Regla de continuidad

Al iniciar una nueva conversación o capítulo, **releer las referencias del repositorio** en vez de depender del recuerdo de conversaciones anteriores. Si no se puede comprobar un detalle, indicarlo y solicitar la referencia necesaria. Mantener el avance paso a paso y no repetir tareas que ya estén verificadas.
