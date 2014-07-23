package edu.georgetown.library.extractSierraBibs;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.json.JSONArray;
import org.json.JSONObject;

public class GUExtractSierraBibs {
	/* sample code for generating the Base64 header
	 * https://sandbox.iii.com/docs/#authAuth.htm%3FTocPath%3DSierra%2520
	 * API%2520Reference%7C_____2 
	 * String s = "Elvis:Presley1";
	 * System.out.println(Base64.encode(s.getBytes()));
	 */

	ApiConfigFile apiConfig;
	QueryQueueFile queryQueueFile;
	
	static boolean isDebug() {return false;}
	static boolean isInfo() {return true;}
	
	static final String locationMapFile = "location_mapping.csv";

	GUExtractSierraBibs(ApiConfigFile apiConfig, QueryQueueFile queryQueueFile) throws IOException {
		this.apiConfig = apiConfig;
		this.queryQueueFile = queryQueueFile;		
	}
	
	
	public Date getEndDate() {
		return Calendar.getInstance().getTime();
	}
	
	public int getDurationDays() {
		return 1;
	}
	

	public TreeMap<Integer,IIIMarc> mapBibs(JSONArray jarr) {
		TreeMap<Integer,IIIMarc> bibMarcMap = new TreeMap<>();
		for (int i = 0; i < jarr.length(); i++) {
			JSONObject obj = jarr.getJSONObject(i);
			IIIMarc iiiMarc;
			try {
				iiiMarc = new IIIMarc(obj);
			} catch (IIIExtractException e) {
				e.printStackTrace();
				continue;
			}
			
			bibMarcMap.put(iiiMarc.id, iiiMarc);
		}
		return bibMarcMap;
	}
	
	public void addItemToBib(TreeMap<Integer,IIIMarc> bibMarcMap, JSONObject iobj) {
		if (!iobj.has("bibIds")) {
			System.err.println("No bib arr for item");
			return;
		}
		JSONArray bibArr = iobj.getJSONArray("bibIds");
		if (bibArr.length() > 1) {
			System.err.println("bib arr not = 1");
		} else if (bibArr.length() == 0) {
			System.err.println("empty bib arr");
			return;
		}
		int bibId = bibArr.getInt(0);
		IIIMarc bib = bibMarcMap.get(bibId);
		if (bib == null) {
			System.err.println("Bib obj not found " + bibId);
			return;
		}
		bib.addItem(iobj, apiConfig.locationMap);
	}
	
	public void extractAssociatedItems(OAuthConn oconn, TreeMap<Integer,IIIMarc> bibMarcMap, ExtractItemStats extractItemStats) throws OAuthSystemException, OAuthProblemException, IIIExtractException {
		while(!extractItemStats.isAllItemsRetrieved()) {
			String itemQ = queryQueueFile.getItemQuery(extractItemStats);
			String iresp = oconn.getApiResult(itemQ);
			
			JSONObject iobjlist = new JSONObject(iresp);
			JSONArray iarr = iobjlist.getJSONArray("entries");
			int itotal = iobjlist.optInt("total", 0);
			if (isInfo()) System.out.println(iarr.length() + " Items retrieved");
			for (int i = 0; i < iarr.length(); i++) {
				JSONObject iobj = iarr.getJSONObject(i);
				addItemToBib(bibMarcMap, iobj);
			}
			extractItemStats.tallyItems(itotal, iarr.length());
		}		
	}
	
	public void runTest() throws OAuthSystemException, OAuthProblemException, IIIExtractException, IOException {

		OAuthConn oconn = new OAuthConn(apiConfig);
		QueryOutputFiles qofs = queryQueueFile.queryOutputFiles;
		
		ExtractStats extractStats = queryQueueFile.extractStats;

		while (!extractStats.allBibsRetrieved()) {
			extractStats.start();
			//String bibQ = getBibQuery(extractStats.getBibLimit(), extractStats.lastId, QUERY_TYPE.ADDED.getQuery(getEndDate(), getDurationDays()));
			String bibQ = queryQueueFile.getBibQuery();
			String resp = oconn.getApiResult(bibQ);
			
			JSONObject jobj = new JSONObject(resp);
			JSONArray jarr = jobj.getJSONArray("entries");
			if (isInfo()) System.out.println(jarr.length() + " Bibs retrieved");
			
			TreeMap<Integer,IIIMarc> bibMarcMap = mapBibs(jarr);
			ExtractItemStats extractItemStats = extractStats.tallyAndListBibs(bibMarcMap);
			if (!bibMarcMap.isEmpty()) {
				extractAssociatedItems(oconn, bibMarcMap, extractItemStats);
			}
			
			for(IIIMarc iiiMarc: bibMarcMap.values()) {
				//The following function is placed here in case the item recs need to inform the bib custom fields
				iiiMarc.appendCustomAndItemMarc();
				qofs.writeAll(iiiMarc);
				if (isDebug()) System.out.println(iiiMarc.marcRec.toString());
			}
			
			extractStats.end();
			
		}			
        qofs.closeAll();
        
        queryQueueFile.complete(extractStats.isResumeNeeded());
        extractStats.report();
        qofs.stats.report();
	}
	
	
	public static void main(String[] args) {		
		try {
			System.out.print("COMMAND LINE: ");
			for(String arg: args) {System.out.print(arg + " ");}
			System.out.println();
			CommandLineParser parser = new BasicParser();
			Options options = CommandLineOptions.getOptions();
			try {
				CommandLine cl = parser.parse(options, args, false);
				String cfname = cl.getOptionValue(CommandLineOptions.O_CONFIG.getOpt(), "");
				if (cfname.isEmpty()) throw new ParseException("Config file must be specified");
				ApiConfigFile apiConfig = new ApiConfigFile(cfname);
				
				String qpstr = cl.getOptionValue(CommandLineOptions.O_Q.getOpt(), ""); 
				
				//In case the DAILY param is selected, loop through all queries
				QUERY_PARAMS qp = QUERY_PARAMS.valueOf(qpstr);
				for(QUERY_TYPE qt: qp.queries) {
					QueryQueueFile queryQueueFile = new QueryQueueFile(apiConfig, cl, qt);
					GUExtractSierraBibs oTest = new GUExtractSierraBibs(apiConfig, queryQueueFile);
					oTest.runTest();					
				}
			} catch (ParseException|IllegalArgumentException|java.text.ParseException|IOException e) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "java -jar GUExtractSierraBibs-1.0.jar", options);
				System.out.println("\n  -- OR -- \n");
				formatter.printHelp( "java -jar GUExtractSierraBibs-1.0.jar", options);
				e.printStackTrace();
			}
		} catch (IIIExtractException e) {
			System.err.println(e.getMessage());
		} catch (OAuthSystemException | OAuthProblemException e) {
			e.printStackTrace();
		}
	}


}
