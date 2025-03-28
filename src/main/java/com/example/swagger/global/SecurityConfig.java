package com.example.swagger.global;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
	@Bean
	public SecurityFilterChain baseSecurityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(authorizeRequests ->
				authorizeRequests
					.requestMatchers("/h2-console/**")
					.permitAll()
					.requestMatchers("/api/*/**")
					.authenticated()
					.anyRequest()
					.permitAll()
			)
			.headers(
				headers ->
					headers.frameOptions(
						frameOptions ->
							frameOptions.sameOrigin()
					)
			)
			.csrf(
				csrf ->
					csrf.disable()
			);

		return http.build();
	}
}
