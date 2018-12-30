# Spring Data Jest Plus

[![circleci](https://img.shields.io/badge/circleci-spring--data--jest--plus-brightgreen.svg)](https://circleci.com/gh/spt-oss/spring-data-jest-plus)
[![maven central](https://img.shields.io/badge/maven_central-spring--data--jest--plus-blue.svg)](https://mvnrepository.com/artifact/com.github.spt-oss/spring-data-jest-plus)
[![javadoc](https://img.shields.io/badge/javadoc-spring--data--jest--plus-blue.svg)](https://www.javadoc.io/doc/com.github.spt-oss/spring-data-jest-plus)

* Custom implementation for [Spring Data Jest](https://github.com/VanRoy/spring-data-jest)
* **Note: This project is unofficial and experimental.**

## Dependencies

* com.github.spt-oss:spring-boot-starter-data-jest:**3.1.5.0**
	* com.github.vanroy:spring-boot-starter-data-jest:**3.1.5.RELEASE**
	* vc.inreach.aws:aws-signing-request-interceptor:**0.0.22**
	* org.elasticsearch:elasticsearch:**5.6.12**
	* org.springframework.boot:spring-boot-starter-json:**2.0.6.RELEASE**
	* org.springframework.cloud:spring-cloud-aws-autoconfigure:**2.0.1.RELEASE**
	* org.springframework.data:spring-data-elasticsearch:**3.0.11.RELEASE**
	* ......
* com.github.spt-oss:spring-boot-starter-data-jest:**3.1.5.1**
	* org.elasticsearch:elasticsearch:**5.6.14**
	* org.springframework.boot:spring-boot-starter-json:**2.0.7.RELEASE**
	* org.springframework.data:spring-data-elasticsearch:**3.0.12.RELEASE**
	* ......

## Usage

### Use with Spring Boot and Amazon Elasticsearch Service

1. Add a dependency in your project.

	```xml
	<dependency>
	    <groupId>com.github.spt-oss</groupId>
	    <artifactId>spring-boot-starter-data-jest</artifactId>
	    <version>3.1.5.1</version>
	</dependency>
	```

1. Add AWS settings to your application properties.

	```yaml
	spring.data.jest:
	    uri: https://search-XXXXX-XXXXX.us-west-1.es.amazonaws.com/
	
	cloud.aws:
	    credentials:
	        accessKey: ABCDEFGHIJKLMNOPQRST
	        secretKey: ABc1d2EfGhIjkLMnOpqRS3+tuVwXYzabCDeFGh4i
	    region:
	        auto: false
	        static: us-west-1
	```

1. Setup `Document` and `Repository` for your project.

	```java
	import org.springframework.data.annotation.Id;
	import org.springframework.data.elasticsearch.annotations.Document;
	import org.springframework.data.elasticsearch.annotations.Mapping;
	import org.springframework.data.elasticsearch.annotations.Setting;
	
	import lombok.Data;
	
	@Document(indexName = "foo", type = "product", createIndex = false)
	@Setting(settingPath = "/path/to/settings.json")
	@Mapping(mappingPath = "/path/to/mappings/product.json")
	@Data
	public class Product {
	    
	    @Id
	    private String id;
	    
	    private String content;
	}
	```
	```java
	import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
	import org.springframework.stereotype.Repository;
	
	@Repository
	public interface ProductRepository extends ElasticsearchRepository<Product, String> {
	}
	```

1. Setup Elasticsearch `Configuration`.

	```java
	import org.springframework.context.annotation.Bean;
	import org.springframework.context.annotation.Configuration;
	import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
	import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
	
	import com.fasterxml.jackson.databind.PropertyNamingStrategy;
	import com.github.vanroy.springboot.autoconfigure.data.jest.CustomElasticsearchJestDataAutoConfiguration.JestObjectMapperCustomizer;
	
	@Configuration
	@EnableElasticsearchRepositories(basePackageClasses = ProductRepository.class)
	public class MyElasticsearchConfiguration {
	    
	    @Bean
	    public JestObjectMapperCustomizer jestObjectMapperCustomizer() {
	        
	        return mapper -> mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
	    }
	}
	```

1. Run the application.

## License

* This software is released under the Apache License 2.0.
