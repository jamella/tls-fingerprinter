package de.rub.nds.ssl.analyzer.vnl;

import de.rub.nds.ssl.analyzer.vnl.fingerprint.ClientHelloFingerprint;
import de.rub.nds.ssl.analyzer.vnl.fingerprint.serialization.Serializer;
import org.apache.log4j.Logger;

/**
 * Set of attributes defining a unique connection to some server, e.g. corresponding to a
 * particular TLS session.
 *
 * @author jBiegert azrdev@qrdn.de
 */
public class SessionIdentifier {
    private static final Logger logger = Logger.getLogger(SessionIdentifier.class);

    private String serverHostName;
    private ClientHelloFingerprint clientHelloSignature;

    /**
     * Initializes all attributes with null => isValid() == false
     */
    public SessionIdentifier() {}

    /**
     * deserialize: initializes clientHelloSignature with null
     */
    public SessionIdentifier(String serialized) {
        if(NO_HOSTNAME.equals(serialized.trim()))
            serverHostName = null;
        else
            serverHostName = serialized.trim();
    }

    /**
     * Initializes all attributes with the given values
     */
    public SessionIdentifier(String serverHostName,
                             ClientHelloFingerprint clientHelloSignature) {
        this.serverHostName = serverHostName;
        this.clientHelloSignature = clientHelloSignature;
    }

    /**
     * @return True iff at least one component of the id is not uninitialized / null
     */
    public boolean isValid() {
        return (serverHostName != null && ! serverHostName.isEmpty()) ||
                clientHelloSignature != null;
    }

    public static final String NO_HOSTNAME = "*";

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof SessionIdentifier))
            return false;

        SessionIdentifier that = (SessionIdentifier) o;

        if (serverHostName != null ?
                !serverHostName.equals(that.serverHostName) :
                that.serverHostName != null)
            return false;
        if (clientHelloSignature != null ?
                !clientHelloSignature.equals(that.clientHelloSignature) :
                that.clientHelloSignature != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = (serverHostName != null ? serverHostName.hashCode() : 0);
        result = prime * result +
                (clientHelloSignature != null ? clientHelloSignature.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("Connection to %s, ClientHello:\n%s",
                serverHostName,
                clientHelloSignature);
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        if(serverHostName != null) {
            sb.append(serverHostName);
        } else {
            sb.append(NO_HOSTNAME);
        }
        sb.append("\n");
        sb.append(Serializer.serializeClientHello(clientHelloSignature));

        return sb.toString();
    }

    public void setServerHostName(String serverHostName) {
        this.serverHostName = serverHostName;
    }

    public void setClientHelloSignature(ClientHelloFingerprint clientHelloSignature) {
        this.clientHelloSignature = clientHelloSignature;
    }

    public String getServerHostName() {
        return serverHostName;
    }

    public ClientHelloFingerprint getClientHelloSignature() {
        return clientHelloSignature;
    }
}
