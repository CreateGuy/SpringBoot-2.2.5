/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.cache;

/**
 * SpringCache支持的缓存存储位置，越在上面优先级越高
 * @since 1.3.0
 */
public enum CacheType {

	/**
	 * 使用容器中的Cache Bean
	 */
	GENERIC,

	/**
	 * JCache (JSR-107) backed caching.
	 */
	JCACHE,

	/**
	 * EhCache backed caching.
	 */
	EHCACHE,

	/**
	 * Hazelcast backed caching.
	 */
	HAZELCAST,

	/**
	 * Infinispan backed caching.
	 */
	INFINISPAN,

	/**
	 * Couchbase backed caching.
	 */
	COUCHBASE,

	/**
	 * Redis支持缓存
	 */
	REDIS,

	/**
	 * Caffeine backed caching.
	 */
	CAFFEINE,

	/**
	 * Simple in-memory caching.
	 */
	SIMPLE,

	/**
	 * No caching.
	 */
	NONE

}
