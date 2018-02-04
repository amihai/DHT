package ro.amihai.dht.tests.util;

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
				IntStream.rangeClosed('a', 'z').boxed(), 
				IntStream.rangeClosed('A', 'Z').boxed())
		.flatMap(Function.identity())
		.map(i -> Character.toString((char)i.intValue()))
		.collect(Collectors.toList());
		
		allowedCharacters.add("-");
		allowedCharacters.add("_");
		
		Collections.shuffle(allowedCharacters);
	}
	
	public String next(int size) {
		Collections.shuffle(allowedCharacters);
		
		return allowedCharacters.stream()
			.limit(size)
			.collect(Collectors.joining());
	}
}
