package edu.georgetown.library.extractSierraBibs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public enum FILE_OUTPUT {
	WRLC_DML_NEW(
		QUERY_TYPE.ADDED, 
		FILE_TARGET.WrlcCatalog, 
		"DANEW",
		null,
		CatDate.NotBlank,
		LocDml.DML,
		LocPio.NA,
		LocInet.NA,
		WrlcEbsco.N,
		Material.NA
	),
	WRLC_DML_UPDATE(
		QUERY_TYPE.UPDATED, 
		FILE_TARGET.WrlcCatalog, 
		"DAUPDATE",
		null, 
		CatDate.NotBlank,
		LocDml.DML,
		LocPio.NA,
		LocInet.NA,
		WrlcEbsco.N,
		Material.NA
	),
	WRLC_GT_NEW(
		QUERY_TYPE.ADDED, 
		FILE_TARGET.WrlcCatalog, 
		"GTNEW",
		null, 
		CatDate.NotBlank,
		LocDml.NoDML,
		LocPio.NoPio,
		LocInet.NA,
		WrlcEbsco.N,
		Material.NA
	),
	WRLC_GT_UPDATE(
		QUERY_TYPE.UPDATED, 
		FILE_TARGET.WrlcCatalog, 
		"GTUPDATE",
		null, 
		CatDate.NotBlank,
		LocDml.NoDML,
		LocPio.NoPio,
		LocInet.NA,
		WrlcEbsco.N,
		Material.NA
	),

	WRLC_DML_ALL(
		QUERY_TYPE.ALL, 
		FILE_TARGET.WrlcCatalogFull, 
		"DML",
		null, 
		CatDate.NotBlank,
		LocDml.DML,
		LocPio.NA,
		LocInet.NA,
		WrlcEbsco.N,
		Material.NA
	),
	WRLC_GT_ALL(
		QUERY_TYPE.ALL, 
		FILE_TARGET.WrlcCatalogFull, 
		"GT_[SEQ]",
		null, 
		CatDate.NotBlank,
		LocDml.NoDML,
		LocPio.NoPio,
		LocInet.NA,
		WrlcEbsco.N,
		Material.NA
	),

	SUMMON_GT_DELETE_NOER(
		QUERY_TYPE.DELETED, 
		FILE_TARGET.Summon, 
		"GTDELETES_SUMMON_NOER_",
		QueryQueueFile.MMDDYY, 
		CatDate.NA,
		LocDml.NA,
		LocPio.NoPio,
		LocInet.NoInet,
		WrlcEbsco.NA,
		Material.NoEorM
	),
	SUMMON_GT_DELETE_ER(
		QUERY_TYPE.DELETED, 
		FILE_TARGET.Summon, 
		"GTDELETES_SUMMON_ER_",
		QueryQueueFile.MMDDYY, 
		CatDate.NA,
		LocDml.NA,
		LocPio.NoPio,
		LocInet.Inet,
		WrlcEbsco.NA,
		Material.EorM
	),
	SUMMON_GT_ADDUP_NOER(
		QUERY_TYPE.ADDED_OR_UPDATED, 
		FILE_TARGET.Summon, 
		"GTUPDATES_SUMMON_NOER_",
		QueryQueueFile.MMDDYY, 
		CatDate.NA,
		LocDml.NA,
		LocPio.NoPio,
		LocInet.NoInet,
		WrlcEbsco.NA,
		Material.NoEorM
	),
	SUMMON_GT_ADDUP_ER(
		QUERY_TYPE.ADDED_OR_UPDATED, 
		FILE_TARGET.Summon, 
		"GTUPDATES_SUMMON_ER_",
		QueryQueueFile.MMDDYY, 
		CatDate.NA,
		LocDml.NA,
		LocPio.NoPio,
		LocInet.Inet,
		WrlcEbsco.NA,
		Material.EorM
	),

	SUMMON_DML_ALL_ER(
			QUERY_TYPE.ALL, 
			FILE_TARGET.SummonFull, 
			"DML_ER_",
			QueryQueueFile.MMDDYY, 
			CatDate.NotBlank,
			LocDml.DML,
			LocPio.NA,
			LocInet.NA,
			WrlcEbsco.NA,
			Material.EorM
		),
	SUMMON_DML_ALL_NOER(
		QUERY_TYPE.ALL, 
		FILE_TARGET.SummonFull, 
		"DML_NOER_",
		QueryQueueFile.MMDDYY, 
		CatDate.NotBlank,
		LocDml.DML,
		LocPio.NA,
		LocInet.NoInet,
		WrlcEbsco.NA,
		Material.NoEorM
	),
	SUMMON_GT_ALL_ER(
			QUERY_TYPE.ALL, 
			FILE_TARGET.SummonFull, 
			"GT_ER_[SEQ]",
			QueryQueueFile.MMDDYY, 
			CatDate.NotBlank,
			LocDml.NoDML,
			LocPio.NoPio,
			LocInet.Inet,
			WrlcEbsco.NA,
			Material.EorM
		),
	SUMMON_GT_ALL_NOER(
		QUERY_TYPE.ALL, 
		FILE_TARGET.SummonFull, 
		"GT_NOER_[SEQ]",
		QueryQueueFile.MMDDYY, 
		CatDate.NotBlank,
		LocDml.NoDML,
		LocPio.NoPio,
		LocInet.NoInet,
		WrlcEbsco.NA,
		Material.NoEorM
	),
	
	GW_GT_DELETE(
		QUERY_TYPE.DELETED, 
		FILE_TARGET.GW_Surveyor, 
		"GTDatabaseDeletes",
		QueryQueueFile.MMDDYY, 
		CatDate.NA,
		LocDml.NA,
		LocPio.NA,
		LocInet.NA,
		WrlcEbsco.NA,
		Material.NA
	),
	GW_GT_DELETE_NER(
		QUERY_TYPE.DELETED, 
		FILE_TARGET.GW_Surveyor, 
		"GTDatabaseDeletesNER",
		QueryQueueFile.MMDDYY, 
		CatDate.NA,
		LocDml.NA,
		LocPio.NA,
		LocInet.NoInet,
		WrlcEbsco.NA,
		Material.NoEorM
	),
	GW_DML_ADDUP(
		QUERY_TYPE.ADDED_OR_UPDATED, 
		FILE_TARGET.GW_Surveyor,
		"DMLDatabase", 
		QueryQueueFile.MMDDYY, 
		CatDate.NotBlank,
		LocDml.DML,
		LocPio.NA,
		LocInet.NA,
		WrlcEbsco.NA,
		Material.NA
	),
	GW_DML_ADDUP_NER(
		QUERY_TYPE.ADDED_OR_UPDATED, 
		FILE_TARGET.GW_Surveyor, 
		"DMLDatabaseNER",
		QueryQueueFile.MMDDYY, 
		CatDate.NotBlank,
		LocDml.DML,
		LocPio.NA,
		LocInet.NoInet,
		WrlcEbsco.NA,
		Material.NoEorM
	),
	GW_GT_ADDUP(
		QUERY_TYPE.ADDED_OR_UPDATED, 
		FILE_TARGET.GW_Surveyor,
		"GTDatabase", 
		QueryQueueFile.MMDDYY, 
		CatDate.NotBlank,
		LocDml.NoDML,
		LocPio.NoPio,
		LocInet.NA,
		WrlcEbsco.NA,
		Material.NA
	),
	GW_GT_ADDUP_NER(
		QUERY_TYPE.ADDED_OR_UPDATED, 
		FILE_TARGET.GW_Surveyor,
		"GTDatabaseNER", 
		QueryQueueFile.MMDDYY, 
		CatDate.NotBlank ,
		LocDml.NoDML,
		LocPio.NoPio,
		LocInet.NoInet,
		WrlcEbsco.NA,
		Material.NoEorM
	),
	;	

	public enum FILE_TARGET {
		WrlcCatalog,
		WrlcCatalogFull,
		Summon,
		SummonFull,
		GW_Surveyor;
	}
	
	interface MarcFilter {
		boolean test(IIIMarc obj);
	}
	
	enum CatDate implements MarcFilter {
		NotBlank {
			public boolean test(IIIMarc obj) {
				return !obj.sfCatalogDate.isEmpty();
			}			
		},
		NA;
		public boolean test(IIIMarc obj) {
			return true;
		}
	}
	
	enum LocDml implements MarcFilter{
		DML{
			public boolean test(IIIMarc obj) {
				return obj.uniqueBibLocs.contains("dml");
			}			
		},
		NoDML{
			public boolean test(IIIMarc obj) {
				return !obj.uniqueBibLocs.contains("dml");
			}			
		},
		NA;
		public boolean test(IIIMarc obj) {
			return true;
		}
	}
	enum LocPio implements MarcFilter {
		NoPio{
			public boolean test(IIIMarc obj) {
				return !obj.uniqueBibLocs.contains("pio");
			}			
		},
		NA;
		public boolean test(IIIMarc obj) {
			return true;
		}
	}
	enum LocInet implements MarcFilter {
		Inet{
			public boolean test(IIIMarc obj) {
				return obj.uniqueBibLocs.contains("inet");
			}			
		},
		NoInet{
			public boolean test(IIIMarc obj) {
				return !obj.uniqueBibLocs.contains("inet");
			}			
		},
		NA;
		public boolean test(IIIMarc obj) {
			return true;
		}
	}
	enum WrlcEbsco implements MarcFilter {
		N,
		NA;
		public boolean test(IIIMarc obj) {
			return true;
		}
	}
	enum Material implements MarcFilter {
		EorM,
		NoEorM,
		NA;
		public boolean test(IIIMarc obj) {
			return true;
		}
	}
	
	QUERY_TYPE queryType;
	FILE_TARGET fileTarget;
	String filePrefix;
	SimpleDateFormat dateMask = null;

	CatDate catdate;
	LocDml locDml; 
	LocPio locPio; 
	LocInet locInet;
	WrlcEbsco wrlcEbsco;
	Material material;
	Vector<MarcFilter> filters = new Vector<>();
	
	public File getFile(Date date, String seq) {
		String ROOT = "data/";
		String dirname = ROOT + fileTarget.name() + "/" + QueryQueueFile.YYYYMMDD.format(date);
		File dir = new File(dirname);
		dir.mkdirs();
		StringBuffer fname = new StringBuffer();
		fname.append(filePrefix.replace("[SEQ]", seq));
		if (dateMask != null) {
			fname.append(dateMask.format(date));
		}
		fname.append(".mrc");
		return new File(dir, fname.toString());
	}
	
	public boolean test(IIIMarc iiiMarc) {
		for(MarcFilter filter: filters) {
			if (!filter.test(iiiMarc)) return false;
		}
		return true;
	}

	FILE_OUTPUT(
			QUERY_TYPE queryType, 
			FILE_TARGET fileTarget, 
			String filePrefix, 
			SimpleDateFormat fmt, 
			CatDate catdate, 
			LocDml locDml, 
			LocPio locPio, 
			LocInet locInet,
			WrlcEbsco wrlcEbsco,
			Material material
		) {
		this.queryType = queryType;
		this.fileTarget = fileTarget;
		this.filePrefix = filePrefix;
		this.dateMask = fmt;

		this.filters.add(catdate);
		this.filters.add(locDml);
		this.filters.add(locPio);
		this.filters.add(locInet);
		this.filters.add(wrlcEbsco);
		this.filters.add(material);
	}
	
	String getFileName(Date endDate) {
		StringBuffer buf = new StringBuffer();
		buf.append(filePrefix);
		if (dateMask != null) buf.append(dateMask.format(endDate));
		buf.append(".dat");
		return buf.toString();
	}
	
	static void report() {
		for(FILE_OUTPUT fout: FILE_OUTPUT.values()) {
			System.out.print(String.format("%30s\t",fout.filePrefix));
			System.out.print(String.format("%10s\t",fout.catdate));
			System.out.print(String.format("%10s\t",fout.locDml));
			System.out.print(String.format("%10s\t",fout.locPio));
			System.out.print(String.format("%10s\t",fout.locInet));
			System.out.print(String.format("%10s\t",fout.wrlcEbsco));
			System.out.print(String.format("%10s\t",fout.material));
			System.out.println();
		}
	}
	public static void main(String[] args) {		
		report();
	}
}