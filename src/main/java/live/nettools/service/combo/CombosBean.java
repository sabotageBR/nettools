package live.nettools.service.combo;

import javax.inject.Named;

import live.nettools.enums.TypeVideoDownload;

@Named
public class CombosBean {

	
	public TypeVideoDownload[] getListTypeVideoDownload() {
		return TypeVideoDownload.values();
	}
	
}
