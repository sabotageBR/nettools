package live.nettools.to;

import java.io.Serializable;

public class HeaderTO implements Serializable{
	
	private static final long serialVersionUID = 5757288565705585629L;
	
	private String key;
	private String value;
	
	public HeaderTO(String key, String value) {
		setKey(key);
		setValue(value);
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	
}
