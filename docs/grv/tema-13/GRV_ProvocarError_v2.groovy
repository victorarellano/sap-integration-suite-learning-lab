import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def Message processData(Message message) {

    def body = new JsonSlurper().parse(message.getBody(java.io.Reader))

    if (body.escenario == "tecnico") {
        throw new IllegalStateException(
            "LAB13_04: Fallo tecnico provocado intencionalmente"
        )
    }

    if (body.monto == null || !(body.monto instanceof Number) ||
        body.monto <= 0) {

        message.setHeader("CamelHttpResponseCode", 400)
        message.setHeader("Content-Type", "application/json")

        message.setBody(JsonOutput.toJson([
            laboratorio: "13.4",
            tipoError: "FUNCIONAL",
            codigo: "MONTO_INVALIDO",
            mensaje: "El monto debe ser mayor que cero"
        ]))

        return message
    }

    message.setHeader("CamelHttpResponseCode", 200)
    message.setHeader("Content-Type", "application/json")

    message.setBody(JsonOutput.toJson([
        laboratorio: "13.4",
        estado: "OK",
        mensaje: "Solicitud procesada correctamente",
        monto: body.monto
    ]))

    return message
}
