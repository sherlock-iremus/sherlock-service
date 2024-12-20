package fr.cnrs.iremus.sherlock.common;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class LRMOO {
    public static final String NS = "http://iflastandards.info/ns/lrm/lrmoo/";
    private static final Model model = ModelFactory.createDefaultModel();

    public static final Resource F1_Work = model.createResource(NS + "F1_Work");
    public static final Resource F2_Expression = model.createResource(NS + "F2_Expression");
    public static final Resource F3_Manifestation = model.createResource(NS + "F3_Manifestation");
    public static final Resource F27_Work_Creation = model.createResource(NS + "F27_Work_Creation");
    public static final Resource F28_Expression_Creation = model.createResource(NS + "F28_Expression_Creation");
    public static final Property R3_is_realised_in = model.createProperty(NS + "R3_is_realised_in");
    public static final Property R4_embodies = model.createProperty(NS + "R4_embodies");
    public static final Property R16_created = model.createProperty(NS + "R16_created");
    public static final Property R17_created = model.createProperty(NS + "R17_created");
    public static final Property R19_created_a_realisation__of = model.createProperty(NS + "R19_created_a_realisation__of");

}
