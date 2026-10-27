
import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {

    // 1. Leer el Body original
    String bodyOriginal = message.getBody(String)

    // 2. Leer un Header enviado desde Postman
    String origen = message.getHeader("X-Origen", String)

    // 3. Asignar un valor si no se recibió el Header
    if (!origen) {
        origen = "No informado"
    }

    // 4. Crear un nuevo Header
    message.setHeader("X-Procesado-Por", "Groovy")

    // 5. Crear Exchange Properties
    message.setProperty("OrigenPeticion", origen)
    message.setProperty("LongitudOriginal", bodyOriginal.length())

    // 6. Modificar el Body
    message.setBody(bodyOriginal.toUpperCase())

    return message
}
