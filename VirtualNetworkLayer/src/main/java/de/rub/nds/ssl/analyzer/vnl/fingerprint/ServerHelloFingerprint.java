package de.rub.nds.ssl.analyzer.vnl.fingerprint;

import de.rub.nds.ssl.stack.protocols.commons.ECipherSuite;
import de.rub.nds.ssl.stack.protocols.commons.EProtocolVersion;
import de.rub.nds.ssl.stack.protocols.handshake.ServerHello;
import de.rub.nds.ssl.stack.protocols.handshake.datatypes.Extensions;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ServerHelloFingerprint {
	
    private EProtocolVersion msgProtocolVersion;
    private ECipherSuite cipherSuite;
    private int sessionIDlen;
    private byte[] compressionMethod;
    private Extensions extensionList;
	
	public ServerHelloFingerprint(ServerHello sh) {
		this.msgProtocolVersion = sh.getMessageProtocolVersion();
		this.cipherSuite = sh.getCipherSuite();
		this.sessionIDlen = sh.getSessionID().getId().length;
		this.compressionMethod = sh.getCompressionMethod();
		this.extensionList = sh.getExtensions();
	}

    @Override
	public int hashCode() {
		// FIXME: This can be done better.
		return this.toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		// FIXME: This can be done better.
		return this.toString().equals(obj.toString());
	}


	public Map<String, Object> getAsMap() {
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("msgProtocolVersion", this.msgProtocolVersion);
		result.put("cipherSuite", this.cipherSuite);
		result.put("sessionIDlen", this.sessionIDlen);
		result.put("compressionMethod", Arrays.toString(this.compressionMethod));
		result.put("extensionList", this.extensionList);
		return result;
	}


	public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("Fingerprint for ServerHello: {");
    	sb.append("\n  ProtocolVersion = ").append(msgProtocolVersion.toString());
    	sb.append("\n  CipherSuite = ").append(cipherSuite);
    	sb.append("\n  CompressionMethod = ").append(Arrays.toString(compressionMethod));
    	sb.append("\n  Length of SessionID = ").append(sessionIDlen);
    	sb.append("\n  Extensions = ").append(extensionList);
        sb.append("\n}");
    	
    	return new String(sb);
    }

}