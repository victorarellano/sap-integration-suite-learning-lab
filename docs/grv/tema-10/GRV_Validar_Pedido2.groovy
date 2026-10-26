
import com.sap.gateway.ip.core.customdev.util.Message
import groovy.util.XmlSlurper

def Message processData(Message message) {

    def body = message.getBody(java.io.Reader)
    def xml = new XmlSlurper().parse(body)

    String pedidoId = xml.Pedido.Id.text()
    String montoTexto = xml.Pedido.Monto.text()

    // Validacion existente: rechazo intencional del pedido 1002
    if (pedidoId == '1002') {
        throw new IllegalStateException(
            "LAB10_ERROR_CONTROLADO: Pedido 1002 rechazado intencionalmente"
        )
    }

    // Nueva validacion: el monto no puede ser negativo
    if (montoTexto?.trim()) {
        BigDecimal monto = new BigDecimal(montoTexto.trim())

        if (monto < 0) {
            throw new IllegalStateException(
                "LAB10_MONTO_NEGATIVO: Pedido ${pedidoId} tiene monto ${monto}"
            )
        }
    }

    return message
}
