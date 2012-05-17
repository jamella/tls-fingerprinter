package de.rub.nds.research.ssl.stack.protocols.msgs.datatypes;

import java.security.PublicKey;

/**
 * Signature interface for signature computation.
 * @author Eugen Weiss - eugen.weiss@ruhr-uni-bochum.de
 * May 17, 2012
 */
public interface ISignature {

    /**
     * Verify signature.
     * @param signature Signature value
     * @param pk Public key
     * @return True if signature is valid
     */
     boolean checkSignature(byte [] signature, PublicKey pk);

}
