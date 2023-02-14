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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * SpringCache 的配置文件
 * @since 1.3.0
 */
@ConfigurationProperties(prefix = "spring.cache")
public class CacheProperties {

	/**
	 * 缓存类型。默认情况下，根据环境自动检测
	 */
	private CacheType type;

	/**
	 * 缓存的名称
	 * <ul>
	 *     <li>没太懂具体的应用场景</li>
	 *     <li>像 {@link SimpleCacheConfiguration} 只是将这些缓存名称加上一个特殊的值保存在内存中，难道只是先创建一个位置吗</li>
	 * </ul>
	 */
	private List<String> cacheNames = new ArrayList<>();

	/**
	 * 下面这几个都是各大缓存/数据库的配置文件
	 */
	private final Caffeine caffeine = new Caffeine();

	private final Couchbase couchbase = new Couchbase();

	private final EhCache ehcache = new EhCache();

	private final Infinispan infinispan = new Infinispan();

	private final JCache jcache = new JCache();

	private final Redis redis = new Redis();

	public CacheType getType() {
		return this.type;
	}

	public void setType(CacheType mode) {
		this.type = mode;
	}

	public List<String> getCacheNames() {
		return this.cacheNames;
	}

	public void setCacheNames(List<String> cacheNames) {
		this.cacheNames = cacheNames;
	}

	public Caffeine getCaffeine() {
		return this.caffeine;
	}

	public Couchbase getCouchbase() {
		return this.couchbase;
	}

	public EhCache getEhcache() {
		return this.ehcache;
	}

	public Infinispan getInfinispan() {
		return this.infinispan;
	}

	public JCache getJcache() {
		return this.jcache;
	}

	public Redis getRedis() {
		return this.redis;
	}

	/**
	 * Resolve the config location if set.
	 * @param config the config resource
	 * @return the location or {@code null} if it is not set
	 * @throws IllegalArgumentException if the config attribute is set to an unknown
	 * location
	 */
	public Resource resolveConfigLocation(Resource config) {
		if (config != null) {
			Assert.isTrue(config.exists(),
					() -> "Cache configuration does not exist '" + config.getDescription() + "'");
			return config;
		}
		return null;
	}

	/**
	 * Caffeine：是基于 Java 8 的高性能，接近最佳的缓存库
	 */
	public static class Caffeine {

		/**
		 * The spec to use to create caches. See CaffeineSpec for more details on the spec
		 * format.
		 */
		private String spec;

		public String getSpec() {
			return this.spec;
		}

		public void setSpec(String spec) {
			this.spec = spec;
		}

	}

	/**
	 * CouchBase：是一款开源的、分布式的、面向文档的NoSQL数据库
	 */
	public static class Couchbase {

		/**
		 * Entry expiration. By default the entries never expire. Note that this value is
		 * ultimately converted to seconds.
		 */
		private Duration expiration;

		public Duration getExpiration() {
			return this.expiration;
		}

		public void setExpiration(Duration expiration) {
			this.expiration = expiration;
		}

	}

	/**
	 * EhCache：纯Java的进程内缓存框架
	 */
	public static class EhCache {

		/**
		 * The location of the configuration file to use to initialize EhCache.
		 */
		private Resource config;

		public Resource getConfig() {
			return this.config;
		}

		public void setConfig(Resource config) {
			this.config = config;
		}

	}

	/**
	 * Infinispan specific cache properties.
	 */
	public static class Infinispan {

		/**
		 * The location of the configuration file to use to initialize Infinispan.
		 */
		private Resource config;

		public Resource getConfig() {
			return this.config;
		}

		public void setConfig(Resource config) {
			this.config = config;
		}

	}

	/**
	 * JCache (JSR-107)：一种对Java对象临时在内存中进行缓存的方法
	 */
	public static class JCache {

		/**
		 * The location of the configuration file to use to initialize the cache manager.
		 * The configuration file is dependent of the underlying cache implementation.
		 */
		private Resource config;

		/**
		 * Fully qualified name of the CachingProvider implementation to use to retrieve
		 * the JSR-107 compliant cache manager. Needed only if more than one JSR-107
		 * implementation is available on the classpath.
		 */
		private String provider;

		public String getProvider() {
			return this.provider;
		}

		public void setProvider(String provider) {
			this.provider = provider;
		}

		public Resource getConfig() {
			return this.config;
		}

		public void setConfig(Resource config) {
			this.config = config;
		}

	}

	/**
	 * Redis缓存配置
	 */
	public static class Redis {

		/**
		 * 缓存的过期时间(剩余活跃时间)
		 */
		private Duration timeToLive;

		/**
		 * 是否允许空值
		 */
		private boolean cacheNullValues = true;

		/**
		 * 键前缀
		 */
		private String keyPrefix;

		/**
		 * 写入Redis时是否使用key前缀
		 */
		private boolean useKeyPrefix = true;

		public Duration getTimeToLive() {
			return this.timeToLive;
		}

		public void setTimeToLive(Duration timeToLive) {
			this.timeToLive = timeToLive;
		}

		public boolean isCacheNullValues() {
			return this.cacheNullValues;
		}

		public void setCacheNullValues(boolean cacheNullValues) {
			this.cacheNullValues = cacheNullValues;
		}

		public String getKeyPrefix() {
			return this.keyPrefix;
		}

		public void setKeyPrefix(String keyPrefix) {
			this.keyPrefix = keyPrefix;
		}

		public boolean isUseKeyPrefix() {
			return this.useKeyPrefix;
		}

		public void setUseKeyPrefix(boolean useKeyPrefix) {
			this.useKeyPrefix = useKeyPrefix;
		}

	}

}
