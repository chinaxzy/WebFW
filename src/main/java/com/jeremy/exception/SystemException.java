/*******************************************************************************
 * Copyright (c) 2016, 2016 GuoGuang Corporation and others.
 * All rights reserved. 
 *  
 * Create By Time 2016-1-11-下午3:48:14
 * Contributors:
 *     longshiyu
 *******************************************************************************/

package com.jeremy.exception;
 

import org.apache.log4j.Logger;

public class SystemException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final Logger logger = Logger.getLogger(SystemException.class);

	
	public SystemException(String msg) {
		logger.error(msg);
	}

	public SystemException(String errId, Object... args) {
		logger.error(errId+":"+ args.toString());
	}
	
	public SystemException(Throwable t) {
		logger.error(t);
	}

	public SystemException(String msg, Throwable t) {
		logger.error(msg,t);
	}

	public void setExpression(String excSource) {
		// TODO Auto-generated method stub
		
	}
	
	
}

