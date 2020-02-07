package abscon.instance.components;

import abscon.instance.Toolkit;
import abscon.instance.intension.PredicateManager;
import utils.Logger;

import java.util.Arrays;

public class PFunction {


	protected String name;

	public String effectiveParametersExpression;

	protected String[] formalParameters;

	protected String functionalExpression;

	protected String[] universalPostfixExpression;
	

	public String getName() {
		return name;
	}

	public String[] getFormalParameters() {
		return formalParameters;
	}

	public String[] getUniversalPostfixExpression() {
		return universalPostfixExpression;
	}

	public String getFunctionalExpression() { return functionalExpression; }


	public PFunction(String name, String formalParametersExpression, String functionalExpression) {
		this.name = name;
		this.formalParameters =  PredicateManager.extractFormalParameters(formalParametersExpression,true);
		this.functionalExpression = functionalExpression;
		this.universalPostfixExpression = PredicateManager.buildUniversalPostfixExpression(functionalExpression, formalParameters);
	}

//	public String toString() {
//		return "  function " + name + " with functional expression = " + functionalExpression + " and (universal) postfix expression = " + Toolkit.buildStringFromTokens(universalPostfixExpression);
//	}

	public String toString() {
		String eParamExpr = String.join(",", effectiveParametersExpression.split(" "));
		return "function: " + functionalExpression + ", " +
				"params: " + "{" + eParamExpr + "}";
	}
}
