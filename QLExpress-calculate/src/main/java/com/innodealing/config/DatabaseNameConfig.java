package com.innodealing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 * 数据名称配置信息
 * @author 赵正来
 *
 */
@ConfigurationProperties(prefix = "database.name")
public class DatabaseNameConfig {
	@Value("${database.name.dmdb}")
	private String dmdb;
	
	@Value("${database.name.amaresun}")
	private String amaresun;
	
//	@Value("${database.name.asbrs-dm}")
//	private String asbrsDm;
	
	@Value("${database.name.bond-ccxe}")
	private String bondCcxe;
	
	@Value("${database.name.innodealing}")
	private String innodealing;
	
	@Value("${database.name.asbrs}")
	private String asbrs;

	public String getDmdb() {
		return dmdb;
	}

	public void setDmdb(String dmdb) {
		this.dmdb = dmdb;
	}

	public String getAmaresun() {
		return amaresun;
	}

	public void setAmaresun(String amaresun) {
		this.amaresun = amaresun;
	}

//	public String getAsbrsDm() {
//		return asbrsDm;
//	}
//
//	public void setAsbrsDm(String asbrsDm) {
//		this.asbrsDm = asbrsDm;
//	}

	public String getBondCcxe() {
		return bondCcxe;
	}

	public void setBondCcxe(String bondCcxe) {
		this.bondCcxe = bondCcxe;
	}

	public String getInnodealing() {
		return innodealing;
	}

	public void setInnodealing(String innodealing) {
		this.innodealing = innodealing;
	}
	
	public String getAsbrs() {
		return asbrs;
	}

	public void setAsbrs(String asbrs) {
		this.asbrs = asbrs;
	}

	@Override
	public String toString() {
		return "DatabaseNameConfig [dmdb=" + dmdb + ", amaresun=" + amaresun + /*", asbrsDm=" + asbrsDm +*/ ", bondCcxe="
				+ bondCcxe + ", innodealing=" + innodealing + "]";
	}
	
	
}
