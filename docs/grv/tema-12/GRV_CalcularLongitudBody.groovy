import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {

    String body = message.getBody(String)
    int longitud = body.length()
    message.setProperty("LongitudBody", longitud)

    return message
}
