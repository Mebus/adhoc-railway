package ch.fork.AdHocRailway.controllers.impl.brain;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.TooManyListenersException;

import org.apache.log4j.Logger;

public class BrainController {
	private static Logger LOGGER = Logger.getLogger(BrainController.class);
	private OutputStream out;
	private final List<BrainListener> listeners = new ArrayList<BrainListener>();
	private static final BrainController INSTANCE = new BrainController();
	private CommPort commPort;
	private InputStream in;

	private boolean connected = false;

	private BrainController() {
		super();
	}

	public static BrainController getInstance() {
		return INSTANCE;
	}

	public void connect(final String portName) {
		CommPortIdentifier portIdentifier;
		try {
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

			if (portIdentifier.isCurrentlyOwned()) {
				LOGGER.error("Port " + portName + " is currently in use");
			} else {
				commPort = portIdentifier.open(this.getClass().getName(), 2000);

				if (commPort instanceof SerialPort) {
					final SerialPort serialPort = (SerialPort) commPort;
					serialPort.setSerialPortParams(230400,
							SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);

					in = serialPort.getInputStream();
					out = serialPort.getOutputStream();

					serialPort.addEventListener(new SerialReader(in));
					serialPort.notifyOnDataAvailable(true);
					connected = true;
				} else {
					LOGGER.error("Only serial ports are allowed");
				}
			}
		} catch (final NoSuchPortException | UnsupportedCommOperationException
				| IOException | TooManyListenersException | PortInUseException e) {
			throw new BrainException("error connection to the brain on port "
					+ portName, e);
		}
	}

	public void disconnect() {

		if (!connected) {
			return;
		}
		try {
			in.close();
			out.close();
			commPort.close();
		} catch (final IOException e) {
			throw new BrainException(
					"error while closing the connection to the brain");
		} finally {
			connected = false;
		}
	}

	/**
	 * Handles the input coming from the serial port. A new line character is
	 * treated as the end of a block in this example.
	 */
	public class SerialReader implements SerialPortEventListener {
		private final InputStream in;
		private final byte[] buffer = new byte[1024];

		public SerialReader(final InputStream in) {
			this.in = in;
		}

		@Override
		public void serialEvent(final SerialPortEvent arg0) {
			int data;

			try {
				int len = 0;
				while ((data = in.read()) > -1) {
					if (data == 0x0d) {
						break;
					}
					buffer[len++] = (byte) data;
				}
				final String receivedString = new String(buffer, 0, len);
				for (final BrainListener listener : listeners) {
					listener.receivedMessage(receivedString);
				}
				LOGGER.debug(receivedString);
			} catch (final IOException e) {
				LOGGER.error("error receiving data from serialport", e);
			}
		}
	}

	public void write(final String str) throws IOException {
		final byte[] bytes = str.getBytes(Charset.forName("US-ASCII"));
		for (final byte b : bytes) {
			out.write(b);
		}
		this.out.write(0x0d);
	}

	/**
	 * @return A HashSet containing the CommPortIdentifier for all serial ports
	 *         that are not currently being used.
	 */
	@SuppressWarnings("rawtypes")
	public static HashSet<CommPortIdentifier> getAvailableSerialPorts() {
		final HashSet<CommPortIdentifier> h = new HashSet<CommPortIdentifier>();
		final Enumeration thePorts = CommPortIdentifier.getPortIdentifiers();
		while (thePorts.hasMoreElements()) {
			final CommPortIdentifier com = (CommPortIdentifier) thePorts
					.nextElement();
			switch (com.getPortType()) {
			case CommPortIdentifier.PORT_SERIAL:
				try {
					final CommPort thePort = com.open("CommUtil", 50);
					thePort.close();
					h.add(com);
				} catch (final PortInUseException e) {
					LOGGER.error("Port, " + com.getName() + ", is in use.");
				} catch (final Exception e) {
					LOGGER.error("Failed to open port " + com.getName());
					e.printStackTrace();
				}
			}
		}
		return h;
	}

	public static List<String> getAvailableSerialPortsAsString() {

		final List<String> ports = new ArrayList<String>();
		final HashSet<CommPortIdentifier> availableSerialPorts = getAvailableSerialPorts();
		for (final CommPortIdentifier i : availableSerialPorts) {
			ports.add(i.getName());
		}
		return ports;
	}

	public void addBrainListener(final BrainListener listener) {
		listeners.add(listener);
	}

	public void removeBrainListener(final BrainListener listener) {
		listeners.remove(listener);
	}

	public static void main(final String[] args) throws IOException {
		final BrainController instance2 = BrainController.getInstance();

		instance2.write("XGO");
	}

}