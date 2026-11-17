
import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    def log = messageLogFactory.getMessageLog(message)

    if (log != null) {
        def excepcion = message.getProperty('CamelExceptionCaught')
        def detalle = excepcion != null
            ? excepcion.getMessage()
            : 'Excepcion sin detalle disponible'

        def nombreArchivo = message.getHeader(
            'CamelFileName', String
        ) ?: 'NO_DISPONIBLE'

        log.addCustomHeaderProperty(
            'LAB21_ArchivoSFTP', nombreArchivo
        )

        log.addCustomHeaderProperty(
            'LAB21_ErrorDetalle', detalle
        )
    }

    return message
}
