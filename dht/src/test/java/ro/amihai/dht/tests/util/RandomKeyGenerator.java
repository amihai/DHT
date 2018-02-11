package ro.amihai.dht.tests.util;

import static java.util.Collections.shuffle;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component
public class RandomKeyGenerator {

	private List<String> allowedCharacters;
	
	@PostConstruct
	private void init() {
		allowedCharacters = Stream.of(
				rangeClosed('a', 'z').boxed(), 
				rangeClosed('A', 'Z').boxed())
		.flatMap(identity())
		.map(this::charToString)
		.collect(toList());
		
		allowedCharacters.add("-");
		allowedCharacters.add("_");
		
		shuffle(allowedCharacters);
	}
	
	public String next(int size) {
		shuffle(allowedCharacters);
		
		return allowedCharacters.stream()
			.limit(size)
			.collect(Collectors.joining());
	}
	
	String charToString(Integer charCode) {
		return Character.toString((char)charCode.intValue());
	}
}
