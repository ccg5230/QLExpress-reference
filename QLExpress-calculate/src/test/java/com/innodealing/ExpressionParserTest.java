package com.innodealing;



import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ql.util.express.ExpressRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BondCalculateApplication.class)
@WebIntegrationTest
public class ExpressionParserTest {
	
	@Autowired private JdbcTemplate jdbcTemplate;

	@Test
	public void creditExpressionTest() {
		String createVarTableSql = 
				"CREATE TABLE dmdb.`t_bond_finance_sheet_indicator_expression_vars` (\r\n" + 
				"	`expression_id` BIGINT(20) NULL DEFAULT NULL,\r\n" + 
				"	`express_format` VARCHAR(2048) NULL DEFAULT NULL COLLATE 'utf8_bin',\r\n" + 
				"	`var` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8_bin',\r\n" + 
				"	INDEX `id` (`expression_id`)\r\n" + 
				")\r\n" + 
				"COLLATE='utf8_bin'\r\n" + 
				"ENGINE=InnoDB;";
				
		String dropVarTableSql = "DROP TABLE IF EXISTS dmdb.t_bond_finance_sheet_indicator_expression_vars";
		
		jdbcTemplate.execute(dropVarTableSql);
		jdbcTemplate.execute(createVarTableSql);
		
		ExpressRunner runner = new ExpressRunner();
		String sql = "select id, express_format from \r\n" + 
				"dmdb.t_bond_finance_sheet_indicator_expression ";
		try {
			List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
			for(Map<String, Object> p : result) {
				System.out.println("-----------------------");
				Long expressionId = (Long)p.get("id") ;
				String expressFormat = (String)p.get("express_format") ;
				System.out.println("expressionId:" + expressionId + ", expressFormat:" + expressFormat);
				try {
					final String insertVarTable = "INSERT INTO dmdb.t_bond_finance_sheet_indicator_expression_vars " +
							"(expression_id, express_format, var) VALUES (?, ?, ?)";
					String[] vars = runner.getOutVarNames(expressFormat);
					for(int i = 0; i < vars.length; ++i) {
						System.out.println(vars[i]);
						jdbcTemplate.update(insertVarTable, new Object[] { expressionId,
								expressFormat, vars[i]
							});
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
