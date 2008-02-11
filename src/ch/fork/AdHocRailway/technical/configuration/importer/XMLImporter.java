package ch.fork.AdHocRailway.technical.configuration.importer;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface;
import ch.fork.AdHocRailway.domain.locomotives.MemoryLocomotivePersistence;
import ch.fork.AdHocRailway.domain.routes.MemoryRoutePersistence;
import ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface;
import ch.fork.AdHocRailway.domain.turnouts.MemoryTurnoutPersistence;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface;
import ch.fork.AdHocRailway.technical.configuration.ConfigurationException;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;

public class XMLImporter
		extends DefaultHandler implements ContentHandler {

	private static Logger				logger		=
															Logger
																	.getLogger(XMLImporter.class);
	private String						filename;
	private boolean						supported	= true;
	private TurnoutPersistenceIface		turnoutPersistence;
	private LocomotivePersistenceIface	locomotivePersistence;
	private RoutePersistenceIface		routePersistence;

	public XMLImporter(String filename) throws ConfigurationException {
		this(filename, MemoryTurnoutPersistence.getInstance(),
				MemoryLocomotivePersistence.getInstance(),
				MemoryRoutePersistence.getInstance());
	}

	public XMLImporter(String filename,
			TurnoutPersistenceIface turnoutPersistence,
			LocomotivePersistenceIface locomotivePersistence,
			RoutePersistenceIface routePersistence)
			throws ConfigurationException {
		this.filename = filename;
		this.turnoutPersistence = turnoutPersistence;
		this.locomotivePersistence = locomotivePersistence;
		this.routePersistence = routePersistence;
		parseDocument(filename);
		if (!supported) {
			throw new ConfigurationException(
					Constants.ERR_VERSION_NOT_SUPPORTED);
		}
	}

	private void parseDocument(String filename) {
		// get a factory
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			// get a new instance of parser
			SAXParser sp = spf.newSAXParser();
			// parse the file and also register this class for call backs
			sp.parse(filename, this);
		} catch (SAXException se) {
			ExceptionProcessor.getInstance().processException(
					"Error opening file", se);
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	// Event Handlers
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		qName = qName.toLowerCase();
		if (qName.equals("railcontrol")) {
			double version =
					Double.parseDouble(attributes.getValue("ExporterVersion"));
			if (version == 0.2) {
				XMLImporter_0_2 importer =
						new XMLImporter_0_2(filename, turnoutPersistence,
								locomotivePersistence, routePersistence);
				logger.info("AdHoc-Railway Config Version 0.2 loaded ("
						+ filename + ")");
				return;
			} else if (version == 0.3) {
				XMLImporter_0_3 importer =
						new XMLImporter_0_3(filename, turnoutPersistence,
								locomotivePersistence, routePersistence);
				logger.info("AdHoc-Railway Config Version 0.3 loaded ("
						+ filename + ")");
				return;
			}
			supported = false;
			return;
		}
	}
}
