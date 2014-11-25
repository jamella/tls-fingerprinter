package de.rub.nds.ssl.analyzer.vnl;

import de.rub.nds.ssl.analyzer.vnl.fingerprint.ClientHelloFingerprint;
import de.rub.nds.ssl.analyzer.vnl.fingerprint.HandshakeFingerprint;
import de.rub.nds.ssl.analyzer.vnl.fingerprint.ServerHelloFingerprint;
import de.rub.nds.ssl.analyzer.vnl.fingerprint.TLSFingerprint;
import de.rub.nds.ssl.stack.protocols.commons.Id;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Listens for fingerprints of "normal" handshakes and guesses how the fingerprints of
 * the corresponding session resumption(s) may look like. Can inject the guess back to
 * the {@link FingerprintListener}, so they become part of the "already seen"
 * fingerprints.
 * @author jBiegert azrdev@qrdn.de
 */
public class ResumptionFingerprintGuesser implements FingerprintReporter {
    private static final Logger logger = Logger.getLogger(ResumptionFingerprintGuesser.class);

    private FingerprintListener listener;

    /**
     * @param listener Used to inject guessed fingerprints. Pass null to disable injection.
     */
    public ResumptionFingerprintGuesser(FingerprintListener listener) {
        this.listener = listener;
    }

    @Override
    public void reportChange(SessionIdentifier sessionIdentifier, TLSFingerprint fingerprint, List<TLSFingerprint> previousFingerprints) {
        //nothing
    }

    @Override
    public void reportUpdate(SessionIdentifier sessionIdentifier, TLSFingerprint fingerprint) {
        //nothing
        //TODO: check if fingerprint == not-guessed resumption, and remove guess ?
    }

    @Override
    public void reportArtificial(SessionIdentifier sessionIdentifier, TLSFingerprint fingerprint) {
        // nothing
    }

    @Override
    public void reportNew(SessionIdentifier sessionIdentifier, TLSFingerprint tlsFingerprint) {
        if(sessionIdentifier instanceof GuessedSessionId ||
                tlsFingerprint instanceof GuessedResumptionFingerprint)
            return;

        SessionIdentifier sessionIdGuess = new GuessedSessionId(sessionIdentifier);
        TLSFingerprint resumptionGuess = new GuessedResumptionFingerprint(tlsFingerprint);
        logger.debug("Guessed session id: " + sessionIdGuess);
        logger.debug("Guessed fingerprint: " + resumptionGuess);
        ;

        if(listener != null) {
            logger.debug("now reporting guessed fingerprint");
            listener.insertFingerprint(sessionIdGuess, resumptionGuess);
        }
    }

    public static class GuessedSessionId extends SessionIdentifier {
        public GuessedSessionId(SessionIdentifier original) {
            super(original.getServerHostName(),
                    new GuessedClientHelloFingerprint(original.getClientHelloSignature()));
        }
    }

    public static class GuessedClientHelloFingerprint extends ClientHelloFingerprint {
        public GuessedClientHelloFingerprint(ClientHelloFingerprint original) {
            super(original); // copy

            // overwrite signs
            signs.put("session-id-empty", false);

            //FIXME: ClientHello.extensionsLayout - multiple variants, dep. on original?
            Object sign = signs.get("extensions-layout");
            if(sign instanceof List) {
                List<Id> extensionsLayout = new ArrayList<>((List<Id>) sign);
                extensionsLayout.remove(new Id(new byte[]{0x00, (byte) 0x0d})); // signature_algorithms
                extensionsLayout.add(new Id(new byte[]{0x00, (byte) 0x15})); // padding
                signs.put("extensions-layout", extensionsLayout);
            } else if(sign != null) {
                logger.warn("ClientHello.extensions-layout not a list: " + sign);
            }
        }
    }

    public static class GuessedResumptionFingerprint extends TLSFingerprint {
        public GuessedResumptionFingerprint(TLSFingerprint original) {
            super(new GuessedHandshakeFingerprint(original.getHandshakeSignature()),
                  new GuessedServerHelloFingerprint(original.getServerHelloSignature()),
                    original.getServerTcpSignature(),
                    original.getServerMtuSignature());
        }
    }

    public static class GuessedHandshakeFingerprint extends HandshakeFingerprint {
        public static final List<MessageTypes> MESSAGE_TYPES =
                Arrays.asList(new MessageTypes[]{
                    new MessageTypeSubtype(new Id((byte) 0x16), new Id((byte) 0x01)),
                    new MessageTypeSubtype(new Id((byte) 0x16), new Id((byte) 0x02)),
                    new MessageType(new Id((byte) 0x14)),
                    new MessageType(new Id((byte) 0x14))
                });

        public GuessedHandshakeFingerprint(HandshakeFingerprint original) {
            super(original); // copy

            // overwrite signs
            signs.put("message-types", MESSAGE_TYPES);
            signs.put("session-ids-match", true);
        }
    }

    public static class GuessedServerHelloFingerprint extends ServerHelloFingerprint {
        public GuessedServerHelloFingerprint(ServerHelloFingerprint original) {
            super(original); // copy

            // overwrite signs
            signs.put("session-id-empty", false);

            //FIXME: ServerHello.extensionsLayout - multiple variants, dep. on original?
            Object sign = signs.get("extensions-layout");
            if(sign instanceof List) {
                final List<Id> extensionsLayout = new ArrayList<>((List<Id>) sign);
                List<Id> newExtensionsLayout = Arrays.asList(
                        new Id(new byte[]{(byte) 0xff, 0x01}));
                signs.put("extensions-layout", newExtensionsLayout);
            } else if(sign != null) {
                logger.warn("ServerHello.extensions-layout not a list: " + sign);
            }
        }
    }
}