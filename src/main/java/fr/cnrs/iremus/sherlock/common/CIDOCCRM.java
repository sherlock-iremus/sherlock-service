package fr.cnrs.iremus.sherlock.common;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class CIDOCCRM {
    public static final String NS = "http://www.cidoc-crm.org/cidoc-crm/";
    private static final Model m_model = ModelFactory.createDefaultModel();

    public static final Resource E1_CRM_Entity = m_model.createResource(NS + "E1_CRM_Entity");
    public static final Resource E13_Attribute_Assignment = m_model.createResource(NS + "E13_Attribute_Assignment");
    public static final Resource E21_Person = m_model.createResource(NS + "E21_Person");
    public static final Resource E28_Conceptual_Object = m_model.createResource(NS + "E28_Conceptual_Object");
    public static final Resource E32_Authority_Document = m_model.createResource(NS + "E32_Authority_Document");
    public static final Resource E41_Appellation = m_model.createResource(NS + "E41_Appellation");
    public static final Resource E42_Identifier = m_model.createResource(NS + "E42_Identifier");
    public static final Property P1_is_identified_by = m_model.createProperty(NS + "P1_is_identified_by");
    public static final Property P2_has_type = m_model.createProperty(NS + "P2_has_type");
    public static final Property P14_carried_out_by = m_model.createProperty(NS + "P14_carried_out_by");
    public static final Property P71_lists = m_model.createProperty(NS + "P71_lists");
    public static final Property P106_is_composed_of = m_model.createProperty(NS + "P106_is_composed_of");
    public static final Property P127_has_broader_term = m_model.createProperty(NS + "P127_has_broader_term");
    public static final Property P140_assigned_attribute_to = m_model.createProperty(NS + "P140_assigned_attribute_to");
    public static final Property P141_assigned = m_model.createProperty(NS + "P141_assigned");
    public static final Property P150_defines_typical_parts_of = m_model.createProperty(NS + "P150_defines_typical_parts_of");
    public static final Property P177_assigned_property_of_type = m_model.createProperty(NS + "P177_assigned_property_of_type");
    public static final Property P190_has_symbolic_content = m_model.createProperty(NS + "P190_has_symbolic_content");

    public static String getURI() {
        return NS;
    }
}
