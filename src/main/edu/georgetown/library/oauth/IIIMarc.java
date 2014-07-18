package edu.georgetown.library.oauth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

class IIIMarc {
	Record marcRec;
	JSONObject json;
	static MarcFactory mf = MarcFactory.newInstance();
	int id = -1;
	Vector<String> uniqueBibLocs = new Vector<>();
	String sfCatalogDate = "";
	String sfMaterialType = "";
	String sf710a = "";
	
	
	static SimpleDateFormat DFMT = new SimpleDateFormat("MM-dd-yy");
	
	static SimpleDateFormat[] DPARSE = {new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'"), new SimpleDateFormat("yyyy-MM-dd")};
	
	static Comparator<String> marcCompare = new Comparator<String>(){
		public int compare(String o1, String o2) {
			if (o1.equals("910")) o1="998a";
			if (o2.equals("910")) o2="998a";
			return o1.compareTo(o2);
		}
	};
	TreeMap<String,JSONObject> tagMap = new TreeMap<>(marcCompare);
	DataField f907 = mf.newDataField("907",' ', ' ');
	DataField f998 = mf.newDataField("998",' ', ' ');
	Vector<DataField> arr945 = new Vector<>();
	
	IIIMarc(JSONObject json) throws IIIExtractException {
		this.json = json;
		marcRec = mf.newRecord();
		if (json.has("id")) {
			id = (Integer) json.get("id");
			marcRec.setId((long)id);
		} else {
			throw new IIIExtractException("No Id");
		}
		
		tagMap.put("907", json);
		tagMap.put("998", json);

		//if (json.has("marc")) {
		//	addMarcAttr(tagMap, json.getJSONObject("marc"));
		//} else 
		if (json.has("varFields")){
			addVarFieldsAttr(tagMap, json.getJSONArray("varFields"));
		}
	}
	
	void appendCustomAndItemMarc() {
		for(String loc: uniqueBibLocs) {
			addSf(f998, 'a', loc);
		}
		extract907values(json);
		extract998values(json);
		for(DataField f945: arr945) {
			marcRec.addVariableField(f945);
		}		
	}

	/*
	void addMarcAttr(TreeMap<String,JSONObject> tagMap, JSONObject mobj) {
		setLeader(mobj, "leader");
		if (mobj.has("fields")) {
			JSONArray farr = mobj.getJSONArray("fields");
			for (int fi = 0; fi < farr.length(); fi++) {
				JSONObject field = farr.getJSONObject(fi);
				String tag = field.getString("tag");
				tagMap.put(tag, field);
			}
			for(String tag: tagMap.keySet()) {
				JSONObject field = tagMap.get(tag);
				if (tag.equals("907")) {
					marcRec.addVariableField(f907);
				} else if (tag.equals("998")) {
					marcRec.addVariableField(f998);					
				} else if (field.has("data")) {
					JSONObject data = field.getJSONObject("data");
					setDataField(data, tag, "code", "data");
				} else if (field.has("value")) {
					setControlField(field, tag, "value");
				}				
			}
		}				
	}
	*/

	void extract907values(JSONObject obj) {
		JSONObject fixed = obj.has("fixedFields") ? obj.getJSONObject("fixedFields") : null;
		addSfString(f907, 'a', obj, "id");
		addSfFixedDate(f907, 'b', fixed, "84", "value"); //update
		addSfFixedDate(f907, 'c', fixed, "83", "value"); //created
	}
	
	void extract998values(JSONObject obj) {
		JSONObject fixed = obj.has("fixedFields") ? obj.getJSONObject("fixedFields") : null;
		
		//When multi, the value should come from varFields, but it is not yet present in api output
		//addSfFixed(f998, 'a', fixed, "26", "value"); //location
		
		addSfFixedDate(f998, 'b', fixed, "28", "value"); //catalog date
		addSfFixed(f998, 'c', fixed, "29", "value"); //bib lev
		addSfFixed(f998, 'd', fixed, "30", "value"); //matl type
		addSfFixed(f998, 'e', fixed, "31", "value"); //supress
		addSfFixed(f998, 'f', fixed, "24", "value"); //lang
		addSfFixed(f998, 'g', fixed, "89", "value"); //country
		addSfFixed(f998, 'h', fixed, "25", "value"); //skip
		addSfFixed(f998, 'i', fixed, "27", "value"); //copy count
	}

	void addVarFieldsAttr(TreeMap<String,JSONObject> tagMap, JSONArray farr) {
		for (int fi = 0; fi < farr.length(); fi++) {
			JSONObject field = farr.getJSONObject(fi);
			
			String ftag = field.optString("fieldTag","");
			if (ftag.equals("_")) {
				setLeader(field, "content");
				continue;
			}

			String tag = field.optString("marcTag","");
			if (tag.isEmpty()) {
				continue;
			}
			tagMap.put(tag, field);
		}
		
		for(String tag: tagMap.keySet()) {
			JSONObject field = tagMap.get(tag);
			if (tag.equals("907")) {
				marcRec.addVariableField(f907);
			} else if (tag.equals("998")) {
				marcRec.addVariableField(f998);					
			} else if (field.has("content")) {
				setControlField(field, tag, "content");
			} else {
				setDataField(field, tag, "tag", "content");
			}
		}
	}

	void setDataField(JSONObject jo, String tag, String codeAttr, String valAttr) {
		DataField df = mf.newDataField();
		df.setTag(tag);
		marcRec.addVariableField(df);
		
		if (jo.has("ind1")) df.setIndicator1(jo.getString("ind1").charAt(0));
		if (jo.has("ind2")) df.setIndicator2(jo.getString("ind2").charAt(0));

		if (jo.has("subfields")) {
			JSONArray sfarr = jo.getJSONArray("subfields");
			for (int sfi = 0; sfi < sfarr.length(); sfi++) {
				JSONObject sfdata = sfarr.getJSONObject(sfi);
				Subfield sdf = mf.newSubfield(sfdata.getString(codeAttr).charAt(0), sfdata.getString(valAttr));
				df.addSubfield(sdf);
				char code = sfdata.getString(codeAttr).charAt(0);
				if (tag.equals("710") && (code == 'a')) sf710a = sfdata.getString(valAttr);
			}
		}
	}
	
	
	void setControlField(JSONObject jo, String tag, String valAttr) {
		ControlField cf = mf.newControlField(tag, jo.getString(valAttr));
		marcRec.addVariableField(cf);						
	}
	
	void setLeader(JSONObject jo, String leaderAttr) {
		String sLeader = jo.optString(leaderAttr);
		if (!sLeader.isEmpty())	{
			marcRec.setLeader(mf.newLeader(sLeader));
		}
	}
	
	
	
	void addItem(JSONObject item, HashMap<String,String> locationMap) {
		DataField f945 = mf.newDataField("945",' ', ' ');
		arr945.add(f945);

		JSONObject fixed = item.has("fixedFields") ? item.getJSONObject("fixedFields") : null;
		JSONArray var = item.has("varFields") ? item.getJSONArray("varFields") : null;
		
		//addSfFixed(f945, '0', fixed, "78", "value"); //oclc output date ??
		if (!addSfString(f945, 'a', item, "callNumber")) {
			addSfVarByFieldTag(f945, 'a', var, "fieldTag", "c");
		}
		addSfVarByFieldTag(f945, 'c', var, "fieldTag", "v"); //volume
		addSfVarByFieldTagDate(f945, 'd', var, "fieldTag", "r"); //last reserve date
		addSfFixed(f945, 'g', fixed, "58", "value"); // copy #
		addSfFixed(f945, 'h', fixed, "93", "value"); // in house uses
		if (!addSfString(f945, 'i', item, "barcode")) {
			addSfVarByFieldTag(f945, 'i', var, "fieldTag", "b");
		}
		addSfFixedDate(f945, 'j', fixed, "68", "value"); // last checkin date
		addSfFixedDate(f945, 'k', fixed, "78", "value"); // last checkout date ??
		
		//Since bib locs are not present in api output, attempted to pull item locs
		String loc = getSfFixed(item, "location", "code");
		if (loc == null) loc = "";
		if (loc.isEmpty()) {
			loc = getSfFixed(fixed, "79", "value");
		}
		if (loc == null) loc = "";
		
		String bibLoc = locationMap.get(loc);
		if (bibLoc != null) loc = bibLoc;
		
		if (!loc.isEmpty()) {
			loc = loc.trim();
			addSf(f945, 'l', loc);
			if (!uniqueBibLocs.contains(loc)) {
				uniqueBibLocs.add(loc);
			}
		}
				
		addSfVarByFieldTag(f945, 'm', var, "fieldTag", "m"); //message
		addSfVarByFieldTag(f945, 'n', var, "fieldTag", "w"); //note
		addSfFixed(f945, 'o', fixed, "60", "value"); // icode
		addSfFixed(f945, 'p', fixed, "62", "value"); // price
		addSfFixed(f945, 'q', fixed, "97", "value"); // imessage
		addSfFixed(f945, 'r', fixed, "108", "value"); // opacmsg
		addSfFixed(f945, 's', fixed, "88", "value"); // status
		addSfFixed(f945, 't', fixed, "61", "value"); // itype
		addSfFixed(f945, 'u', fixed, "76", "value"); // total checkouts
		addSfFixed(f945, 'v', fixed, "77", "value"); // total renewals

		addSfFixed(f945, 'w', fixed, "109", "value"); // ytdcirc
		addSfFixed(f945, 'x', fixed, "110", "value"); // lyrcirc
		if (!addSfString(f945, 'y', item, "id")) {
			addSfFixed(f945, 'y', fixed, "81", "value"); //item record number 
		}
		if (!addSfDate(f945, 'z', item, "createdDate")) {
			addSfFixed(f945, 'z', fixed, "83", "value"); // item created date
		}
	}
	
	String parseDate(String s) {
		for(SimpleDateFormat dparse: DPARSE) {
			try {
				Date date = dparse.parse(s);
				return DFMT.format(date);
			} catch (ParseException e) {
			}				
		}
		System.err.println("DATE: "+s);
		return null;
	}
	
	boolean addSf(DataField field, char sfc, String val) {
		if (val == null) return false;
		Subfield sf = mf.newSubfield(sfc, val);
		field.addSubfield(sf);
		if (field.getTag().equals("998")) {
			if (sfc == 'd') sfMaterialType = val;
			if (sfc == 'b') sfCatalogDate = val;			
		}
		return true;
	}
	boolean addSfString(DataField field, char sfc, JSONObject jo, String key) {
		if (jo == null) return false;
		if (!jo.has(key)) return false;
		return addSf(field, sfc, jo.optString(key,""));
	}
	boolean addSfDate(DataField field, char sfc, JSONObject jo, String key) {
		if (jo == null) return false;
		if (!jo.has(key)) return false;
		String s = parseDate(jo.optString(key,""));
		if (s == null) return false;
		return addSf(field, sfc, s);
	}
	
	String getSfFixed(JSONObject jo, String key, String childKey) {
		if (jo == null) return null;
		if (!jo.has(key)) return null;
		JSONObject joc = jo.getJSONObject(key);
		if (!joc.has(childKey)) return null;
		return joc.optString(childKey,"");
	}

	boolean addSfFixed(DataField field, char sfc, JSONObject jo, String key, String childKey) {
		String s = getSfFixed(jo, key, childKey);
		if (s == null) return false;
		return addSf(field, sfc, s);
	}

	boolean addSfFixedDate(DataField field, char sfc, JSONObject jo, String key, String childKey) {
		String s = getSfFixed(jo, key, childKey);
		if (s == null) return false;
		s = parseDate(s);
		if (s == null) return false;
		return addSf(field, sfc, s);
	}
	String getSfVarByFieldTag(JSONArray jarr, String key, String val) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < jarr.length(); i++) {
			JSONObject vf = jarr.getJSONObject(i);
			if (vf.optString(key,"").equals(val)){
				if (vf.has("content")) {
					buf.append(vf.getString("content"));
				} else if (vf.has("subfields")) {
					JSONArray sjarr = vf.getJSONArray("subfields");
					for(int j=0; j < sjarr.length(); j++) {
						JSONObject svf = sjarr.getJSONObject(j);
						buf.append(svf.optString("content",""));
					}						
				} else {
					return null;
				}
			}
		}
		if (buf.length() == 0) return null;
		return buf.toString();
	}
	boolean addSfVarByFieldTag(DataField field, char sfc, JSONArray jarr, String key, String val) {
		String s = getSfVarByFieldTag(jarr, key, val);
		if (s == null) return false;
		return addSf(field, sfc, s);
	}
	
	boolean addSfVarByFieldTagDate(DataField field, char sfc, JSONArray jarr, String key, String val) {
		String s = getSfVarByFieldTag(jarr, key, val);
		if (s == null) return false;
		s = parseDate(s);
		if (s == null) return false;
		return addSf(field, sfc, s);
	}
}

