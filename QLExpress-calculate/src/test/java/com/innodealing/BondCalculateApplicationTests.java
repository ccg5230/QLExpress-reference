package com.innodealing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.innodealing.constant.CommonConstant;
import com.innodealing.constant.ResponseData;
import com.innodealing.dao.IndicatorDao;
import com.innodealing.engine.OriginalData;
import com.innodealing.engine.innodealing.CalculateDm;
import com.innodealing.engine.innodealing.CalculateDm.Execute;
import com.innodealing.engine.innodealing.DateConvertUtil;
import com.innodealing.service.CreditRatingCalculateService;
import com.innodealing.service.FinanceIndicatorService;
import com.innodealing.util.DateUtils;
import com.innodealing.vo.ExpressionVo;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BondCalculateApplication.class)
@WebIntegrationTest
public class BondCalculateApplicationTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	
	@Autowired private FinanceIndicatorService indicatorService;
	
	@Autowired private CreditRatingCalculateService creditRatingCalculateService;

	@Test
	public void contextLoads() {
		System.out.println(jdbcTemplate.queryForList("select * from bank_fina_sheet"));
	}

	@Test
	public void initFincaceIndicatorTable() {
		// 指标和指标对应的计算公式
		Map<String, String> indicatorMap = new HashMap<>();
		indicatorMap.put("bank_ratio1", "BBS001/10000");
		indicatorMap.put("bank_ratio2", "BBS003/10000");
		indicatorMap.put("bank_ratio3", "BBS207/10000");
		indicatorMap.put("bank_ratio4", "BTN147/10000");

		// 主体指标的原始数据
//		Long compId = 1046L;
//		String sqlQuery = "select * from manu_fina_sheet where comp_id = ?";
//		String sqlInsert = "insert into dm_analysis_bank ";
//		List<Map<String, Object>> list = jdbcTemplate.queryForList(sqlQuery, compId);
//		for (Map<String, Object> map : list) {
//			Map<String, Object> item = new HashMap<>();
//			for (Entry<String, Object> entry : map.entrySet()) {
//				if (entry == null || entry.getValue() == null) {
//					System.out.println("entry or value is null");
//					continue;
//				}
//				String k = entry.getKey();
//				Object v = entry.getValue();
//				if (v instanceof BigDecimal || v instanceof Long) {
//					item.put(k, v);
//				}
//				System.out.println(entry.getKey() + "---->" + entry.getValue().getClass().getName());
//			}
//
//			indicatorMap.forEach((indicatorName, expresion) -> {
//				for (Entry<String, Object> entry : item.entrySet()) {
//					String k = entry.getKey();
//					if ("BTN147".equals(k)) {
//						System.out.println(k);
//					}
//					Object v = entry.getValue().toString();
//					if (expresion.toLowerCase().contains(k.toLowerCase())) {
//						v = v == null ? new BigDecimal(0) : v;
//						expresion = expresion.replaceAll(k, v.toString());
//					}
//					;
//				}
//				Execute execute = new CalculateDm(null, null).new Execute();
//				;
//				String suffix = execute.infixToSuffix(expresion);
//				try {
//					System.out.println(indicatorName + "-->" + execute.suffixToArithmetic(suffix, null));
//				} catch (Exception e) {
//					System.out.println("expression->" + suffix);
//					e.printStackTrace();
//				}
//			});

//		}

	}

	@Test
	public void buildIndicatorExpression() {
		XSSFWorkbook book = null;
		try {
			book = new XSSFWorkbook(
					new FileInputStream(new File("E:\\工作文档\\产品\\产品\\债券重构\\债券筛选\\财务指标计算公式\\指标计算公式.xlsx")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 第一个sheet
		XSSFSheet sheet = book.getSheetAt(0);
		int lastRowNum = book.getSheetAt(0).getLastRowNum();
		for (int i = 1; i < lastRowNum + 1; i++) {
			XSSFRow row = sheet.getRow(i);
			System.out.println(row.getCell(0).getStringCellValue());
			System.out.println(row.getCell(1).getStringCellValue());
			System.out.println(row.getCell(2).getStringCellValue());
			System.out.println(row.getCell(3).getStringCellValue());
			System.out.println(row.getCell(4).getStringCellValue());
			System.out.println(row.getCell(6).getStringCellValue());

			String tabelName = row.getCell(0).getStringCellValue();
			String field = row.getCell(2).getStringCellValue();
			String fieldName = row.getCell(3).getStringCellValue();
			String expression = row.getCell(5).getStringCellValue();
			String remark = row.getCell(6).getStringCellValue();
			if (field == null || "".equals(field) || "指标代码".equals(field)) {
				continue;
			}

			String querySql = "select count(*) from t_bond_indicator_expression where table_name = ? and field = ?";
			Object[] args = { tabelName, field };
			int count = jdbcTemplate.queryForObject(querySql, args, Integer.class);
			if (count == 0) {
				String insertSql = "insert into t_bond_indicator_expression(table_name,field,field_name,expression,remark) values(?,?,?,?,?)";
				jdbcTemplate.update(insertSql, tabelName, field, fieldName, expression, remark);
			}
		}

	}

	@Test
	public void loadIndicatorExpression() {
		// 获取原始数据
		Long compId = /* 27936L */ 1046L;
		String sqlQuery = "select * from manu_fina_sheet where comp_id = ? order by fin_date desc";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sqlQuery, compId);
		OriginalData od = new OriginalData(list);

		// 获取所有表达式
		String sqlQueryEcpression = "select table_name as tableName,field,field_name as fieldName,remark,expression from dmdb_zzl.t_bond_indicator_expression where table_name = 'FIN_RATIO_INDU'";
		List<ExpressionVo> listExpression = jdbcTemplate.query(sqlQueryEcpression,
				new BeanPropertyRowMapper<>(ExpressionVo.class));

		Map<String, String> expressions = new HashMap<>();
		listExpression.forEach(item -> {
			expressions.put(item.getField(), item.getExpression());
		});

		// 操作每期原始数据
		od.getIndicatorItems().forEach((finDate, datas) -> {
			System.out.println("finDate-->" + finDate);
			Map<String, Object> map = new HashMap<>();
			listExpression.forEach(item -> {
				String expression = item.getExpression();
				String field = item.getField();
				if ("Wrkg_Cptl".equals(field)) {
					System.out.println(1);
				}
				if ("Comp_ID".toLowerCase().equals(field.toLowerCase())) {
					// System.out.println(1);
				}

				Map<String, String> mapConstant = new HashMap<>();
				mapConstant.put("comp_id", "Comp_ID");
				mapConstant.put("fin_date", "FIN_DATE");
				mapConstant.put("fin_entity", "FIN_ENTITY");
				mapConstant.put("fin_state_type", "FIN_STATE_TYPE");
				mapConstant.put("fin_period", "FIN_PERIOD");

				Map<String, Object> data = od.getIndicatorItems().get(finDate);

				if (mapConstant.containsKey(field.toLowerCase())) {
					map.put(field, data.get(field));
				} else {
					BigDecimal ex = null;
	                try {
	                    ex = DateConvertUtil.formatterExpression(expression, od, expressions, finDate);
	                } catch(Exception e) {
	                    System.out.println(e.getMessage());
	                }
					// BigDecimal value = new CalculateDm().calculate(data, ex);
					map.put(field, ex);
				}
			});

			System.out.println("map to db-->" + map);
		});

		// 处理上几期表达式

	}
	
	@Test
	public void saveInduTest(){
		long s = System.currentTimeMillis();
		indicatorService.saveIndu();
		long e = System.currentTimeMillis();
		System.out.println("saveIndu time is " + (e-s));
	}
	
	
	@Test
	public void saveInsuTest(){
		long s = System.currentTimeMillis();
		indicatorService.saveInsu();
		long e = System.currentTimeMillis();
		System.out.println("saveInsu time is " + (e-s));
	}
	
	@Test
	public void saveSecuTest(){
		long s = System.currentTimeMillis();
		indicatorService.saveSecu();
		long e = System.currentTimeMillis();
		System.out.println("saveSecu time is " + (e-s));
	}
	
	@Test
	public void saveBankTest(){
		long s = System.currentTimeMillis();
		indicatorService.saveBank();
		long e = System.currentTimeMillis();
		System.out.println("saveBank time is " + (e-s));
	}
	
	
	
	@Autowired private IndicatorDao indicatorDao;
	@Test
	public void initIssuerTypeTest(){
		long s = System.currentTimeMillis();
		//indicatorDao.initIssuerType();
		long e = System.currentTimeMillis();
		System.out.println("initIssuerType time is " + (e-s));
	}
	
	
	@Test
	public void asbrsDataSourceTest(){
		System.out.println(jdbcTemplate.getDataSource());
		System.out.println(jdbcTemplate.getDataSource());
	}
	
	@Test
    public void testAddCalculate(){
        ThreadPoolExecutor threadpool = new ThreadPoolExecutor(1, 1, 60,TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(200)); 
        List<Future<ResponseData>> calculationResultList = new ArrayList<Future<ResponseData>>(30);
            calculationResultList.add(threadpool.submit(new Callable<ResponseData>() {
                @Override
                public ResponseData call() {
                    return creditRatingCalculateService.addCalculate(2L,DateUtils.convert2Date("2007-12-31", DateUtils.YYYY_MM_DD),"0");
                }
            }));
            
            calculationResultList.add(threadpool.submit(new Callable<ResponseData>() {
                @Override
                public ResponseData call() {
                    return creditRatingCalculateService.addCalculate(2L,DateUtils.convert2Date("2008-03-31", DateUtils.YYYY_MM_DD),"0");
                }
            }));
            
            calculationResultList.add(threadpool.submit(new Callable<ResponseData>() {
                @Override
                public ResponseData call() {
                    return creditRatingCalculateService.addCalculate(2L,DateUtils.convert2Date("2008-06-30", DateUtils.YYYY_MM_DD),"0");
                }
            }));

            
            calculationResultList.add(threadpool.submit(new Callable<ResponseData>() {
                @Override
                public ResponseData call() {
                    return creditRatingCalculateService.addCalculate(2L,DateUtils.convert2Date("2008-09-30", DateUtils.YYYY_MM_DD),"0");
                }
            }));
            
            calculationResultList.add(threadpool.submit(new Callable<ResponseData>() {
                @Override
                public ResponseData call() {
                    return creditRatingCalculateService.addCalculate(2L,DateUtils.convert2Date("2008-12-31", DateUtils.YYYY_MM_DD),"0");
                }
            }));
            
            calculationResultList.add(threadpool.submit(
                    new Callable<ResponseData>() {
                        @Override
                        public ResponseData call() throws Exception {
                            return creditRatingCalculateService.addCalculate(2L,DateUtils.convert2Date("2009-03-31", DateUtils.YYYY_MM_DD),"0");
                        }
                                    }));
                calculationResultList.add(threadpool.submit(
                    new Callable<ResponseData>() {
                        @Override
                        public ResponseData call() throws Exception {
                            return creditRatingCalculateService.addCalculate(2L,DateUtils.convert2Date("2009-06-30", DateUtils.YYYY_MM_DD),"0");
                                            }
                                        }));
                calculationResultList.add(threadpool.submit(
                    new Callable<ResponseData>() {
                        @Override
                        public ResponseData call() throws Exception {
                            return creditRatingCalculateService.addCalculate(2L,DateUtils.convert2Date("2009-09-30", DateUtils.YYYY_MM_DD),"0");
                        }
                                        }));
                calculationResultList.add(threadpool.submit(
                    new Callable<ResponseData>() {
                        @Override
                        public ResponseData call() throws Exception {
                            return creditRatingCalculateService.addCalculate(2L,DateUtils.convert2Date("2009-12-31", DateUtils.YYYY_MM_DD),"0");
                        }
                                        }));
                
                
                
                calculationResultList.add(threadpool.submit(
                    new Callable<ResponseData>() {
                        @Override
                        public ResponseData call() throws Exception {
                            return creditRatingCalculateService.addCalculate(2224L,DateUtils.convert2Date("2007-12-31", DateUtils.YYYY_MM_DD),"0");
                        }
                                        }));
                calculationResultList.add(threadpool.submit(
                    new Callable<ResponseData>() {
                        @Override
                        public ResponseData call() throws Exception {
                            return creditRatingCalculateService.addCalculate(2224L,DateUtils.convert2Date("2008-12-31", DateUtils.YYYY_MM_DD),"0");
                        }
                                        }));
                calculationResultList.add(threadpool.submit(
                    new Callable<ResponseData>() {
                        @Override
                        public ResponseData call() throws Exception {
                            return creditRatingCalculateService.addCalculate(2224L,DateUtils.convert2Date("2009-03-31", DateUtils.YYYY_MM_DD),"0");
                        }
                                        }));
                calculationResultList.add(threadpool.submit(
                    new Callable<ResponseData>() {
                        @Override
                        public ResponseData call() throws Exception {
                            return creditRatingCalculateService.addCalculate(2224L,DateUtils.convert2Date("2009-06-30", DateUtils.YYYY_MM_DD),"0");
                        }
                                        }));
                calculationResultList.add(threadpool.submit(
                    new Callable<ResponseData>() {
                        @Override
                        public ResponseData call() throws Exception {
                            return creditRatingCalculateService.addCalculate(2224L,DateUtils.convert2Date("2009-09-30", DateUtils.YYYY_MM_DD),"0");
                        }
                                        }));

            
            // 关闭线程池:会等待所有线程执行完
            threadpool.shutdown();
            for (Future<ResponseData> res : calculationResultList) {
                try {
                    if (!"0000".equals(res.get().getResponseCode())) {
                        System.out.println(res.get().getResponseCode()+","+res.get().getResponseMessage());
                    } else {
                        System.out.println("yes="+res.get().getResponseCode()+","+res.get().getResponseMessage());
                    }
                } catch (InterruptedException | ExecutionException e) {
                    
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    
                }
            }
            
//            ExecutorService pool = Executors.newFixedThreadPool(10); 
//            List<Future<ResponseData>> resultLists=new ArrayList<Future<ResponseData>>(10);
//            resultLists.add(pool.submit(
//                new Callable<ResponseData>() {
//                                    @Override
//                                    public ResponseData call() throws Exception {
//                                        return creditRatingCalculateService.addCalculate(2L,DateUtils.convert2Date("2009-03-31", DateUtils.YYYY_MM_DD),"0");
//                                    }
//                                }));
//            resultLists.add(pool.submit(
//                    new Callable<ResponseData>() {
//                                        @Override
//                                        public ResponseData call() throws Exception {
//                                            return creditRatingCalculateService.addCalculate(2L,DateUtils.convert2Date("2009-06-30", DateUtils.YYYY_MM_DD),"0");
//                                        }
//                                    }));
//            resultLists.add(pool.submit(
//                    new Callable<ResponseData>() {
//                                        @Override
//                                        public ResponseData call() throws Exception {
//                                            return creditRatingCalculateService.addCalculate(2L,DateUtils.convert2Date("2009-06-30", DateUtils.YYYY_MM_DD),"0");
//                                        }
//                                    }));
//            resultLists.add(pool.submit(
//                    new Callable<ResponseData>() {
//                                        @Override
//                                        public ResponseData call() throws Exception {
//                                            return creditRatingCalculateService.addCalculate(2L,DateUtils.convert2Date("2009-09-30", DateUtils.YYYY_MM_DD),"0");
//                                        }
//                                    }));
//            resultLists.add(pool.submit(
//                    new Callable<ResponseData>() {
//                                        @Override
//                                        public ResponseData call() throws Exception {
//                                            return creditRatingCalculateService.addCalculate(2L,DateUtils.convert2Date("2009-12-31", DateUtils.YYYY_MM_DD),"0");
//                                        }
//                                    }));
//            
//            
//            
//            resultLists.add(pool.submit(
//                    new Callable<ResponseData>() {
//                                        @Override
//                                        public ResponseData call() throws Exception {
//                                            return creditRatingCalculateService.addCalculate(2224L,DateUtils.convert2Date("2007-12-31", DateUtils.YYYY_MM_DD),"0");
//                                        }
//                                    }));
//            resultLists.add(pool.submit(
//                    new Callable<ResponseData>() {
//                                        @Override
//                                        public ResponseData call() throws Exception {
//                                            return creditRatingCalculateService.addCalculate(2224L,DateUtils.convert2Date("2008-12-31", DateUtils.YYYY_MM_DD),"0");
//                                        }
//                                    }));
//            resultLists.add(pool.submit(
//                    new Callable<ResponseData>() {
//                                        @Override
//                                        public ResponseData call() throws Exception {
//                                            return creditRatingCalculateService.addCalculate(2224L,DateUtils.convert2Date("2009-03-31", DateUtils.YYYY_MM_DD),"0");
//                                        }
//                                    }));
//            resultLists.add(pool.submit(
//                    new Callable<ResponseData>() {
//                                        @Override
//                                        public ResponseData call() throws Exception {
//                                            return creditRatingCalculateService.addCalculate(2224L,DateUtils.convert2Date("2009-06-30", DateUtils.YYYY_MM_DD),"0");
//                                        }
//                                    }));
//            resultLists.add(pool.submit(
//                    new Callable<ResponseData>() {
//                                        @Override
//                                        public ResponseData call() throws Exception {
//                                            return creditRatingCalculateService.addCalculate(2224L,DateUtils.convert2Date("2009-09-30", DateUtils.YYYY_MM_DD),"0");
//                                        }
//                                    }));
//
//            for (Future<ResponseData> res : resultLists) {
//                try {
//                    if (!"0000".equals(res.get().getResponseCode())) {
//                        System.out.println(res.get().getResponseCode()+","+res.get().getResponseMessage());
//                    } else {
//                        System.out.println("yesnode="+res.get().getResponseCode()+","+res.get().getResponseMessage());
//                    }
//                } catch (InterruptedException | ExecutionException e) {
//                    
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                    
//                }
//            }
    }
	
	
	
	public static void main(String[] args) {
		
	}
}

