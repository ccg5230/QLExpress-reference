package com.innodealing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.innodealing.config.AppConfig;
import com.innodealing.config.DatabaseNameConfig;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@RestController
@EnableSwagger2
@EnableConfigurationProperties({DatabaseNameConfig.class,AppConfig.class})
public class BondCalculateApplication {

	@RequestMapping(value = {"/",""}, method = RequestMethod.GET)
	public ModelAndView  swagger(Model model){
		return new ModelAndView(new RedirectView("swagger-ui.html"));
	}
	
	public static void main(String[] args) {
		new BondCalculateApplication();
		SpringApplication.run(BondCalculateApplication.class, args);
	}
	
}


