package ro.amihai.dht.gossip;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GossipService {

	private Logger logger = LoggerFactory.getLogger(GossipService.class);
	
	
	@Autowired
	private GossipRegistry gossipRegistry;
	
	@Autowired
	private GossipTranslater gossipTranslater;
	
	@RequestMapping(method={RequestMethod.PUT},value={"/gossip"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Gossip> put(@Valid @RequestBody Gossip gossip) {
		
		if ( gossipRegistry.registerGossip(gossip) ) {
			logger.info("Start to apply the gossip on current node:", gossip);
			gossipTranslater.applyGossipToCurrentNode(gossip);
		} else {
			logger.info("Gossip already processed in the past:", gossip);
		}
		
		return ResponseEntity.ok(gossip);
	}
}
