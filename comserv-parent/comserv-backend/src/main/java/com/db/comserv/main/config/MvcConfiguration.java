/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */
package com.db.comserv.main.config;


import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.config.java.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.db.comserv.main.security.SecurityConfig;
import com.db.comserv.main.utilities.DataSettingProperties;
import com.db.comserv.main.utilities.Utils;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;



@EnableWebMvc
@Configuration
@ComponentScan(basePackages="com.db.comserv.main")
@Import({ SecurityConfig.class })
@EnableScheduling
public class MvcConfiguration extends WebMvcConfigurerAdapter implements SchedulingConfigurer {

	 @Autowired
	 private DataSettingProperties dbProperties;
	 
	@Bean
	public ViewResolver getViewResolver(){
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/pages/");
		resolver.setSuffix(".jsp");
		return resolver;
	}
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/**").addResourceLocations("/resources/**");
	}

	
   

	@Bean
	@Autowired
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(DataSettingProperties dsProperties) {
        PropertySourcesPlaceholderConfigurer propertiesConfig = new PropertySourcesPlaceholderConfigurer();
        propertiesConfig.setProperties(dsProperties.getAllProperties());
        return propertiesConfig;
    } 
	
	  

	 @Override
     public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
         taskRegistrar.setScheduler(taskScheduler());
     }

     @Bean(destroyMethod="shutdownNow")
     public Executor taskScheduler() {
         return Executors.newScheduledThreadPool(Integer.parseInt(dbProperties.getValue("minThreadPoolSize")), getThreadFactory("scheduled"));
     }
     
     
    @Bean(destroyMethod="shutdownNow")
 	public ExecutorService taskExecutor() {
    	int  maxPoolSize   =  Integer.parseInt(dbProperties.getValue("MaxThreadPoolSize"));
    	long keepAliveTime = Integer.parseInt(dbProperties.getValue("ThreadkeepLiveTime"));
    	ExecutorService threadPool =
    	        new ThreadPoolExecutor(
    	        		maxPoolSize,
    	                maxPoolSize,
    	                keepAliveTime,
    	                TimeUnit.MILLISECONDS,
    	                new LinkedBlockingQueue<Runnable>(), 
    	                getThreadFactory("executor")
    	                );
    	return threadPool;
 	}
    
	public static ThreadFactory getThreadFactory(final String namePrefix) {
		return new ThreadFactory() {
			private final ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory(); 
			
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = defaultThreadFactory.newThread(r);
				thread.setDaemon(true);
				thread.setName(namePrefix + "-" + thread.getName());
				return thread;
			}
		};
	}
   
}
