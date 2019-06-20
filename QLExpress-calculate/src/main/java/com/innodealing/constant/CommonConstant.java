package com.innodealing.constant;
/**
 * 常量
 * @author 赵正来
 *
 */
public class CommonConstant {
	/**  银行 bank */
	public static final String ISSUER_TYPE_BANK = "bank";
	
	/** 保险  insurance */
    public static final String ISSUER_TYPE_INSU = "insu";
    
    /** 券商 security */
    public static final String ISSUER_TYPE_SECU = "secu";
	
	/** 非金融 industry/工业 */
	public static final String ISSUER_TYPE_INDU = "indu";

	/** 商业 */
	public static final String ISSUER_TYPE_BUSIN = "busin";
	
	/** 房地产 */
	public static final String ISSUER_TYPE_ESTATE = "estate";
	
	/** 非金融标识 */
    public static final String ISSUER_TYPE_NFIN = "nfin";
    
	/** 专项指标银行表 */
	public static final String SPECIAL_TABLE_BANK = "dm_analysis_bank";
	
	/**  专项指标非金融表 */
	public static final String SPECIAL_TABLE_INDU = "dm_analysis_indu";
	
	/**  专项指标保险表 */
	public static final String SPECIAL_TABLE_INSU = "dm_analysis_insu";
	
	/** 专项指标券商表 */
	public static final String SPECIAL_TABLE_SECU = "dm_analysis_secu";

	/**
	 * 评级评分模型名称
	 */
	public static final String MODEL_NAME_INDU = "工业模型";
	
	public static final String MODEL_NAME_BUSIN = "商业服务业模型";
	
	public static final String MODEL_NAME_ESTATE = "房地产模型";
	
	public static final String MODEL_NAME_BANK = "银行模型";
	
	public static final String MODEL_NAME_SECU = "证券模型";
	
	public static final String MODEL_NAME_INSU = "保险模型";
	
	
	/** 财报来源：中诚信*/
	public static final String SHEET_SOURCE_CCXE ="0";
	
	/** 财报来源：DM*/
    public static final String SHEET_SOURCE_DM ="1";
    
    /** 财报指标计算安硕接口令牌token(Redis)*/
    public static final String SHEET_TOKEN_CACHE="finaSheetToken";
    
    /** 财报指标计算年化错误：本季度数据缺失*/
    public static final String CAL_ERROR_CODE_Q_NO="Q_NO";
    
    /** 财报指标计算年化错误：去年本季度数据缺失*/
    public static final String CAL_ERROR_CODE_PP_Q_NO="PP_Q_NO";
    
    /** 财报指标计算年化错误：去年年报数据缺失*/
    public static final String CAL_ERROR_CODE_PP_Y_NO="PP_Y_NO";
       
	
	/**
	 * 
	 * ClassName:AmaresunCallbackCode
	 * Function: 安硕接口返回码
	 * Reason:	 便于前台显示 
	 * @author   chungaochen
	 * @version  CommonConstant
	 * @since    Ver 1.1
	 * @Date	 2017	2017年6月6日		上午11:09:55
	 *
	 * @see
	 */
	public enum AmaresunCallbackCode {
	    NOMAL_RETURN("0000","正常返回"),
	    SYSTEM_OTHER_ERROR("1111","系统其它错误"),
	    TOKEN_IS_EMPTY("1000","token为空"), 
	    USERID_IS_EMPTY("1001","userId 为空"),
	    TOKEN_NO_AUTHENTICATION("1002","未获取token认证"),
	    TOKEN_NO_RIGHT("1003","token不正确"),
	    TOKEN_IS_INVALID("1004","token失效,需要重新认证"),
	    NOT_FIND_DRIVER_FOR_DATABASE("1005","AMARESUN ERROR 02: 找不到连接数据库的驱动"),
	    DATABASE_CONNECTION_EXCEPTION("1006","数据库连接异常"),
	    JSON_FORMAT_ERROR("1007","DM ERROR 01: JSON格式不正确"),
	    MODEL_NOT_EXIST("1008","模型不存在"),
	    NUMBER_OF_INDEX_ERROR("1009","传入指标数量不正确"),
	    SOME_INDEX_IS_NULL("1010","有指标为NULL, 无法计算评级")
	    ;
	    /** 返回码  */
	    private String code ;
	    /** 返回码含义 */
	    private String codeMessage;
	    
	    private AmaresunCallbackCode(String code, String codeMessage) {
	        this.code = code;
	        this.codeMessage = codeMessage;
	    }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getCodeMessage() {
            return codeMessage;
        }

        public void setCodeMessage(String codeMessage) {
            this.codeMessage = codeMessage;
        }
	    
        public static String getName(String code) {  
            for (AmaresunCallbackCode c : AmaresunCallbackCode.values()) {  
                if (c.getCode().equals(code) ) {  
                    return c.codeMessage;  
                }  
            }  
            return null;  
        }  
        
        public boolean test(String code) {
            if(this.getCode().equals(code)) {
                return true;
            } else {
                return false;
            }
        }

	}
	
	/**
	 * 
	 * ClassName:DMCallbackCode
	 * Function: DM返回码
	 * Reason:	
	 * @author   chungaochen
	 * @version  CommonConstant
	 * @since    Ver 1.1
	 * @Date	 2017	2017年6月6日		上午11:55:54
	 *
	 * @see
	 */
    public enum DMCallbackCode {
        NOMAL_RETURN("0000","正常返回"),
        SYSTEM_INTERNAL_ERROR("9999","系统内部错误")
        ;
        /** 返回码  */
        private String code ;
        /** 返回码含义 */
        private String codeMessage;
        
        private DMCallbackCode(String code, String codeMessage) {
            this.code = code;
            this.codeMessage = codeMessage;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getCodeMessage() {
            return codeMessage;
        }

        public void setCodeMessage(String codeMessage) {
            this.codeMessage = codeMessage;
        }
        
        public static String getName(String code) {  
            for (AmaresunCallbackCode c : AmaresunCallbackCode.values()) {  
                if (c.getCode().equals(code) ) {  
                    return c.codeMessage;  
                }  
            }  
            return null;  
        } 
        
        public boolean test(String code) {
            if(this.getCode().equals(code)) {
                return true;
            } else {
                return false;
            }
        }

    }
	
    /**
	 * FINANCE 财务指标，SPECIAL 专项指标
	 */
	public static final Integer FINANCE = 1, SPECIAL = 2;
	

}
