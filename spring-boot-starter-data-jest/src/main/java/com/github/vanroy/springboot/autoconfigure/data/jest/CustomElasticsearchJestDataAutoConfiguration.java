/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vanroy.springboot.autoconfigure.data.jest;

import java.lang.reflect.Field;
import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.DefaultEntityMapper;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.vanroy.springdata.jest.JestElasticsearchTemplate;
import com.github.vanroy.springdata.jest.mapper.DefaultJestResultsMapper;
import com.github.vanroy.springdata.jest.mapper.JestResultsMapper;

import io.searchbox.client.JestClient;
import lombok.NonNull;

/**
 * Custom {@link ElasticsearchJestDataAutoConfiguration}
 */
@Configuration
@AutoConfigureBefore(ElasticsearchJestDataAutoConfiguration.class)
@AutoConfigureAfter(ElasticsearchJestAutoConfiguration.class)
public class CustomElasticsearchJestDataAutoConfiguration extends ElasticsearchJestDataAutoConfiguration {
	
	/**
	 * {@link ObjectMapper}
	 */
	private final ObjectMapper objectMapper;
	
	/**
	 * Constructor
	 * 
	 * @param objectMapperProvider {@link ObjectProvider}: {@link ObjectMapper}
	 * @param customizerProvider {@link ObjectProvider}: {@link JestObjectMapperCustomizer}
	 */
	public CustomElasticsearchJestDataAutoConfiguration(
	/* @formatter:off */
		@NonNull ObjectProvider<ObjectMapper> objectMapperProvider,
		@NonNull ObjectProvider<JestObjectMapperCustomizer> customizerProvider) {
		/* @formatter:on */
		
		ObjectMapper objectMapper = Optional.ofNullable(objectMapperProvider.getIfAvailable())
		/* @formatter:off */
			.map(mapper -> mapper.copy())
			.orElseGet(() -> null);
			/* @formatter:on */
		
		if (objectMapper != null) {
			
			Optional.ofNullable(customizerProvider.getIfAvailable())
			/* @formatter:off */
				.ifPresent(customizer -> customizer.customize(objectMapper));
				/* @formatter:on */
		}
		
		this.objectMapper = objectMapper;
	}
	
	@Bean
	@ConditionalOnMissingBean
	@Override
	public ElasticsearchOperations elasticsearchTemplate(JestClient client) {
		
		ElasticsearchConverter elasticsearchConverter = new MappingElasticsearchConverter(
			new SimpleElasticsearchMappingContext());
		
		JestResultsMapper resultsMapper = new DefaultJestResultsMapper(elasticsearchConverter.getMappingContext(),
			new CustomDefaultEntityMapper(this.objectMapper));
		
		return new JestElasticsearchTemplate(client, elasticsearchConverter, resultsMapper);
	}
	
	/**
	 * Jest {@link ObjectMapper} customizer
	 */
	public interface JestObjectMapperCustomizer {
		
		/**
		 * Customize
		 * 
		 * @param objectMapper {@link ObjectMapper}
		 */
		void customize(ObjectMapper objectMapper);
	}
	
	/**
	 * Custom {@link DefaultEntityMapper}
	 */
	public static class CustomDefaultEntityMapper extends DefaultEntityMapper {
		
		/**
		 * Constructor
		 * 
		 * @param objectMapper {@link ObjectMapper}
		 */
		public CustomDefaultEntityMapper(ObjectMapper objectMapper) {
			
			super();
			
			if (objectMapper != null) {
				
				Field field = ReflectionUtils.findField(DefaultEntityMapper.class, "objectMapper");
				
				Assert.notNull(field, "Field 'objectMapper' is null");
				
				ReflectionUtils.makeAccessible(field);
				ReflectionUtils.setField(field, this, objectMapper);
			}
		}
	}
}
