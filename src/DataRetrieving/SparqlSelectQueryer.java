package DataRetrieving;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResult;
import org.openrdf.query.TupleQuery;

public class SparqlSelectQueryer extends SparqlQueryer<BindingSet> {
	private static final Logger logger = LogManager.getLogger(SparqlSelectQueryer.class);

	public SparqlSelectQueryer(String service) {
		super(service);
	}

	@Override
	public QueryResult<BindingSet> queryWithLimit(String query, int limit, int offset){
		this.prepareQuery(query, limit, offset);
		QueryResult<BindingSet> res = null;
		if (this.query instanceof TupleQuery){
			try {
				res=  ((TupleQuery) this.query).evaluate();
			} catch (QueryEvaluationException e) {
				logger.error("The SELECT query could not be evaluated:\n"+query+"\n", e);
			}
		}
		return res;
	}

}
