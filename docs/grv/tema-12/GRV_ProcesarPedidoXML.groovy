
import com.sap.gateway.ip.core.customdev.util.Message
import groovy.util.XmlSlurper

def Message processData(Message message) {

    // Leer el XML recibido
    String body = message.getBody(String)

    // Interpretar el documento XML
    def pedido = new XmlSlurper(false, false).parseText(body)

    // Extraer información
    String idPedido = pedido.Id.text()
    String cliente = pedido.Cliente.text()

    // Calcular el total de los productos
    BigDecimal total = 0

    pedido.Productos.Producto.each { producto ->
        BigDecimal precio = new BigDecimal(producto.Precio.text())
        int cantidad = producto.Cantidad.text().toInteger()

        total += precio * cantidad
    }

    // Almacenar resultados
    message.setProperty("IdPedido", idPedido)
    message.setProperty("Cliente", cliente)
    message.setProperty("TotalPedido", total.toString())

    return message
}
