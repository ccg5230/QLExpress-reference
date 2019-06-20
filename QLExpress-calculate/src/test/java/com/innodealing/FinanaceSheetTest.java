package com.innodealing;

import java.util.List;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.innodealing.config.DatabaseNameConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BondCalculateApplication.class)
@WebIntegrationTest
public class FinanaceSheetTest {
	
	private  @Autowired  JdbcTemplate jdbcTemplate;
	
	private @Autowired DatabaseNameConfig config;
	
	public void indicatorGroup(){
	}

}
