package ClientRequest;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openrdf.model.Statement;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;

import DataRetrieving.Prefixer;

import com.complexible.stardog.StardogException;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.reasoning.api.ReasoningType;

/**
 * Servlet implementation class Battle
 */
@WebServlet("/Battle")
public class Battle extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String LOCAL_DB_SERVER = "snarl://localhost:5820";
	private static final String DB_NAME = "Battles3";
	
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
		
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {
			ReasoningConnection aConn = ConnectionConfiguration
			        .to(DB_NAME)
			        .credentials("admin", "admin")
			        .server(LOCAL_DB_SERVER)
			        .reasoning(ReasoningType.SL)
			        .connect();
			
			
			GraphQueryResult res = aConn.graph(Prefixer.INSTANCE.toString() + "\nCONSTRUCT{ ?entity ?rel ?obj . } WHERE { ?entity a btl:Battle ; ?rel ?obj . }").execute();
			
			//List<Statement> result = new ArrayList<Statement>();
			//ByteArrayOutputStream b = new ByteArrayOutputStream();
			//JSONLDWriter p = new JSONLDWriter(b);
			Map<String, JSONObject> map = new LinkedHashMap<String, JSONObject>();
			JSONArray array = new JSONArray();
			while(res.hasNext()){
				Statement s = res.next();
				if(!map.containsKey(s.getSubject().toString())){
					map.put(s.getSubject().toString(), new JSONObject());
				}
				map.get(s.getSubject().toString()).put(s.getPredicate().toString(), s.getObject().toString());
			}
			
			response.getWriter().append(JSONValue.toJSONString(map));
			
		} catch (StardogException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}


}
