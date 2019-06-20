package com.innodealing.dao;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.innodealing.config.DatabaseNameConfig;
import com.innodealing.exception.BusinessException;
import com.innodealing.vo.BondComExtVo;
/**
 * 主体关系类dao
 * @author 赵正来
 *
 */
@Component
public class BondComExtDao {
	private @Autowired JdbcTemplate jdbcTemplate;
	
	private @Autowired DatabaseNameConfig databaseNameConfig;
	
	private static volatile Map<Long,BondComExtVo> AMA_COM_ID_KEY;
	
	private static volatile Map<Long,BondComExtVo> COM_UNI_CODE_KEY;
	
	/**
	 * 查找所有
	 * @return
	 */
	public List<BondComExtVo> findAll(){
		String sql = "SELECT com_chi_name,com_uni_code,ama_com_id ,ama_com_name, indu_uni_code,indu_uni_name_l4  FROM " + databaseNameConfig.getDmdb() + ".t_bond_com_ext ";
		return jdbcTemplate.query(sql, new BeanPropertyRowMapper<BondComExtVo>(BondComExtVo.class));
	}
	
	/**
	 * 缓存中查找
	 * @return
	 */
//	public Map<Long,BondComExtVo> findAllAmaCache(){
//		if(AMA_COM_ID_KEY == null){
//			synchronized(BondComExtDao.class){
//				if(AMA_COM_ID_KEY == null){
//					List<BondComExtVo> list = findAll();
//					AMA_COM_ID_KEY = new HashMap<>();
//					for (BondComExtVo bondComExtVo : list) {
//						AMA_COM_ID_KEY.put(bondComExtVo.getAmaComId(), bondComExtVo);
//					}
//				}
//			}
//			
//		}
//		return AMA_COM_ID_KEY;
//	}
	
	/**
	 * 查找
	 * @return
	 */
	public BondComExtVo findByUniCode(Long comUniCode){
		String sql = "SELECT com_chi_name,com_uni_code,ama_com_id ,ama_com_name, indu_uni_code,indu_uni_name_l4  FROM " + databaseNameConfig.getDmdb() + ".t_bond_com_ext where com_uni_code = " + comUniCode;
		return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<BondComExtVo>(BondComExtVo.class));
	}
	
	/**
	 * 查找
	 * @return
	 */
	public BondComExtVo findByComId(Long compId){
		String sql = "SELECT com_chi_name,com_uni_code,ama_com_id ,ama_com_name, indu_uni_code,indu_uni_name_l4  FROM " + databaseNameConfig.getDmdb() + ".t_bond_com_ext where ama_com_id = " + compId;
		BondComExtVo vo = null;
		try {
			vo = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<BondComExtVo>(BondComExtVo.class));
		} catch (EmptyResultDataAccessException e) {
			throw new BusinessException("该主体不存在");
		}
		return vo;
	}
	
	/**
	 * 缓存中查找
	 * @return
	 */
//	public Map<Long,BondComExtVo> findAllNuiCache(){
//		if(COM_UNI_CODE_KEY == null){
//			synchronized(BondComExtDao.class){
//				if(COM_UNI_CODE_KEY == null){
//					List<BondComExtVo> list = findAll();
//					COM_UNI_CODE_KEY = new HashMap<>();
//					for (BondComExtVo bondComExtVo : list) {
//						COM_UNI_CODE_KEY.put(bondComExtVo.getComUniCode(), bondComExtVo);
//					}
//				}
//			}
//			
//		}
//		return COM_UNI_CODE_KEY;
//	}
	
}


