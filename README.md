# Chatbot - Jairo Linares

---

## ¿Qué es?

Se trata de un programa de **chat por consola** que permite mantener una conversación con el modelo de lenguaje **ChatGPT** utilizando una clave de API propia.  
El usuario puede escribir mensajes libremente o utilizar **comandos** para configurar el comportamiento del chat y gestionar la sesión.

---

## ¿Cómo funciona?

Al ejecutar el programa, se muestra un prompt con el texto `user:`.

A partir de ese momento, el usuario puede:

* Escribir cualquier mensaje para conversar directamente con ChatGPT.  
* Escribir un **comando** (que comienza con `/`) para realizar una acción específica.  
* Escribir `/exit` para salir del programa.

El programa utiliza un archivo de configuración (`data/userConfig.json`) donde se guarda la **clave de API**, el **modelo**, la **temperatura** y el **rol** del chat.  
Estos valores se cargan automáticamente al iniciar el programa y se pueden modificar mediante los comandos correspondientes.

---

## Configuración inicial (API Key)

Antes de usar el chatbot por primera vez, debes **introducir tu clave de API de OpenAI** en el archivo `data/userConfig.json`.  
Si el archivo no existe, el programa lo generará automáticamente en la primera ejecución.

1. Abre el archivo `data/userConfig.json`.  
2. Localiza el campo `apiKey` e introduce tu clave entre comillas.  
3. Guarda los cambios.

Ejemplo:

```json
{
  "apiKey": "tu_clave_de_openai_aqui",
  "param": {
    "model": "gpt-3.5-turbo",
    "role": "assistant",
    "temperature": 0.7
  }
}
```

---

## Comandos disponibles

* **`/help`**  
  Muestra una lista con todos los comandos disponibles y una breve descripción de cada uno.

* **`/new`**  
  Reinicia la conversación con el modelo, borrando el historial del chat.
  Antes de hacerlo, el programa solicitará confirmación escribiendo `NEW`.

* **`/config <parámetro>`**  
  Permite modificar los parámetros principales del chat (modelo, rol o temperatura).  
  Se pueden introducir valores nuevos durante la ejecución.

* **`/clean`**  
  Limpia la consola para mantener la interfaz despejada.  
  Antes de hacerlo, el programa solicitará confirmación escribiendo `CLEAN`.

* **`/history`** *(alias: `his`)*  
  Guarda el historial del chat en un archivo.

* **`/exit`**  
  Cierra el programa y finaliza la sesión actual.

---

## Parámetros configurables

* **`model`**  
  Define el modelo de ChatGPT que se utilizará.  
  Por defecto, el programa emplea `gpt-3.5-turbo`.

* **`role`**  
  Permite establecer el papel o comportamiento del chat (por ejemplo: “profesor”, “asistente”, “traductor”...).

* **`temperature`** *(alias: `temp`)*  
  Controla el nivel de creatividad o aleatoriedad de las respuestas.  
  El valor debe estar entre **0.0** y **1.0**, donde valores bajos generan respuestas más precisas y valores altos, respuestas más creativas.

---

## Licencia

Este proyecto está bajo la licencia [MIT](LICENSE).

---

### Nota sobre el historial del repositorio

El 2025-11-03 se perdió accidentalmente la carpeta `.git`, lo que provocó la eliminación del historial de commits.  
Para más detalles, consulta [INCIDENTE-2025-11-03.md](INCIDENTE-2025-11-03.md).
