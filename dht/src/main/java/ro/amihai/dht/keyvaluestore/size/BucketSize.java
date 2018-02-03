package ro.amihai.dht.keyvaluestore.size;

public class BucketSize {

	private int bucket;
	
	private long size;
	
	private long lastUpdate;

	public BucketSize() {
		
	}
	
	public BucketSize(int bucket, long size, long lastUpdate) {
		this.size = size;
		this.lastUpdate = lastUpdate;
	}
	
	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public int getBucket() {
		return bucket;
	}

	public void setBucket(int bucket) {
		this.bucket = bucket;
	}
	
	
}
