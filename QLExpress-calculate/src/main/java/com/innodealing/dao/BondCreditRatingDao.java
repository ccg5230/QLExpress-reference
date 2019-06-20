package com.innodealing.dao;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.innodealing.config.DatabaseNameConfig;
import com.innodealing.domain.BondCreditRating;

@Component
public class BondCreditRatingDao {
	
	
    @Autowired private JdbcTemplate jdbcTemplate;
    
    @Autowired private DatabaseNameConfig databaseNameConfig;
    
    public static final Logger log = LoggerFactory.getLogger(BondCreditRatingDao.class);
	
	 /**
     * 
     * BondCreditRating:(批量插入BondCreditRating)
     * @param  @param beanList
     * @param  @return    设定文件
     * @return boolean    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    public void batchInsertBondCreditRating(List<BondCreditRating> beanList) throws Exception {
        String sql = "insert into " + databaseNameConfig.getDmdb() +
                ".t_bond_credit_rating(com_chi_name,com_uni_code,model_id ,fin_date ,create_time ,last_update_time," +
                "ratio1,ratio2,ratio3,ratio4,ratio5,ratio6,ratio7,ratio8,ratio9,ratio10," +        
                "rating,ratio1_score,ratio2_score,ratio3_score,ratio4_score,ratio5_score,ratio6_score," +
                "ratio7_score,ratio8_score,ratio9_score,ratio10_score,fin_quality_score,source,remark) "
                + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";//29columns
        try {
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {   
                
                @Override  
                public int getBatchSize() {   
                     return beanList.size();    
                }   
                @Override  
                public void setValues(PreparedStatement ps, int i)   
                        throws SQLException {   
                      ps.setString(1, beanList.get(i).getComChiName());    
                      ps.setLong(2, beanList.get(i).getComUniCode());    
                      ps.setString(3, beanList.get(i).getModelId());    
                      ps.setDate(4, beanList.get(i).getFinDate());    
                      ps.setTimestamp(5, beanList.get(i).getCreateTtime());                    
                      ps.setTimestamp(6, beanList.get(i).getLastUpdateTime());   
                      ps.setBigDecimal(7, beanList.get(i).getRatio1()==null? null :beanList.get(i).getRatio1().setScale(4, BigDecimal.ROUND_HALF_UP));
                      ps.setBigDecimal(8, beanList.get(i).getRatio2()==null? null :beanList.get(i).getRatio2().setScale(4, BigDecimal.ROUND_HALF_UP));
                      ps.setBigDecimal(9, beanList.get(i).getRatio3()==null? null :beanList.get(i).getRatio3().setScale(4, BigDecimal.ROUND_HALF_UP));
                      ps.setBigDecimal(10, beanList.get(i).getRatio4()==null? null :beanList.get(i).getRatio4().setScale(4, BigDecimal.ROUND_HALF_UP));
                      ps.setBigDecimal(11, beanList.get(i).getRatio5()==null? null :beanList.get(i).getRatio5().setScale(4, BigDecimal.ROUND_HALF_UP));
                      ps.setBigDecimal(12, beanList.get(i).getRatio6()==null? null :beanList.get(i).getRatio6().setScale(4, BigDecimal.ROUND_HALF_UP));
                      ps.setBigDecimal(13, beanList.get(i).getRatio7()==null? null :beanList.get(i).getRatio7().setScale(4, BigDecimal.ROUND_HALF_UP));
                      ps.setBigDecimal(14, beanList.get(i).getRatio8()==null? null :beanList.get(i).getRatio8().setScale(4, BigDecimal.ROUND_HALF_UP));
                      ps.setBigDecimal(15, beanList.get(i).getRatio9()==null? null :beanList.get(i).getRatio9().setScale(4, BigDecimal.ROUND_HALF_UP));
                      ps.setBigDecimal(16, beanList.get(i).getRatio10()==null? null :beanList.get(i).getRatio10().setScale(4, BigDecimal.ROUND_HALF_UP));
                      ps.setString(17, beanList.get(i).getRating());
                      ps.setBigDecimal(18, beanList.get(i).getRatio1Score()==null? null :beanList.get(i).getRatio1Score().setScale(6, BigDecimal.ROUND_HALF_UP));
                      ps.setBigDecimal(19, beanList.get(i).getRatio2Score()==null? null :beanList.get(i).getRatio2Score().setScale(6, BigDecimal.ROUND_HALF_UP));
                      ps.setBigDecimal(20, beanList.get(i).getRatio3Score()==null? null :beanList.get(i).getRatio3Score().setScale(6, BigDecimal.ROUND_HALF_UP));
                      ps.setBigDecimal(21, beanList.get(i).getRatio4Score()==null? null :beanList.get(i).getRatio4Score().setScale(6, BigDecimal.ROUND_HALF_UP));
                      ps.setBigDecimal(22, beanList.get(i).getRatio5Score()==null? null :beanList.get(i).getRatio5Score().setScale(6, BigDecimal.ROUND_HALF_UP));
                      ps.setBigDecimal(23, beanList.get(i).getRatio6Score()==null? null :beanList.get(i).getRatio6Score().setScale(6, BigDecimal.ROUND_HALF_UP));
                      ps.setBigDecimal(24, beanList.get(i).getRatio7Score()==null? null :beanList.get(i).getRatio7Score().setScale(6, BigDecimal.ROUND_HALF_UP));
                      ps.setBigDecimal(25, beanList.get(i).getRatio8Score()==null? null :beanList.get(i).getRatio8Score().setScale(6, BigDecimal.ROUND_HALF_UP));
                      ps.setBigDecimal(26, beanList.get(i).getRatio9Score()==null? null :beanList.get(i).getRatio9Score().setScale(6, BigDecimal.ROUND_HALF_UP));
                      ps.setBigDecimal(27, beanList.get(i).getRatio10Score()==null? null :beanList.get(i).getRatio10Score().setScale(6, BigDecimal.ROUND_HALF_UP));
                      ps.setBigDecimal(28, beanList.get(i).getFinQualityScore()==null? null :beanList.get(i).getFinQualityScore().setScale(6, BigDecimal.ROUND_HALF_UP));
                      ps.setString(29, beanList.get(i).getSource());
                      ps.setString(30, beanList.get(i).getRemark());
                }    
          });    
        } catch (DataAccessException e) {
            log.error("batchUpdate into  t_bond_credit_rating error!" + e.getMessage());
            throw e;
        }
        
    }

    /**
     * 
     * delCreditRatingByIssuerIdAndFinDate:(根据主体id和财报日期删除财报指标质量得分评级)
     * @param  @param com_uni_code 主体id
     * @param  @param finDate
     * @param  @return    设定文件
     * @return int    DOM对象
     * @throws 
     * @since  CodingExample　Ver 1.1
     */
    public int delCreditRatingByIssuerIdAndFinDate(long com_uni_code, Date finDate) {
        String sql = "DELETE FROM " + databaseNameConfig.getDmdb() +
                ".t_bond_credit_rating WHERE com_uni_code=? AND fin_date=? ";
        int count =jdbcTemplate.update(sql, com_uni_code,finDate);
        return count;
    }
    
    /**
     * 根据主体id和财报日期查找财报指标质量得分评级
     * @param compUniCode
     * @return
     */
    public List<BondCreditRating> findByCompId(Long compUniCode){
    	String sql = "select com_uni_code as comUniCode,"
    			+ "model_id as modelId,"
    			+ "fin_date as finDate,"
    			+ "com_chi_name as comChiName,"
    			+ "rating,"
    			+ "ratio1_score as ratio1Score,"
    			+ "ratio2_score as ratio2Score,"
    			+ "ratio3_score as ratio3Score,"
    			+ "ratio4_score as ratio4Score,"
    			+ "ratio5_score as ratio5Score,"
    			+ "ratio6_score as ratio6Score,"
    			+ "ratio7_score as ratio7Score,"
    			+ "ratio8_score as ratio8Score,"
    			+ "ratio9_score as ratio9Score,"
    			+ "ratio10_score as ratio10Score,"
    			+ "remark as remark,"
    			+ "fin_quality_score as finQualityScore"
    			+ " from " +databaseNameConfig.getDmdb() + ".t_bond_credit_rating"
    			+ " where com_uni_code = " + compUniCode + " order by fin_date asc";
    	return jdbcTemplate.query(sql, new BeanPropertyRowMapper<BondCreditRating>(BondCreditRating.class));
    }
}


