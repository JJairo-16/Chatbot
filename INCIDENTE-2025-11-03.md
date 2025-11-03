# Informe de incidente — Pérdida de `.git`

**Fecha:** 2025-11-03  
**Proyecto:** Chatbot - Jairo Linares

## Resumen

Se eliminó accidentalmente el directorio `.git` del repositorio, perdiendo todo el historial de commits. No había un remoto del que recuperar la historia.

## Impacto

- Sin historial previo de cambios.
- Se mantienen únicamente los archivos de código vigentes.

## Resolución

1. Re-inicialización del repositorio con `git init`.
2. Commit inicial con el estado actual del código.
3. Documento de transparencia (este archivo) y nota en el README.

## Medidas preventivas

- Configurar remoto y `git push` regular.
- Usar copias de seguridad/espelhos del repo.
- Revisar comandos antes de ejecutar operaciones destructivas.

## Estado

El proyecto funciona correctamente; solo se ha perdido la trazabilidad histórica.
