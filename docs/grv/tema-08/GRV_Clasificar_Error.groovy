
import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonOutput

def Message processData(Message message) {

    // 1. Recuperar la excepcion capturada
    def exception = message.getProperty("CamelExceptionCaught")

    String tipoExcepcion = exception?.getClass()?.getName() ?: "SIN_EXCEPCION"
    String mensajeOriginal = exception?.getMessage() ?: "SIN_MENSAJE"

    // 2. Clasificar el error a partir de la excepcion
    int httpStatus = 500
    String codigo = "ERROR_INTERNO"
    String mensaje = "No fue posible procesar la solicitud."

    if (mensajeOriginal.contains("No se encontró el cliente solicitado")) {
        httpStatus = 404
        codigo = "CLIENTE_NO_ENCONTRADO"
        mensaje = "El cliente solicitado no existe."
    }

    // 3. Construir respuesta JSON
    def respuesta = [
        error: [
            codigo: codigo,
            mensaje: mensaje
        ]
    ]

    // 4. Configurar respuesta HTTP
    message.setHeader("CamelHttpResponseCode", httpStatus)
    message.setHeader("Content-Type", "application/json")
    message.setBody(JsonOutput.toJson(respuesta))

    // 5. Headers temporales para diagnostico
    message.setHeader("X-Lab-Tipo-Excepcion", tipoExcepcion)
    message.setHeader("X-Lab-Mensaje-Excepcion", mensajeOriginal)

    return message
}
