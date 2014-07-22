package edu.georgetown.library.extractSierraBibs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;

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
	ExtractStats extractStats;
	
	static boolean isDebug() {return false;}
	static boolean isInfo() {return true;}
	
	HashMap<String,String> locationMap = new HashMap<>();
	static final String locationMapFile = "location_mapping.csv";

	GUExtractSierraBibs(ApiConfigFile apiConfig, QueryQueueFile queryQueueFile) throws IOException {
		this.apiConfig = apiConfig;
		this.queryQueueFile = queryQueueFile;
		
		extractStats = new ExtractStats(queryQueueFile.maxBib);
		//extractStats = new ExtractStats(1);
		
		Vector<Vector<String>> locationData = DelimitedFileReader.parseFile(new File(locationMapFile), ",");
		boolean header = true;
		for(Vector<String> row: locationData) {
			if (header) {
				header = false;
				continue;
			}
			if (row.size() < 3) continue;
			String item = row.get(0);
			String bib = row.get(2);
			locationMap.put(item, bib);
		}
	}
	
	public static String getBibQuery(int limit, int lastId, String filter) {
		StringBuffer buf = new StringBuffer();
		buf.append("bibs?limit=");
		buf.append(limit);
		buf.append("&id=[" + (lastId+1) + ",]");
		buf.append("&fields=id,varFields,fixedFields");
		buf.append(filter);
		if (isInfo()) System.out.println(buf.toString());
		return buf.toString();
	}
	
	public Date getEndDate() {
		return Calendar.getInstance().getTime();
	}
	
	public int getDurationDays() {
		return 1;
	}
	
    public static String getItemQuery(int limit, int offset, String bibIds) {
		StringBuffer ibuf = new StringBuffer();
		ibuf.append("items?limit=");
		ibuf.append(limit);
		ibuf.append("&offset=");
		ibuf.append(offset);
		ibuf.append("&bibIds=");
		ibuf.append(bibIds.toString());
		ibuf.append("&fields=default,varFields,fixedFields");
		if (isInfo()) System.out.println(ibuf.toString());
		return ibuf.toString();
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
		bib.addItem(iobj, locationMap);
	}
	
	public void extractAssociatedItems(OAuthConn oconn, TreeMap<Integer,IIIMarc> bibMarcMap, ExtractItemStats extractItemStats) throws OAuthSystemException, OAuthProblemException, IIIExtractException {
		while(!extractItemStats.isAllItemsRetrieved()) {
			String itemQ = getItemQuery(extractStats.reqSize, extractItemStats.offset, extractItemStats.bibIds);
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
	
	public void runTest() throws OAuthSystemException, OAuthProblemException, FileNotFoundException, IIIExtractException {
		OAuthConn oconn = new OAuthConn(apiConfig);

		QUERY_TYPE QT = QUERY_TYPE.UPDATED;
		QueryOutputFiles qofs = new QueryOutputFiles(QT, getEndDate(), 0);
				
		while (!extractStats.allBibsRetrieved()) {
			extractStats.start();
			//String bibQ = getBibQuery(extractStats.getBibLimit(), extractStats.lastId, QUERY_TYPE.ADDED.getQuery(getEndDate(), getDurationDays()));
			String bibQ = getBibQuery(extractStats.getBibLimit(), extractStats.lastId, QT.getQuery(getEndDate(), getDurationDays()));
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
				if (isInfo()) System.out.println(iiiMarc.marcRec.toString());
			}
			
			extractStats.end();
			
		}
        qofs.closeAll();
        extractStats.report();
        qofs.stats.report();
	}
	
	
	public static void main(String[] args) {		
		try {
			CommandLineParser parser = new BasicParser();
			Options options = QueryQueueFile.getOptions();
			try {
				CommandLine cl = parser.parse(options, args, false);
				String cfname = cl.getOptionValue(QueryQueueFile.O_CONFIG.getOpt(), "");
				if (cfname.isEmpty()) throw new ParseException("Config file must be specified");
				ApiConfigFile apiConfig = new ApiConfigFile(cfname);
				QueryQueueFile queryQueueFile = new QueryQueueFile(cl);
				
				System.exit(1);
				
				GUExtractSierraBibs oTest = new GUExtractSierraBibs(apiConfig, queryQueueFile);
				oTest.runTest();
			} catch (ParseException|IllegalArgumentException|java.text.ParseException|IOException e) {
				System.err.println(e);
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "java -jar GUExtractSierraBibs-1.0.jar", options);
				System.out.println("\n  -- OR -- \n");
				formatter.printHelp( "java -jar GUExtractSierraBibs-1.0.jar", options);
			}
		} catch (OAuthSystemException | OAuthProblemException | IIIExtractException e) {
			e.printStackTrace();
		}
	}


}
