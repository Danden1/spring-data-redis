/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.redis.core;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import org.reactivestreams.Publisher;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisStreamCommands.Consumer;
import org.springframework.data.redis.connection.RedisStreamCommands.StreamMessage;
import org.springframework.data.redis.connection.RedisStreamCommands.StreamOffset;
import org.springframework.data.redis.connection.RedisStreamCommands.StreamReadOptions;
import org.springframework.data.redis.connection.RedisZSetCommands.Limit;

/**
 * Redis stream specific operations.
 *
 * @author Mark Paluch
 * @since 2.2
 */
public interface ReactiveStreamOperations<K, V> {

	/**
	 * Acknowledge one or more messages as processed.
	 *
	 * @param key the stream key.
	 * @param group name of the consumer group.
	 * @param messageIds message Id's to acknowledge.
	 * @return length of acknowledged messages.
	 * @see <a href="http://redis.io/commands/xack">Redis Documentation: XACK</a>
	 */
	Mono<Long> acknowledge(K key, String group, String... messageIds);

	/**
	 * Append one or more message to the stream {@code key}.
	 *
	 * @param key the stream key.
	 * @param bodyPublisher message body {@link Publisher}.
	 * @return the message Ids.
	 * @see <a href="http://redis.io/commands/xadd">Redis Documentation: XADD</a>
	 */
	default Flux<String> add(K key, Publisher<? extends Map<K, V>> bodyPublisher) {
		return Flux.from(bodyPublisher).flatMap(it -> add(key, it));
	}

	/**
	 * Append a message to the stream {@code key}.
	 *
	 * @param key the stream key.
	 * @param body message body.
	 * @return the message Id.
	 * @see <a href="http://redis.io/commands/xadd">Redis Documentation: XADD</a>
	 */
	Mono<String> add(K key, Map<K, V> body);

	/**
	 * Removes the specified entries from the stream. Returns the number of items deleted, that may be different from the
	 * number of IDs passed in case certain IDs do not exist.
	 *
	 * @param key the stream key.
	 * @param messageIds stream message Id's.
	 * @return number of removed entries.
	 * @see <a href="http://redis.io/commands/xdel">Redis Documentation: XDEL</a>
	 */
	Mono<Long> delete(K key, String... messageIds);

	/**
	 * Get the length of a stream.
	 *
	 * @param key the stream key.
	 * @return length of the stream.
	 * @see <a href="http://redis.io/commands/xlen">Redis Documentation: XLEN</a>
	 */
	Mono<Long> size(K key);

	/**
	 * Read messages from a stream within a specific {@link Range}.
	 *
	 * @param key the stream key.
	 * @param range must not be {@literal null}.
	 * @return list with members of the resulting stream.
	 * @see <a href="http://redis.io/commands/xrange">Redis Documentation: XRANGE</a>
	 */
	default Flux<StreamMessage<K, V>> range(K key, Range<String> range) {
		return range(key, range, Limit.unlimited());
	}

	/**
	 * Read messages from a stream within a specific {@link Range} applying a {@link Limit}.
	 *
	 * @param key the stream key.
	 * @param range must not be {@literal null}.
	 * @param limit must not be {@literal null}.
	 * @return list with members of the resulting stream.
	 * @see <a href="http://redis.io/commands/xrange">Redis Documentation: XRANGE</a>
	 */
	Flux<StreamMessage<K, V>> range(K key, Range<String> range, Limit limit);

	/**
	 * Read messages from one or more {@link StreamOffset}s.
	 *
	 * @param stream the streams to read from.
	 * @return list with members of the resulting stream.
	 * @see <a href="http://redis.io/commands/xread">Redis Documentation: XREAD</a>
	 */
	default Flux<StreamMessage<K, V>> read(StreamOffset<K> stream) {
		return read(StreamReadOptions.empty(), new StreamOffset[] { stream });
	}

	/**
	 * Read messages from one or more {@link StreamOffset}s.
	 *
	 * @param streams the streams to read from.
	 * @return list with members of the resulting stream.
	 * @see <a href="http://redis.io/commands/xread">Redis Documentation: XREAD</a>
	 */
	default Flux<StreamMessage<K, V>> read(StreamOffset<K>... streams) {
		return read(StreamReadOptions.empty(), streams);
	}

	/**
	 * Read messages from one or more {@link StreamOffset}s.
	 *
	 * @param readOptions read arguments.
	 * @param stream the streams to read from.
	 * @return list with members of the resulting stream.
	 * @see <a href="http://redis.io/commands/xread">Redis Documentation: XREAD</a>
	 */
	default Flux<StreamMessage<K, V>> read(StreamReadOptions readOptions, StreamOffset<K> stream) {
		return read(readOptions, new StreamOffset[] { stream });
	}

	/**
	 * Read messages from one or more {@link StreamOffset}s.
	 *
	 * @param readOptions read arguments.
	 * @param streams the streams to read from.
	 * @return list with members of the resulting stream.
	 * @see <a href="http://redis.io/commands/xread">Redis Documentation: XREAD</a>
	 */
	Flux<StreamMessage<K, V>> read(StreamReadOptions readOptions, StreamOffset<K>... streams);

	/**
	 * Read messages from one or more {@link StreamOffset}s using a consumer group.
	 *
	 * @param consumer consumer/group.
	 * @param stream the streams to read from.
	 * @return list with members of the resulting stream.
	 * @see <a href="http://redis.io/commands/xreadgroup">Redis Documentation: XREADGROUP</a>
	 */
	default Flux<StreamMessage<K, V>> read(Consumer consumer, StreamOffset<K> stream) {
		return read(consumer, StreamReadOptions.empty(), new StreamOffset[] { stream });
	}

	/**
	 * Read messages from one or more {@link StreamOffset}s using a consumer group.
	 *
	 * @param consumer consumer/group.
	 * @param streams the streams to read from.
	 * @return list with members of the resulting stream.
	 * @see <a href="http://redis.io/commands/xreadgroup">Redis Documentation: XREADGROUP</a>
	 */
	default Flux<StreamMessage<K, V>> read(Consumer consumer, StreamOffset<K>... streams) {
		return read(consumer, StreamReadOptions.empty(), streams);
	}

	/**
	 * Read messages from one or more {@link StreamOffset}s using a consumer group.
	 *
	 * @param consumer consumer/group.
	 * @param readOptions read arguments.
	 * @param stream the streams to read from.
	 * @return list with members of the resulting stream.
	 * @see <a href="http://redis.io/commands/xreadgroup">Redis Documentation: XREADGROUP</a>
	 */
	default Flux<StreamMessage<K, V>> read(Consumer consumer, StreamReadOptions readOptions, StreamOffset<K> stream) {
		return read(consumer, readOptions, new StreamOffset[] { stream });
	}

	/**
	 * Read messages from one or more {@link StreamOffset}s using a consumer group.
	 *
	 * @param consumer consumer/group.
	 * @param readOptions read arguments.
	 * @param streams the streams to read from.
	 * @return list with members of the resulting stream.
	 * @see <a href="http://redis.io/commands/xreadgroup">Redis Documentation: XREADGROUP</a>
	 */
	Flux<StreamMessage<K, V>> read(Consumer consumer, StreamReadOptions readOptions, StreamOffset<K>... streams);

	/**
	 * Read messages from a stream within a specific {@link Range} in reverse order.
	 *
	 * @param key the stream key.
	 * @param range must not be {@literal null}.
	 * @return list with members of the resulting stream.
	 * @see <a href="http://redis.io/commands/xrevrange">Redis Documentation: XREVRANGE</a>
	 */
	default Flux<StreamMessage<K, V>> reverseRange(K key, Range<String> range) {
		return reverseRange(key, range, Limit.unlimited());
	}

	/**
	 * Read messages from a stream within a specific {@link Range} applying a {@link Limit} in reverse order.
	 *
	 * @param key the stream key.
	 * @param range must not be {@literal null}.
	 * @param limit must not be {@literal null}.
	 * @return list with members of the resulting stream.
	 * @see <a href="http://redis.io/commands/xrevrange">Redis Documentation: XREVRANGE</a>
	 */
	Flux<StreamMessage<K, V>> reverseRange(K key, Range<String> range, Limit limit);

	/**
	 * Trims the stream to {@code count} elements.
	 *
	 * @param key the stream key.
	 * @param count length of the stream.
	 * @return number of removed entries.
	 * @see <a href="http://redis.io/commands/xtrim">Redis Documentation: XTRIM</a>
	 */
	Mono<Long> trim(K key, long count);
}