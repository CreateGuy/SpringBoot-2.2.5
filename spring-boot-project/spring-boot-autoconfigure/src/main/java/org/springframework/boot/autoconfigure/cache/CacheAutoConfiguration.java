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

import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration.CacheConfigurationImportSelector;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration.CacheManagerEntityManagerFactoryDependsOnPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheAspectSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.orm.jpa.AbstractEntityManagerFactoryBean;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.util.Assert;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for the cache abstraction. Creates a
 * {@link CacheManager} if necessary when caching is enabled via
 * {@link EnableCaching @EnableCaching}.
 * <p>
 * Cache store can be auto-detected or specified explicitly via configuration.
 *
 * @author Stephane Nicoll
 * @since 1.3.0
 * @see EnableCaching
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(CacheManager.class)
@ConditionalOnBean(CacheAspectSupport.class)
@ConditionalOnMissingBean(value = CacheManager.class, name = "cacheResolver")
@EnableConfigurationProperties(CacheProperties.class)
@AutoConfigureAfter({ CouchbaseAutoConfiguration.class, HazelcastAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class, RedisAutoConfiguration.class })
@Import({ CacheConfigurationImportSelector.class, CacheManagerEntityManagerFactoryDependsOnPostProcessor.class })
public class CacheAutoConfiguration {

	/**
	 * 是操作 {@link CacheManager} 的回调对象
	 * @param customizers
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean
	public CacheManagerCustomizers cacheManagerCustomizers(ObjectProvider<CacheManagerCustomizer<?>> customizers) {
		return new CacheManagerCustomizers(customizers.orderedStream().collect(Collectors.toList()));
	}

	/**
	 * 用于验证 {@link CacheManager} 是否存在，并提供更有意义的异常
	 * @param cacheProperties
	 * @param cacheManager
	 * @return
	 */
	@Bean
	public CacheManagerValidator cacheAutoConfigurationValidator(CacheProperties cacheProperties,
			ObjectProvider<CacheManager> cacheManager) {
		return new CacheManagerValidator(cacheProperties, cacheManager);
	}

	/**
	 * 是一个 {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor BeanFactoryPostProcessor}，让某种类型的Bean依赖于名字叫cacheManager的Bean
	 */
	@ConditionalOnClass(LocalContainerEntityManagerFactoryBean.class)
	@ConditionalOnBean(AbstractEntityManagerFactoryBean.class)
	static class CacheManagerEntityManagerFactoryDependsOnPostProcessor
			extends EntityManagerFactoryDependsOnPostProcessor {

		CacheManagerEntityManagerFactoryDependsOnPostProcessor() {
			super("cacheManager");
		}

	}

	/**
	 * 用于验证 {@link CacheManager} 是否存在，并提供更有意义的异常
	 */
	static class CacheManagerValidator implements InitializingBean {

		private final CacheProperties cacheProperties;

		private final ObjectProvider<CacheManager> cacheManager;

		CacheManagerValidator(CacheProperties cacheProperties, ObjectProvider<CacheManager> cacheManager) {
			this.cacheProperties = cacheProperties;
			this.cacheManager = cacheManager;
		}

		@Override
		public void afterPropertiesSet() {
			Assert.notNull(this.cacheManager.getIfAvailable(),
					() -> "No cache manager could be auto-configured, check your configuration (caching " + "type is '"
							+ this.cacheProperties.getType() + "')");
		}

	}

	/**
	 * 负责将SpringCache支持的缓存类型的自动配置类加入到容器中,如下所示
	 * <ul>
	 *     <li>0 = "org.springframework.boot.autoconfigure.cache.GenericCacheConfiguration"</li>
	 *     <li>1 = "org.springframework.boot.autoconfigure.cache.JCacheCacheConfiguration"</li>
	 *     <li>2 = "org.springframework.boot.autoconfigure.cache.EhCacheCacheConfiguration"</li>
	 *     <li>3 = "org.springframework.boot.autoconfigure.cache.HazelcastCacheConfiguration"</li>
	 *     <li>4 = "org.springframework.boot.autoconfigure.cache.InfinispanCacheConfiguration"</li>
	 *     <li>5 = "org.springframework.boot.autoconfigure.cache.CouchbaseCacheConfiguration"</li>
	 *     <li>6 = "org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration"</li>
	 *     <li>7 = "org.springframework.boot.autoconfigure.cache.CaffeineCacheConfiguration"</li>
	 *     <li>8 = "org.springframework.boot.autoconfigure.cache.SimpleCacheConfiguration"</li>
	 *     <li>9 = "org.springframework.boot.autoconfigure.cache.NoOpCacheConfiguration"</li>
	 * </ul>
	 */
	static class CacheConfigurationImportSelector implements ImportSelector {

		@Override
		public String[] selectImports(AnnotationMetadata importingClassMetadata) {
			CacheType[] types = CacheType.values();
			String[] imports = new String[types.length];
			for (int i = 0; i < types.length; i++) {
				imports[i] = CacheConfigurations.getConfigurationClass(types[i]);
			}
			return imports;
		}

	}

}
