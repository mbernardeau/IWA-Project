package DataRetrieving;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import com.complexible.stardog.StardogException;
import com.opencsv.CSVReader;

public class CSVRetriever {
	RepositoryConnection stardogConnection;
	private static ValueFactoryImpl vf= ValueFactoryImpl.getInstance();

	private static final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private static final String OWL  = "http://www.w3.org/2002/07/owl#";
	private static final String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
	private static final String XSD = "http://www.w3.org/2001/XMLSchema#";
	private static final String BTL = "http://battles.com/";
	private static final String FOAF = "http://xmlns.com/foaf/0.1/";

	public CSVRetriever(RepositoryConnection stardogConnection){
		this.stardogConnection = stardogConnection;
	}


	public void importWeather(URL file) throws StardogException, IOException, QueryEvaluationException, RepositoryException, MalformedQueryException{
		stardogConnection.begin();
		CSVReader reader = new CSVReader(new FileReader(file.getPath().replace("%20", " ")), ',');
		Map<String, Map<String, String>> shorts = new HashMap<String, Map<String, String>>();
		String [] nextLine;
		String [] headers = reader.readNext();

		TupleQueryResult res = stardogConnection.prepareTupleQuery(QueryLanguage.SPARQL, Prefixer.INSTANCE.toString() + "\nSELECT ?instance ?short ?type WHERE{ {?instance a btl:wx1 ; btl:short ?short ; a ?type . } UNION {?instance a btl:wx2 ; btl:short ?short ; a ?type . } UNION {?instance a btl:wx3 ; btl:short ?short ; a ?type . } UNION {?instance a btl:wx4 ; btl:short ?short ; a ?type . } FILTER(?type != owl:Thing)}").evaluate();
		while(res.hasNext()){
			BindingSet set = res.next();
			if(shorts.get(set.getBinding("type").getValue().toString()) == null){
				initObjectProperty(set.getBinding("type").getValue().toString());
				shorts.put(set.getBinding("type").getValue().toString(), new HashMap<String, String>());
			}
			shorts.get(set.getBinding("type").getValue().toString()).put(set.getBinding("short").getValue().toString().replace("\"", ""), set.getBinding("instance").getValue().toString().replace("\"", ""));	
		}

		while ((nextLine = reader.readNext()) != null) {
			res = stardogConnection.prepareTupleQuery(QueryLanguage.SPARQL, Prefixer.INSTANCE + "\nSELECT ?battle WHERE{ ?battle btl:isqno "+nextLine[0]+" . }")
					.evaluate();
			if(res.hasNext()){
				URI battleEntity  = vf.createURI(res.next().getBinding("battle").getValue().stringValue());

				for(int i = 2; i < nextLine.length; i++){
					Map<String, String> wx = shorts.get(BTL + headers[i]);

					if(nextLine[i] != null && nextLine[i].length()>0 && wx.get(nextLine[i]) != null)
						stardogConnection.add(vf.createStatement(battleEntity,
								createEntity(BTL, headers[i]),
								vf.createURI(wx.get(nextLine[i]))));
				}
			}
		}

		stardogConnection.commit();
		reader.close();
		//	CSVReader reader = new CSVReader(new FileReader("../../CDB13_data/weather.csv"));
	}


	public void importEnum(String file, String name) throws IOException, StardogException, RepositoryException{

		stardogConnection.begin();
		CSVReader reader = new CSVReader(new FileReader(file), ',');

		//Resource aContext = vf.createURI(BTL);
		initDatatypeProperty("short");
		initClass(toUpper(name, '_'));


		String [] nextLine;
		reader.readNext();

		while ((nextLine = reader.readNext()) != null) {
			if(nextLine[1] != null  && nextLine[1].length()>0){
				addInstance(nextLine[1], name);
				stardogConnection.add(vf.createStatement(createEntity(BTL, nextLine[1]),
						vf.createURI(BTL, "short"),
						vf.createLiteral(nextLine[0])));
			}
		}

		stardogConnection.commit();
		reader.close();
	}



	public void importBattles(URL file) throws StardogException, IOException, NumberFormatException, RepositoryException{
		stardogConnection.begin();
		CSVReader reader = new CSVReader(new FileReader(file.getPath().replace("%20", " ")), ',');
		List<Integer> toAdd = new ArrayList<Integer>();
		toAdd.add(3);
		toAdd.add(4);

		String [] nextLine;
		String [] headers = reader.readNext();
		List<String> wars = new LinkedList<String>();


		initClass("Battle");
		initClass("War");
		initDatatypeProperty(headers[0]);
		initObjectProperty("war");

		for(Integer column : toAdd){
			initDatatypeProperty(headers[column]);
		}

		while ((nextLine = reader.readNext()) != null) {
			URI battleEntity = createEntity(BTL, nextLine[2]);
			boolean hasWar = nextLine[1] != null && nextLine[1].length() > 0;

			if(hasWar && !wars.contains(nextLine[1])){ // War is not already defined
				addInstance(nextLine[1], "War");
				wars.add(nextLine[1]);
			}

			addInstance(nextLine[2], "Battle");
			
			if(hasWar)
				stardogConnection.add(vf.createStatement(battleEntity, 
						vf.createURI(BTL, "war"), 
						createEntity(BTL, nextLine[1])));

			stardogConnection.add(vf.createStatement(battleEntity,
					vf.createURI(BTL, headers[0]),
					vf.createLiteral(Integer.valueOf(nextLine[0]))
					));

			if(nextLine[44] != null && nextLine[44].length() > 0){
				stardogConnection.add(vf.createStatement(battleEntity, 
						vf.createURI(OWL, "sameAs"),
						vf.createURI(nextLine[44])));
				
				/*stardogConnection.add(vf.createStatement(vf.createURI(nextLine[44]), 
						vf.createURI(OWL, "sameAs"),
						battleEntity));*/
			}
			
			for(Integer column : toAdd){
				stardogConnection.add(vf.createStatement(battleEntity,
						vf.createURI(BTL, headers[column]),
						vf.createLiteral(nextLine[column])
						));
			}
		}

		stardogConnection.commit();
		reader.close();
	}

	public void importCommanders(URL file) throws StardogException, IOException, QueryEvaluationException, RepositoryException, MalformedQueryException{
		stardogConnection.begin();
		CSVReader reader = new CSVReader(new FileReader(file.getPath().replace("%20", " ")), ',');
		String [] headers = reader.readNext();
		String [] nextLine;
		List<String> commanders = new LinkedList<String>();

		initClass("Commander");
		initObjectProperty("commander");


		while ((nextLine = reader.readNext()) != null) {
			if(nextLine[3] != null && nextLine[3].length() > 0){
				TupleQueryResult res = stardogConnection.prepareTupleQuery(QueryLanguage.SPARQL, Prefixer.INSTANCE + "\nSELECT ?battle WHERE{ ?battle btl:isqno \""+nextLine[0]+"\"^^xsd:int . }")
						.evaluate();
				if(nextLine[0].equals("347")){
					System.out.println("Itération :"+nextLine[0]);
				}
				
				while(res.hasNext()){
					URI battleEntity  = vf.createURI(res.next().getBinding("battle").getValue().stringValue());

					if(!commanders.contains(nextLine[3])){ // Commander is not already defined
						addInstance(nextLine[3], "Commander");
						commanders.add(nextLine[3]);
					}
					
					if(nextLine[4] != null &&  nextLine[4].length() > 5)
						stardogConnection.add(vf.createStatement(createEntity(BTL, nextLine[3]), 
								vf.createURI(FOAF, "isPrimaryTopicOf"),
								vf.createURI(nextLine[4].startsWith("http://") ? nextLine[4] : "http://en.wikipedia.org/wiki/"+nextLine[4])
								));

					stardogConnection.add(vf.createStatement(battleEntity,
							vf.createURI(BTL, "commander"),
							createEntity(BTL, nextLine[3])
							));
					stardogConnection.add(vf.createStatement(createEntity(BTL, nextLine[3]),
							vf.createURI(BTL, "side"),
							vf.createLiteral(nextLine[2])
							));
				}
			}
		}

		stardogConnection.commit();
		reader.close();
	}

	private String toUpper(String in, char separator){
		StringBuffer res = new StringBuffer();
		String[] strArr = in.split(" ");
		for (String str : strArr) {
			if(str.length() > 0){
				char[] stringArray = str.trim().toCharArray();
				stringArray[0] = Character.toUpperCase(stringArray[0]);
				str = new String(stringArray);

				res.append(str).append(separator);
			}
		}
		return res.deleteCharAt(res.length()-1).toString();
	}

	private void addInstance(String name, String clazz) throws StardogException, RepositoryException{
		stardogConnection.add(vf.createStatement(createEntity(BTL, name),
				vf.createURI(RDF, "type"),
				vf.createURI(BTL, clazz)));
		stardogConnection.add(vf.createStatement(createEntity(BTL, name),
				vf.createURI(RDFS, "label"),
				vf.createLiteral(toUpper(name, ' '), "en")));
		stardogConnection.add(vf.createStatement(createEntity(BTL, name),
				vf.createURI(RDF, "type"),
				vf.createURI(OWL, "Thing")));
	}


	private URI createEntity(String context, String name){
		return vf.createURI(context, toUpper(name, '_').replace('/', '_'));
	}

	private void initDatatypeProperty(String name) throws StardogException, RepositoryException{
		stardogConnection.add(vf.createStatement(
				vf.createURI(BTL, name), 
				vf.createURI(RDF, "type"), 
				vf.createURI(OWL, "DatatypeProperty")));
		/*stardogConnection.add(vf.createStatement(
				vf.createURI(BTL, name), 
				vf.createURI(RDFS, "subPropertyOf"), 
				vf.createURI(RDF, "Property")));*/
	}

	private void initObjectProperty(String name) throws StardogException, RepositoryException{
		stardogConnection.add(vf.createStatement(
				vf.createURI(BTL, name), 
				vf.createURI(RDF, "type"), 
				vf.createURI(OWL, "ObjectProperty")));
		stardogConnection.add(vf.createStatement(
				vf.createURI(BTL, name), 
				vf.createURI(RDFS, "subPropertyOf"), 
				vf.createURI(RDF, "Property")));

	}
	private void initClass(String name) throws StardogException, RepositoryException{
		stardogConnection.add(vf.createStatement(
				vf.createURI(BTL, name), 
				vf.createURI(RDF, "type"), 
				vf.createURI(OWL, "Class")));
		stardogConnection.add(vf.createStatement(
				vf.createURI(BTL, name),
				vf.createURI(RDFS, "subClassOf"), 
				vf.createURI(OWL, "Thing")));
	}
}
