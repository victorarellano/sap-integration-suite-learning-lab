
import com.sap.gateway.ip.core.customdev.util.Message
import groovy.util.XmlSlurper

def Message processData(Message message) {

    String body = message.getBody(String)

    def xml = new XmlSlurper().parseText(body)
    String pedidoId = xml.Pedido.Id.text()

    if (pedidoId == '1002') {
        throw new IllegalStateException("LAB10_ERROR_CONTROLADO: Pedido 1002 rechazado intencionalmente")
    }

    return message
}
