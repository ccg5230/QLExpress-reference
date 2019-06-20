package com.innodealing.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;

/**
 * 
 * ClassName:BondFinanceSheetIndicatorExpression
 * Function: 财报指标计算公式domain
 * Reason:	 数据查询映射
 *
 * @author   chungaochen
 * @version  
 * @since    Ver 1.1
 * @Date	 2017	2017年5月25日		下午3:32:44
 *
 * @see
 */
public class BondFinanceSheetIndicatorExpression implements Serializable {

	/** 
	* @Fields serialVersionUID :
	*/ 
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "主键id")
	private Long id;
	
	@ApiModelProperty(value = "指标代码")
	private String field;
	
	@ApiModelProperty(value = "指标名称")
	private String fieldName;
	
	@ApiModelProperty(value = "指标类别")
    private String type;
	
	@ApiModelProperty(value = "模型ID")
    private String modelId;
	
	@ApiModelProperty(value = "表达式")
	private String expression;
	
	@ApiModelProperty(value = "表达式说明")
    private String expressDescription;
	
	@ApiModelProperty(value = "备注说明")
	private String remark;
	
	@ApiModelProperty(value = "表达式格式化")
	private String expressFormat;

	/**
	 * formate:(转换计算表达式)
	 * @param  @param expression
	 * @param  @return    设定文件
	 * @return String    DOM对象
	 * @throws 
	 * @since  CodingExample　Ver 1.1
	 */
	public static String formate(String expression) {
        return expression.toLowerCase()
            .replaceAll("（", "(")
            .replaceAll("）", ")")
            .replaceAll("\\[", "(")
            .replaceAll("\\]", ")")
            .replaceAll("＋", "+")
            .replaceAll("－", "-")
            .replaceAll("，", ",")
            .replaceAll("；", ",");
    }
	
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getExpressDescription() {
        return expressDescription;
    }

    public void setExpressDescription(String expressDescription) {
        this.expressDescription = expressDescription;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getExpressFormat() {
        return expressFormat;
    }

    public void setExpressFormat(String expressFormat) {
        this.expressFormat = expressFormat;
    }

    @Override
    public String toString() {
        return "BondFinanceSheetIndicatorExpression [id=" + id + ", field=" + field + ", fieldName=" + fieldName + ", type=" + type + ", modelId=" + modelId
                +", expression=" + expression + ", expressDescription=" + expressDescription + ", remark=" + remark
                + ", expressFormat=" + expressFormat + "]";
    }
	
	
}
