package ClientRequest;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openrdf.model.Statement;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

import DataRetrieving.Prefixer;

/**
 * Servlet implementation class Battle
 */
@WebServlet("/Battle")
public class Battle extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String LOCAL_DB_SERVER = "http://localhost:8181/openrdf-sesame";
	private static final String DB_NAME = "Battles";
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Battle() {
        super();
        // TODO Auto-generated constructor stub
        Prefixer.INSTANCE.addPrefix("dbpedia", "http://dbpedia.org/resource/");
		Prefixer.INSTANCE.addPrefix("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
		Prefixer.INSTANCE.addPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		Prefixer.INSTANCE.addPrefix("dbo", "http://dbpedia.org/ontology/");
		Prefixer.INSTANCE.addPrefix("btl", "http://battles.com/");
		Prefixer.INSTANCE.addPrefix("owl", "http://www.w3.org/2002/07/owl#");
		Prefixer.INSTANCE.addPrefix("foaf", "http://xmlns.com/foaf/0.1/");
		Prefixer.INSTANCE.addPrefix("dbpedia-owl", "http://dbpedia.org/ontology/");
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		
		try {
			/*Connection aConn = ConnectionConfiguration
			        .to(DB_NAME)
			        .credentials("admin", "admin")
			        .server(LOCAL_DB_SERVER)
			        .reasoning(ReasoningType.SL)
			        .connect();
			*/
			Repository sesameServer = new HTTPRepository(LOCAL_DB_SERVER,DB_NAME);  
			sesameServer.initialize(); 
			
			RepositoryConnection rConn = sesameServer.getConnection();
			
			GraphQuery query = rConn.prepareGraphQuery(QueryLanguage.SPARQL, createRequest(request));
			query.setIncludeInferred(true);
			GraphQueryResult res = query.evaluate();
			
			//List<Statement> result = new ArrayList<Statement>();
			//ByteArrayOutputStream b = new ByteArrayOutputStream();
			//JSONLDWriter p = new JSONLDWriter(b);
			Map<String, JSONObject> map = new LinkedHashMap<String, JSONObject>();
			while(res.hasNext()){
				Statement s = res.next();
				if(!map.containsKey(s.getSubject().toString())){
					map.put(s.getSubject().toString(), new JSONObject());
				}
				map.get(s.getSubject().toString()).put(s.getPredicate().toString(), s.getObject().toString());
			}
			
			response.getWriter().append(JSONValue.toJSONString(map));
			
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private String createRequest(HttpServletRequest request){
		
		int isqno = -1;
		int year = 0;
		int limit;
		
		if(request.getParameter("limit") != null){
			try{
				limit = Integer.valueOf(request.getParameter("limit"));
			}catch(NumberFormatException e){
				limit = 1000;
			}
		}else{
			limit = 1000;
		}
		
		if(request.getParameter("isqno") != null){
			try{
				isqno = Integer.valueOf(request.getParameter("isqno"));
			}catch(NumberFormatException e){
				isqno = -1;
				// Wrong format, doing normal request
			}
		}
		if(request.getParameter("year") != null){
			try{
				year = Integer.valueOf(request.getParameter("year"));
			}catch(NumberFormatException e){
				year = -1;
				// Wrong format, doing normal request
			}
		}
		String result = Prefixer.INSTANCE.toString() + "\n"+
						"CONSTRUCT{ ?entity ?rel ?obj . } "+
						"WHERE { {" ;
		
		String subRequest = "?entity a btl:Battle ; \n";
		
		if(isqno != -1)
			subRequest += "\nbtl:isqno \""+isqno+"\"^^<http://www.w3.org/2001/XMLSchema#integer> ;\n";
		
		result += 		subRequest + 
							"?rel ?obj . }"+	
						"LIMIT "+limit;
		
		return result;
	}

}
