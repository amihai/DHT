package ro.amihai.dht.keyvaluestore.size;

public class BucketSize {

	private int bucket;
	
	private long size;
	
	private long lastUpdate;

	public BucketSize() {
		
	}
	
	public BucketSize(int bucket, long size, long lastUpdate) {
		this.bucket = bucket;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bucket;
		result = prime * result + (int) (lastUpdate ^ (lastUpdate >>> 32));
		result = prime * result + (int) (size ^ (size >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BucketSize other = (BucketSize) obj;
		if (bucket != other.bucket)
			return false;
		if (lastUpdate != other.lastUpdate)
			return false;
		if (size != other.size)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BucketSize [bucket=" + bucket + ", size=" + size + ", lastUpdate=" + lastUpdate + "]";
	}
	
	
}
