package DataRetrieving;

import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.rio.RDFFormat;

public class NtripleSPARQLRepository extends SPARQLRepository {
	public NtripleSPARQLRepository(String endpointUrl) {
		super(endpointUrl);
		this.getHTTPClient().setPreferredRDFFormat(RDFFormat.NTRIPLES);
	}
}