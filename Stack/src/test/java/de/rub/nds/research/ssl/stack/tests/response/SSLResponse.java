package de.rub.nds.research.ssl.stack.tests.response;

import java.util.Observable;
import java.util.Observer;


import de.rub.nds.research.ssl.stack.protocols.ARecordFrame;
import de.rub.nds.research.ssl.stack.protocols.alert.Alert;
import de.rub.nds.research.ssl.stack.protocols.commons.EContentType;
import de.rub.nds.research.ssl.stack.protocols.commons.SecurityParameters;
import de.rub.nds.research.ssl.stack.protocols.handshake.AHandshakeRecord;
import de.rub.nds.research.ssl.stack.protocols.handshake.HandshakeEnumeration;
import de.rub.nds.research.ssl.stack.protocols.handshake.MessageObservable;
import de.rub.nds.research.ssl.stack.protocols.msgs.ChangeCipherSpec;
import de.rub.nds.research.ssl.stack.protocols.msgs.TLSCiphertext;
import de.rub.nds.research.ssl.stack.tests.common.SSLHandshakeWorkflow;
import de.rub.nds.research.ssl.stack.tests.common.SSLHandshakeWorkflow.EStates;
import de.rub.nds.research.ssl.stack.tests.trace.Trace;

/**
 * A response during the SSL protocol processing.
 *
 * @author Eugen Weiss - eugen.weiss@ruhr-uni-bochum.de
 * @version 0.1 Apr 15, 2012
 */
public class SSLResponse extends ARecordFrame implements Observer {

    /**
     * Current trace
     */
    private Trace trace;
    /**
     * Handshake workflow
     */
    private SSLHandshakeWorkflow workflow;

    /**
     * Initialize a SSL response.
     *
     * @param response Bytes of the received response
     */
    public SSLResponse(final byte[] response,
            SSLHandshakeWorkflow workflow) {
        super(response);
        this.workflow = workflow;
    }

    /**
     * Extracts the SSL record messages for the response bytes.
     *
     * @param trace Trace object to save the status
     * @param response Bytes of the received response
     * @param param Security parameters as defined in Chapter 6.1 of RFC 2246
     * @return ResponseHandler
     */
    public final void handleResponse(final Trace trace,
            final byte[] response) {
        SecurityParameters param = SecurityParameters.getInstance();
        MessageObservable msgObserve = MessageObservable.getInstance();
        EContentType contentType = getContentType();
        switch (contentType) {
            case CHANGE_CIPHER_SPEC:
                ChangeCipherSpec ccs = new ChangeCipherSpec(response, true);
                trace.setCurrentRecord(ccs);
                workflow.statusChanged(trace);
                trace.setState(EStates.getStateById(workflow.getCurrentState()));
                workflow.addToList(trace);
                break;
            case ALERT:
                Alert alert = new Alert(response, true);
                trace.setCurrentRecord(alert);
                workflow.statusChanged(trace);
                workflow.addToList(trace);
                break;
            case HANDSHAKE:
                if (workflow.isEncrypted()) {
                    TLSCiphertext ciphertext = new TLSCiphertext(response, true);
                    trace.setCurrentRecord(ciphertext);
                    workflow.statusChanged(trace);
                    trace.setState(EStates.getStateById(
                            workflow.getCurrentState()));
                    workflow.addToList(trace);
                } else {
                    setTrace(trace);
                    msgObserve.addObserver(this);
                    new HandshakeEnumeration(response, true);
                    msgObserve.deleteObservers();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Set the trace for the handshake message
     *
     * @param trace Trace
     */
    private void setTrace(Trace trace) {
        this.trace = trace;
    }

    @Override
    public void update(Observable o, Object arg) {
        Trace trace = new Trace();
        trace.setNanoTime(this.trace.getNanoTime());
        trace.setTimestamp(this.trace.getTimestamp());
        AHandshakeRecord handRecord;
        if (o instanceof MessageObservable) {
            handRecord = (AHandshakeRecord) arg;
            new HandshakeResponse(handRecord, trace, workflow);
            setTrace(trace);
        }
        trace.setState(EStates.getStateById(workflow.getCurrentState()));
        workflow.addToList(trace);
    }
}
