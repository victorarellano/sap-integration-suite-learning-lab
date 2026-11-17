import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {
    String contenido = message.getBody(String)

    if (contenido.contains('ERROR_CONTROLADO')) {
        throw new IllegalStateException(
            'LAB21_06_ERROR_CONTROLADO: contenido de archivo rechazado'
        )
    }

    return message
}
