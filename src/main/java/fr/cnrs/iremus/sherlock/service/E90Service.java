package fr.cnrs.iremus.sherlock.service;

import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.jena.rdf.model.Resource;

import java.util.List;

@Singleton public class E90Service {
    @io.micronaut.context.annotation.Property(name = "jena")
    protected String jena;

    @Inject
    Sherlock sherlock;

    @Inject
    ResourceService resourceService;

    private final List<Resource> E90TypesOrderedByAccuracyDesc = List.of(CIDOCCRM.E36_Visual_Item, CIDOCCRM.E34_Inscription, CIDOCCRM.E90_Symbolic_Object);
    public Resource getMostAccurateE90RDFType(Resource e90) throws Exception {
        List<Resource> resourceRDFTypes = resourceService.getResourceRDFTypes(e90);
        for (Resource type : E90TypesOrderedByAccuracyDesc) {
            if (resourceRDFTypes.contains(type)) return type;
        }
        throw new Exception();
    }
}
