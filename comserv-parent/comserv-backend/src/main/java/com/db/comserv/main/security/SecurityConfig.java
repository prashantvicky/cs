/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */
package com.db.comserv.main.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import com.db.comserv.main.security.CustomAuthProvider;
import com.db.comserv.main.utilities.DataSettingProperties;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	DataSettingProperties properties;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		boolean isSecure = Boolean.parseBoolean(properties.getJSONData("secure"));
		if(isSecure){
			 http.authorizeRequests()
			 	.antMatchers("/v0/*").permitAll()
			 	.antMatchers("/v0/policy/*").permitAll()
			 	.and().csrf().disable().httpBasic()
			 	.and().exceptionHandling().accessDeniedPage("/401")
			 	;
		}
		else
		{
			 http.authorizeRequests()
			 	.antMatchers("/v0/*").permitAll()
			 	.antMatchers("/v0/policy/*").permitAll()
			 	.and().csrf().disable().httpBasic()
			 	.and().exceptionHandling().accessDeniedPage("/401")
			 	;
		}
	}
	
}


