package com.innodealing.vo.rating;

import java.io.Serializable;

/**
 * 安硕主体模型信息
 * @author 赵正来
 *
 */
public class RatingModelVo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * 模型id
	 */
	private Integer modelId;
	
	
	/**
	 * 模型名称
	 */
	private String modelName;


	public Integer getModelId() {
		return modelId;
	}


	public void setModelId(Integer modelId) {
		this.modelId = modelId;
	}


	public String getModelName() {
		return modelName;
	}


	public void setModelName(String modelName) {
		this.modelName = modelName;
	}


	public RatingModelVo() {
		super();
	}


	public RatingModelVo(Integer modelId, String modelName) {
		super();
		this.modelId = modelId;
		this.modelName = modelName;
	}


	@Override
	public String toString() {
		return "RatingModelVo [modelId=" + modelId + ", modelName=" + modelName + "]";
	}
	
	
}
