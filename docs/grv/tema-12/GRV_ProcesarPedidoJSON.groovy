
import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def Message processData(Message message) {

    def pedido = new JsonSlurper().parse(
        message.getBody(java.io.Reader)
    )

    BigDecimal total = 0

    pedido.productos.each { producto ->
        BigDecimal precio = new BigDecimal(producto.precio.toString())
        BigDecimal cantidad = new BigDecimal(producto.cantidad.toString())
        total += precio * cantidad
    }

    def respuesta = [
        idPedido: pedido.id.toString(),
        cliente: pedido.cliente,
        totalPedido: total
    ]

    message.setBody(JsonOutput.toJson(respuesta))
    message.setHeader("Content-Type", "application/json")

    return message
}
