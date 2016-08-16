/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */
package com.db.comserv.main.config;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.db.comserv.main.utilities.DataSettingProperties;

@EnableWebMvc
@Configuration
@ComponentScan(basePackages="com.db.comserv.main")
public class MvcConfiguration extends WebMvcConfigurerAdapter implements InitializingBean{
	@Autowired
	ServletContext servletContext;
	@Autowired
	DataSettingProperties dsProps;
	
	@Bean
	public ViewResolver getViewResolver(){
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/views/");
		resolver.setSuffix(".jsp");
		return resolver;
	}
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/**")
			.addResourceLocations("/resources/")
			.setCachePeriod(3600)
			.resourceChain(true)
			.addResolver(new PathResourceResolver());
		
		registry.addResourceHandler("/robots.txt")
			.addResourceLocations("/resources/robots.txt")
			.setCachePeriod(3600)
			.resourceChain(true)
			.addResolver(new PathResourceResolver());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (dsProps.getValue("cookie.domain") != null) {
			servletContext.getSessionCookieConfig().setDomain(dsProps.getValue("cookie.domain"));
		}
	}
}
