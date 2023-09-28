package fr.cnrs.iremus.sherlock.common;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class Types {
    private static final Model m_model = ModelFactory.createDefaultModel();
    public static final Resource E55_MEI_CONTENT = m_model.createResource(Sherlock.NS + "9cf40433-f154-46d1-97e0-542bec3abf09");
    public static final Resource E55_XML_MEI_FILE = m_model.createResource(Sherlock.NS + "bf9dce29-8123-4e8e-b24d-0c7f134bbc8e");
}
