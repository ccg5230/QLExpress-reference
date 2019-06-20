package com.innodealing.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.innodealing.config.DatabaseNameConfig;
import com.innodealing.domain.RatingRatioScore;
/**
 * 指标得分Dao
 * @author 赵正来
 *
 */
@Component
public class RatingRatioScoreDao {

	private @Autowired JdbcTemplate jdbcTemplate;
	
	private @Autowired DatabaseNameConfig dbNameConfig;
	

	
	public boolean insert(RatingRatioScore ratingRatioScore) throws Exception{
		
		if(ratingRatioScore == null){
			throw new NullPointerException("ratingRatioScore不能为空");
		}
		
		String sql = "insert into " + dbNameConfig.getDmdb() +
				".rating_ratio_score (comp_id,year,model_id,model_name,ratio1,ratio2,ratio3,ratio4,ratio5,ratio6,ratio7,ratio8,ratio9,ratio10) " + 
				" values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		//如果数据库中有则删除
		deleteByCompIdAndYear(ratingRatioScore.getCompId(), ratingRatioScore.getYear());
		//插入数据
		int count = jdbcTemplate.update(sql, 
				ratingRatioScore.getCompId(),
				ratingRatioScore.getYear(),
				ratingRatioScore.getModelId(),
				ratingRatioScore.getModelName(),
				ratingRatioScore.getRatio1(),
				ratingRatioScore.getRatio2(),
				ratingRatioScore.getRatio3(),
				ratingRatioScore.getRatio4(),
				ratingRatioScore.getRatio5(),
				ratingRatioScore.getRatio6(),
				ratingRatioScore.getRatio7(),
				ratingRatioScore.getRatio8(),
				ratingRatioScore.getRatio9(),
				ratingRatioScore.getRatio10());
		return count == 1;
	}
	
	public boolean batchInsert(List<RatingRatioScore> list) throws Exception{
	    if(null==list || list.size()==0) {
	        return false;
	    }
		String sql = "insert into " + dbNameConfig.getDmdb() +
				".rating_ratio_score (comp_id,year,model_id,model_name,ratio1,ratio2,ratio3,ratio4,ratio5,ratio6,ratio7,ratio8,ratio9,ratio10) " + 
				" values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		//先删除之前数据
		deleteByCompId(list.get(0).getCompId());
		//然后批量插入
		jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				
				ps.setObject(1, list.get(i).getCompId());
				ps.setObject(2, list.get(i).getYear());
				ps.setObject(3, list.get(i).getModelId());
				ps.setObject(4, list.get(i).getModelName());
				ps.setObject(5, list.get(i).getRatio1());
				ps.setObject(6, list.get(i).getRatio2());
				ps.setObject(7, list.get(i).getRatio3());
				ps.setObject(8, list.get(i).getRatio4());
				ps.setObject(9, list.get(i).getRatio5());
				ps.setObject(10, list.get(i).getRatio6());
				ps.setObject(11, list.get(i).getRatio7());
				ps.setObject(12, list.get(i).getRatio8());
				ps.setObject(13, list.get(i).getRatio9());
				ps.setObject(14, list.get(i).getRatio10());
			}
			
			@Override
			public int getBatchSize() {
				return list.size(); 
			}
		});
		return true;
	}
	
	
	public boolean deleteByCompIdAndYear(Long compId, Integer year)  throws Exception{
		String sql = "delete from " +  dbNameConfig.getDmdb() + ".rating_ratio_score where comp_id = ? and year = ?";
		int count = jdbcTemplate.update(sql, compId, year);
		return count == 1;
	}
	
	public boolean deleteByCompId(Long compId)  throws Exception{
		String sql = "delete from " +  dbNameConfig.getDmdb() + ".rating_ratio_score where comp_id = ? ";
		int count = jdbcTemplate.update(sql, compId);
		return count == 1;
	}
	
	
	
	
}
