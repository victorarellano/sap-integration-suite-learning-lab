
import com.sap.gateway.ip.core.customdev.util.Message
import java.net.URLDecoder

def Message processData(Message message) {

    String query = message.getHeader('CamelHttpQuery', String)

    if (!query) {
        throw new IllegalArgumentException("El parametro id es obligatorio")
    }

    List<String> ids = []

    query.split('&', -1).each { parametro ->

        String[] partes = parametro.split('=', 2)
        String nombre = URLDecoder.decode(partes[0], 'UTF-8')

        if (nombre == 'id') {
            String valor = partes.length == 2 ? URLDecoder.decode(partes[1], 'UTF-8') : ''
            ids.add(valor)
        }
    }

    if (ids.size() != 1) {
        throw new IllegalArgumentException("Debe existir exactamente un parametro id"
        )
    }

    String id = ids[0]

    if (!(id ==~ /[0-9]+/) || !(id ==~ /.*[1-9].*/)) {
        throw new IllegalArgumentException("El parametro id debe ser un entero positivo"
        )
    }

    message.setProperty('P_ID_RECURSO', id)

    return message
}
