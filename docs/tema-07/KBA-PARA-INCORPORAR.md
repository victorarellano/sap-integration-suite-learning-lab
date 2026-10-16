## KBA — `Premature end of file` al ejecutar Message Mapping por HTTPS

**Contexto:** Laboratorio 07.2, iFlow `Empleado Mapping HTTP`, mapping `MM_Empleado_Trabajador`.

**Síntoma:** POST desde Postman con `500 Internal Server Error`. En Monitor Message Processing, fallo en `Map Empleado to Trabajador` y mensaje `Premature end of file`.

**Diagnóstico:** se revisó el contenido de la solicitud y se detectó que no se había incluido el Body XML requerido. El error es compatible con entrada vacía o incompleta; no demuestra por sí solo un problema del XSD.

**Solución verificada:** configurar Postman con Body → raw → XML, enviar un documento `Empleado` válido y utilizar `Content-Type: application/xml`. La nueva respuesta contenía `Trabajador/Codigo=1001` y `Trabajador/NombreCompleto=Victor Arellano`.

**Alcance:** confirmado para esta ejecución del laboratorio. No generalizar como causa única de cualquier error `Premature end of file`.

**Evidencias:** `docs/images/tema-07/07-2-postman-error-http-500.png` y `docs/images/tema-07/07-2-postman-transformacion-correcta.png`.
