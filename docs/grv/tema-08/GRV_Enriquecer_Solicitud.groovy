import com.sap.it.script.v2.api.Message
import groovy.xml.MarkupBuilder
import groovy.xml.XmlSlurper

def Message processData(Message message) {

    String originalXml = message.getProperty("SolicitudOriginal") as String

    if (!originalXml?.trim()) {
        throw new IllegalStateException("No existe la propiedad SolicitudOriginal"
        )
    }

    def solicitud = new XmlSlurper().parseText(originalXml)
    def clientes = new XmlSlurper().parse(
        message.getBody(java.io.Reader)
    )

    String vSolicitudId = solicitud.solicitudId.text()
    String vClienteId = solicitud.clienteId.text()
    String vOrigen = solicitud.origen.text()

    String headerOrigen = message.getHeader("X-Lab-Origen", String)
    message.setProperty("HeaderOrigenDetectado", headerOrigen)

    if (!vClienteId) {
        throw new IllegalArgumentException("La solicitud no contiene clienteId"
        )
    }

    def coincidencias = clientes.Cliente.findAll {
        it.clienteId.text() == vClienteId
    }

    if (coincidencias.size() == 0) {
        message.setProperty("CodigoErrorFuncional", "CLIENTE_NO_ENCONTRADO")

        throw new IllegalStateException(
            "No se encontró el cliente solicitado"
        )
    }

    if (coincidencias.size() > 1) {
        throw new IllegalStateException(
            "Se encontraron múltiples clientes para el ID solicitado"
        )
    }

    def cliente = coincidencias[0]

    def writer = new StringWriter()
    def xml = new MarkupBuilder(writer)

    xml.SolicitudEnriquecida {
        solicitudId(vSolicitudId)
        clienteId(vClienteId)
        origen(vOrigen)
        Cliente {
            nombre(cliente.nombre.text())
            segmento(cliente.segmento.text())
        }
    }

    message.setBody(writer.toString())
    message.setHeader("Content-Type", "application/xml")

    return message
}
