package de.rub.nds.ssl.stack.tests.fingerprint;

import de.rub.nds.ssl.stack.protocols.commons.ECipherSuite;
import de.rub.nds.ssl.stack.protocols.commons.EProtocolVersion;
import de.rub.nds.ssl.stack.protocols.handshake.ClientHello;
import de.rub.nds.ssl.stack.protocols.handshake.datatypes.CipherSuites;
import de.rub.nds.ssl.stack.protocols.handshake.datatypes.RandomValue;
import de.rub.nds.ssl.stack.tests.analyzer.AFingerprintAnalyzer;
import de.rub.nds.ssl.stack.tests.analyzer.TestHashAnalyzer;
import de.rub.nds.ssl.stack.tests.analyzer.parameters.ClientHelloParameters;
import de.rub.nds.ssl.stack.tests.common.MessageBuilder;
import de.rub.nds.ssl.stack.tests.common.TestConfiguration;
import de.rub.nds.ssl.stack.tests.trace.Trace;
import de.rub.nds.ssl.stack.tests.workflows.ObservableBridge;
import de.rub.nds.ssl.stack.tests.workflows.SSLHandshakeWorkflow;
import de.rub.nds.ssl.stack.tests.workflows.SSLHandshakeWorkflow.EStates;
import java.net.SocketException;
import java.util.Observable;
import java.util.Observer;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Fingerprint the ClientHello SSL message.
 *
 * @author Eugen Weiss -eugen.weiss@ruhr-uni-bochum.de
 * @version 0.1 Apr 18, 2012
 */
public class FingerprintClientHello implements Observer {

    /**
     * Handshake workflow to observe.
     */
    private SSLHandshakeWorkflow workflow;
    /**
     * Test host.
     */
    private static final String HOST = "localhost";
    /**
     * Test port.
     */
    private static final int PORT = 9443;
    /**
     * Test counter.
     */
    private int counter = 1;
    /**
     * Defualt protocol version.
     */
    private EProtocolVersion protocolVersion = EProtocolVersion.TLS_1_0;
    /**
     * Test parameters.
     */
    private ClientHelloParameters parameters = new ClientHelloParameters();
    static Logger logger = Logger.getRootLogger();
    byte[] sessionID = new byte[]{(byte) 0xff, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f, (byte) 0x0f,
        (byte) 0x0f
    };

    @BeforeClass
    public void setUp() {
        PropertyConfigurator.configure("logging.properties");
    }

    /**
     * Test parameters for ClientHello fingerprinting.
     *
     * @return List of parameters
     */
    @DataProvider(name = "clientHello")
    public Object[][] createData1() {
        return new Object[][]{
                    {"Invalid protocol version 0xff,0xff",
                        new byte[]{(byte) 0xff, (byte) 0xff}, null, null, null,
                        null, null},
                    {"Invalid protocol version 0x00,0x00",
                        new byte[]{(byte) 0x00, (byte) 0x00}, null, null, null,
                        null, null},
                    {"Invalid protocol version SSLv3",
                        new byte[]{(byte) 0x03, (byte) 0x00}, null, null, null,
                        null, null},
                    {"Invalid protocol version TLSv1.2",
                        new byte[]{(byte) 0x03, (byte) 0x03}, null, null, null,
                        null, null},
                    {"No session ID defined but value is set to 0xff",
                        null, new byte[]{(byte) 0xff},
                        null, null, null, null},
                    {"256 Byte sessionID", null,
                        null, sessionID, null, null, null},
                    {"256 Byte sessionID and sessionID length 0x00", null,
                        null, sessionID, new byte[]{(byte) 0x00}, null, null},
                    {"Compression method 0xa1", null, null, null,
                        null, null, new byte[]{(byte) 0xa1}},
                    {"Wrong value for cipher suite length 0x01", null, null,
                        null, null,
                        new byte[]{(byte) 0x01}, null},
                    {"Wrong value for cipher suite length 0x00", null, null,
                        null, null,
                        new byte[]{(byte) 0x00}, null},};
    }

    /**
     * Manipulate Client Hello message to perform fingerprinting tests
     *
     * @param desc Test description
     * @param protocolVersion TLS protocol version
     * @param random Random value
     * @param suites Cipher suites
     * @param compMethod Compression method
     */
    @Test(enabled = true, dataProvider = "clientHello", invocationCount = 1)
    public void fingerprintClientHello(String desc,
            byte[] protVersion, byte[] noSessionValue, byte[] session,
            byte[] sessionIdLength,
            byte[] cipherLength, byte[] compMethod) throws SocketException {
        logger.info("++++Start Test No." + counter + "(" + desc + ")++++");
        workflow = new SSLHandshakeWorkflow();
        if (TestConfiguration.HOST.isEmpty() || TestConfiguration.PORT == 0) {
            workflow.connectToTestServer(HOST, PORT);
            logger.info("Test Server: " + HOST + ":" + PORT);
        } else {
            workflow.connectToTestServer(TestConfiguration.HOST,
                    TestConfiguration.PORT);
            logger.info(
                    "Test Server: " + TestConfiguration.HOST + ":" + TestConfiguration.PORT);
        }
        workflow.addObserver(this, EStates.CLIENT_HELLO);
        logger.info(EStates.CLIENT_HELLO.name() + " state is observed");
        parameters.setProtocolVersion(protVersion);
        parameters.setNoSessionIdValue(noSessionValue);
        parameters.setSessionId(session);
        parameters.setSessionIdLen(sessionIdLength);
        parameters.setCipherLen(cipherLength);
        parameters.setCompMethod(compMethod);
        parameters.setTestClassName(this.getClass().getName());
        parameters.setDescription(desc);
        workflow.start();

        AFingerprintAnalyzer analyzer = new TestHashAnalyzer(parameters);
        analyzer.analyze(workflow.getTraceList());

        this.counter++;
        logger.info("++++Test finished.++++");
    }

    /**
     * Update observed object.
     *
     * @param o Observed object
     * @param arg Arguments
     */
    @Override
    public void update(Observable o, Object arg) {
        MessageBuilder msgBuilder = new MessageBuilder();
        Trace trace = null;
        EStates states = null;
        ObservableBridge obs;
        if (o instanceof ObservableBridge) {
            obs = (ObservableBridge) o;
            states = (EStates) obs.getState();
            trace = (Trace) arg;
        }
        if (states == EStates.CLIENT_HELLO) {
            ECipherSuite[] suites = new ECipherSuite[]{
                ECipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA};
            CipherSuites cipherSuites = new CipherSuites();
            cipherSuites.setSuites(suites);
            RandomValue random = new RandomValue();
            byte[] compMethod = new byte[]{0x00};
            ClientHello clientHello = msgBuilder.createClientHello(this.protocolVersion.
                    getId(),
                    random.encode(false), cipherSuites.encode(false), compMethod);
            byte[] payload;
            if (parameters.getSessionId() != null) {
                byte[] session = parameters.getSessionId();
                clientHello.setSessionID(session);
            }
            if (parameters.getCompMethod() != null) {
                clientHello.setCompressionMethod(parameters.getCompMethod());
            }
            payload = clientHello.encode(true);
            if (parameters.getProtocolVersion() != null) {
                byte[] protVersion = parameters.getProtocolVersion();
                System.arraycopy(protVersion, 0, payload, 9, protVersion.length);
            }
            if (parameters.getNoSessionIdValue() != null) {
                byte[] value = parameters.getNoSessionIdValue();
                System.arraycopy(value, 0, payload, payload.length - 7,
                        value.length);
            }
            if (parameters.getSessionIdLen() != null) {
                byte[] sLen = parameters.getSessionIdLen();
                System.arraycopy(sLen, 0, payload, 43, sLen.length);
            }
            if (parameters.getCipherLen() != null) {
                byte[] cLen = parameters.getCipherLen();
                System.arraycopy(cLen, 0, payload, payload.length - 5,
                        cLen.length);
            }
            trace.setCurrentRecordBytes(payload);
            trace.setCurrentRecord(clientHello);
        }
    }

    /**
     * Close the Socket after the test run.
     */
    @AfterMethod
    public void tearDown() {
        workflow.closeSocket();
    }
}