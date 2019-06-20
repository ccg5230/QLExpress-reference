package com.innodealing;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.innodealing.dao.BondComExtDao;
import com.innodealing.vo.BondComExtVo;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BondCalculateApplication.class)
@WebIntegrationTest
public class BondComExtDaoTest {

	private @Autowired BondComExtDao bondComExtDao;
	
	@Test
	public void findAllTest(){
		List<BondComExtVo> list = bondComExtDao.findAll();
		System.out.println(list);
	}
}
