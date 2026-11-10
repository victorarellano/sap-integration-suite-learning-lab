
import com.sap.gateway.ip.core.customdev.util.Message
import java.util.concurrent.TimeoutException

def Message processData(Message message) {

    def exception = message.getProperty("CamelExceptionCaught")
    def current = exception

    int httpCode = 500
    String errorCode = "INTEGRATION_ERROR"
    String errorMessage = "No fue posible procesar la solicitud."

    // Buscar la causa original de la excepcion.
    int depth = 0

    while (current != null && depth < 10) {

        if (current instanceof IllegalArgumentException) {
            httpCode = 400
            errorCode = "INVALID_REQUEST"
            errorMessage = "El parametro id es obligatorio o invalido."
            break
        }

        // Timeout en la comunicacion con el servicio externo
        if (current instanceof TimeoutException) {
            httpCode = 504
            errorCode = "UPSTREAM_TIMEOUT"
            errorMessage = "El servicio externo excedio el tiempo de espera."
            break
        }

        if (current.getClass().getName() == "org.apache.camel.component.ahc.AhcOperationFailedException") {
            httpCode = 502
            errorCode = "UPSTREAM_ERROR"
            errorMessage = "El servicio externo no pudo completar la solicitud."
            break
        }



        current = current.getCause()
        depth++
    }

    message.setProperty("httpErrorCode", httpCode)
    message.setProperty("errorCode", errorCode)
    message.setProperty("errorMessage", errorMessage)

    return message
}
