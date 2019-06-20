/**
 * MaxOperator.java
 * com.innodealing.util.express
 *
 * Function： TODO 
 *
 *   ver     date      		author
 * ──────────────────────────────────
 *   		 2017年6月5日 		chungaochen
 *
 * Copyright (c) 2017, DealingMatrix All Rights Reserved.
*/

package com.innodealing.util.express;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ql.util.express.Operator;

/**
 * ClassName:MaxOperator
 * Function: max比较函数
 * Reason:	 
 *
 * @author   chungaochen
 * @version  
 * @since    Ver 1.1
 * @Date	 2017年6月5日		上午11:48:01
 *
 * @see 	 
 */
public class DMMaxOperator extends Operator {

    Logger log = LoggerFactory.getLogger(StdOperator.class);

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    
    
    public DMMaxOperator() {
        this.name = "dmmax";
    }

    public DMMaxOperator(String name){
        this.name = name;
    }
    
    @Override
    public Object executeInner(Object[] list) throws Exception {
        List<Double> listData = new ArrayList<>();
        for (int i = 0; i < list.length; i++) {
            Object obj = list[i];
            if(null == obj) {
                listData.add(new Double(0));
            } else {
                Double val = Double.parseDouble(obj.toString());
                listData.add(val);
            }
        }
        
        double[] data = new double[listData.size()];
        for (int i = 0; i < data.length; i++) {
            data[i] = listData.get(i);
        }
        return  Math.max(data[0],data[1]);
        
    }

}

