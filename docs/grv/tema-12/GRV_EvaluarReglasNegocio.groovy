
import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def Message processData(Message message) {

    def pedido = new JsonSlurper().parse(
        message.getBody(java.io.Reader)
    )

    def productos = pedido.productos

    if (!(productos instanceof List) || productos.isEmpty()) {
        def respuesta = [
            estado: "RECHAZADO",
            motivo: "El pedido debe contener productos"
        ]

        message.setBody(JsonOutput.toJson(respuesta))
        message.setHeader("Content-Type", "application/json")
        message.setHeader("CamelHttpResponseCode", 400)
        return message
    }

    BigDecimal total = BigDecimal.ZERO

    productos.each { producto ->
        BigDecimal precio = new BigDecimal(producto.precio.toString())
        BigDecimal cantidad = new BigDecimal(producto.cantidad.toString())
        total += precio * cantidad
    }

    BigDecimal porcentaje = BigDecimal.ZERO

    if (total >= 100000) {
        porcentaje = 0.10G
    } else if (total >= 50000) {
        porcentaje = 0.05G
    }

    BigDecimal descuento = total * porcentaje
    BigDecimal totalFinal = total - descuento

    def respuesta = [
        idPedido: pedido.id,
        cliente: pedido.cliente,
        estado: "APROBADO",
        totalOriginal: total,
        porcentajeDescuento: porcentaje * 100,
        descuento: descuento,
        totalFinal: totalFinal
    ]

    message.setBody(JsonOutput.toJson(respuesta))
    message.setHeader("Content-Type", "application/json")

    return message
}
