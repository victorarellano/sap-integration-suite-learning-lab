# SAP Integration Suite Learning Lab

Curso práctico y progresivo para aprender **SAP Integration Suite**, con
foco inicial en **Cloud Integration (CPI)**. La documentación está en
español y cada tema mantiene un estado explícito para saber qué fue
realmente estudiado, practicado y validado.

## Objetivo

Partir desde una cuenta SAP BTP Trial sin configurar y avanzar, mediante
microaprendizajes y laboratorios, hasta construir integraciones con
patrones y funcionalidades habituales de SAP Cloud Integration.

El criterio de avance no es solamente leer un tema: un tema se considera
completado cuando su ejercicio fue ejecutado y validado.

## Estados

| Estado | Significado |
|---|---|
| ⬜ `Pendiente` | Aún no iniciado |
| 🟨 `En curso` | Se está estudiando o implementando |
| 🟦 `Revisado` | Conceptos vistos, falta validación práctica |
| ✅ `Completado` | Conceptos revisados y ejercicio validado |
| 🔁 `Reforzar` | Visto anteriormente, pero requiere práctica adicional |
| ⛔ `Bloqueado` | Existe una dependencia o problema que impide continuar |

## Ruta del curso

| ID | Tema | Estado | Evidencia |
|---|---|---|---|
| 01 | [Habilitación de SAP BTP Trial](docs/01-habilitacion-sap-btp-trial.md) | ✅ `Completado` | Cuenta, subaccount, Cloud Foundry y space `dev` |
| 02 | [Habilitación de SAP Integration Suite y Cloud Integration](docs/02-habilitacion-integration-suite.md) | ✅ `Completado` | Suscripción, roles, capability y runtime |
| 03 | [Primer iFlow HTTP + OAuth Client Credentials](docs/03-primer-iflow-http-oauth.md) | ✅ `Completado` | Endpoint protegido probado desde Postman |
| 04 | Packages, artefactos y ciclo de vida de un iFlow | ⬜ `Pendiente` | — |
| 05 | Message, headers, properties y Content Modifier | ⬜ `Pendiente` | — |
| 06 | Sender y Receiver Adapters | ⬜ `Pendiente` | — |
| 07 | Transformaciones: Message Mapping | ⬜ `Pendiente` | — |
| 08 | Content Enricher y Request-Reply | ⬜ `Pendiente` | — |
| 09 | Routing: Router y condiciones | ⬜ `Pendiente` | — |
| 10 | Splitter, Gather y procesamiento de colecciones | ⬜ `Pendiente` | — |
| 11 | Formatos JSON, XML y conversores | ⬜ `Pendiente` | — |
| 12 | Groovy Script: cuándo usarlo y cuándo evitarlo | ⬜ `Pendiente` | — |
| 13 | Manejo de errores y Exception Subprocess | ⬜ `Pendiente` | — |
| 14 | Monitorización, MPL y trazabilidad | ⬜ `Pendiente` | — |
| 15 | Seguridad: OAuth, Basic, certificados y Security Material | ⬜ `Pendiente` | — |
| 16 | Externalized Parameters y configuración por ambiente | ⬜ `Pendiente` | — |
| 17 | Data Store y persistencia temporal | ⬜ `Pendiente` | — |
| 18 | Integraciones asíncronas y colas | ⬜ `Pendiente` | — |
| 19 | APIs y consumo de servicios externos | ⬜ `Pendiente` | — |
| 20 | OData en Integration Suite | ⬜ `Pendiente` | — |
| 21 | Integración SFTP y archivos | ⬜ `Pendiente` | — |
| 22 | Patrones de integración aplicados en CPI | ⬜ `Pendiente` | — |
| 23 | Transporte, versionado y despliegue entre ambientes | ⬜ `Pendiente` | — |
| 24 | Observabilidad y diagnóstico de integraciones | ⬜ `Pendiente` | — |
| 25 | Laboratorio integrador final | ⬜ `Pendiente` | — |

## Registro de conocimiento y problemas

Los comportamientos inesperados, incidentes de Trial, errores
reproducibles y referencias SAP se consolidan en [KBA y
hallazgos](docs/kba-y-hallazgos.md).

## Metodología

Cada tema se desarrolla como un microaprendizaje: objetivo breve,
conceptos mínimos, laboratorio guiado, validación observable y registro
del estado. Cuando corresponda, se documentan capturas reales del
laboratorio.

La guía que define cómo debe desarrollarse el curso está en
[INSTRUCCIONES.md](INSTRUCCIONES.md).

## Entorno inicial del laboratorio

El laboratorio se inició sobre SAP BTP Trial en AWS, región US East
(VA), con Cloud Foundry y un space `dev`. Se habilitó SAP Integration
Suite, Cloud Integration y Process Integration Runtime. El primer iFlow
se probó desde Postman mediante OAuth 2.0 Client Credentials.

> **Seguridad:** nunca almacenar `client_secret`, access tokens,
> contraseñas o material criptográfico real en Git. Las capturas
> incorporadas al repositorio fueron seleccionadas para evitar
> credenciales sensibles.
