package ro.amihai.dht.keyvaluestore;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class KeyValue {
	
	@NotNull
    @Size(min=1, max=64)
	@Pattern(regexp="[a-zA-Z_-]*")
	private String key;

	@NotNull
	private String value;
	
	public KeyValue() {	
	}
	
	public KeyValue(String key, String value) {
		this.key = key;
		this.value = value;
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
