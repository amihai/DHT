package ro.amihai.dht.tests.util;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RandomKeySpliterator implements Spliterator<String> {

	@Autowired
	private RandomKeyGenerator randomKeyGenerator;

	@Override
	public boolean tryAdvance(Consumer<? super String> action) {
		Objects.requireNonNull(action);
		action.accept(randomKeyGenerator.next(64));
		return true; 
	}

	@Override
	public Spliterator<String> trySplit() {
		return this;
	}

	@Override
	public long estimateSize() {
		return Long.MAX_VALUE;
	}

	@Override
	public int characteristics() {
		return Spliterator.IMMUTABLE | Spliterator.NONNULL;
	}
	

}
