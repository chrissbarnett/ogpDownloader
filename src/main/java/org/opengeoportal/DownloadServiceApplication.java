package org.opengeoportal;

import org.opengeoportal.datastore.DataStoreQuery;
import org.opengeoportal.shapefile.ShapefileBuilder;
import org.opengeoportal.shapefile.ShapefileCreator;
import org.opengeoportal.solr.IdQuery;
import org.opengeoportal.util.compress.ShapeArchive;
import org.opengeoportal.xml.MetadataFileWriter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.annotation.EnableJms;


@Configuration
@ComponentScan
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@EnableJms
@EnableCaching
public class DownloadServiceApplication {

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public MetadataFileWriter getWriter(){
		return new MetadataFileWriter();
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public ShapeArchive getShapeArchive(){
		return new ShapeArchive();
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public IdQuery getIdQuery(){
		return new IdQuery();
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public ShapefileBuilder getShapefileBuilder(){return new ShapefileBuilder();}

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public ShapefileCreator getShapefileCreator(){return new ShapefileCreator();}

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public DataStoreQuery getDataStoreQuery(){
		return new DataStoreQuery();
	}

	public static void main(String[] args) {
		SpringApplication.run(DownloadServiceApplication.class, args);
	}
}
