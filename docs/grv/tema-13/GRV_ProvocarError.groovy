import com.sap.gateway.ip.core.customdev.util.Message

def Message processData(Message message) {

    message.setProperty("Laboratorio", "13.1")

    throw new IllegalStateException(
        "LAB13_01: Error provocado intencionalmente"
    )
}
