package ro.amihai.dht.bucketstonodes.observer;

import java.util.Observer;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ro.amihai.dht.bucketstonodes.BucketsToNodes;

public abstract class BucketsToNodesObserver implements Observer {

	private Logger logger = LoggerFactory.getLogger(BucketsInCurrentNode.class);
	
	@Autowired
	protected BucketsToNodes bucketsToNodes;
	
	@PostConstruct
	protected void init() {
		logger.debug("Initialize {} Observer", getClass());
		update(null, null);
		bucketsToNodes.addObserver(this);
	}

}
