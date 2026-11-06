
import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper

def Message processData(Message message) {

    // Leer el JSON recibido desde JMS
    def json = new JsonSlurper().parse(message.getBody(java.io.Reader))

    // Extraer el identificador de la orden
    def ordenId = json.ordenId

    if (ordenId == null || ordenId.toString().trim().isEmpty()) {
        throw new IllegalArgumentException(
            "El mensaje no contiene un ordenId valido"
        )
    }

    // Conservar el identificador como Exchange Property
    message.setProperty("OrdenId", ordenId.toString())

    // Registrar el identificador en el MPL
    def messageLog = messageLogFactory.getMessageLog(message)

    if (messageLog != null) {
        messageLog.addCustomHeaderProperty(
            "OrdenId",
            ordenId.toString()
        )
    }

    return message
}
