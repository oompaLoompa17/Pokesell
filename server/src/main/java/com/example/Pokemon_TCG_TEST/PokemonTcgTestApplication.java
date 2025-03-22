package com.example.Pokemon_TCG_TEST;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PokemonTcgTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(PokemonTcgTestApplication.class, args);
	}

}
