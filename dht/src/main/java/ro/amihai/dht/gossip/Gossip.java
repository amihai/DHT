package ro.amihai.dht.gossip;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import ro.amihai.dht.node.NodeAddress;
import ro.amihai.dht.service.keyvaluestore.KeyValue;

public class Gossip {

	@NotNull
	private UUID uuid;
	
	@NotNull
	private GossipAction action;
	
	private KeyValue keyValue;

	private Map<Integer, Set<NodeAddress>> bucketsToNodes;
	
	
	public Gossip() {
		
	}
	
	@Override
	public String toString() {
		return "Gossip [uuid=" + uuid + ", action=" + action + ", keyValue=" + keyValue + ", bucketsToNodes="
				+ bucketsToNodes + "]";
	}

	public Gossip(GossipAction action, KeyValue keyValue) {
		this.uuid = UUID.randomUUID();
		this.action = action;
		this.keyValue = keyValue;
	}
	
	public Gossip(GossipAction action, Map<Integer, Set<NodeAddress>> bucketsToNodes) {
		this.uuid = UUID.randomUUID();
		this.action = action;
		this.bucketsToNodes = bucketsToNodes;
	}
	
	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public GossipAction getAction() {
		return action;
	}

	public void setAction(GossipAction type) {
		this.action = type;
	}

	public KeyValue getKeyValue() {
		return keyValue;
	}

	public void setKeyValue(KeyValue keyValue) {
		this.keyValue = keyValue;
	}

	public Map<Integer, Set<NodeAddress>> getBucketsToNodes() {
		return bucketsToNodes;
	}

	public void setBucketsToNodes(Map<Integer, Set<NodeAddress>> bucketsToNodes) {
		this.bucketsToNodes = bucketsToNodes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
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
		Gossip other = (Gossip) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}
	
	
	
}
