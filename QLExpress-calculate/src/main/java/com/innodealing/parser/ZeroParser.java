package com.innodealing.parser;

public class ZeroParser implements Parser {

	@Override
	public Result parse(String target) {
		return Result.succeed("", target); 
	}

}
