/* Generated By:JJTree: Do not edit this line. ExpScalarFloat.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

public class ExpScalarFloat extends ExpScalar<Number> {
    public ExpScalarFloat(int id) {
        super(id);
    }

    public ExpScalarFloat(AgExpressionParser p, int id) {
        super(p, id);
    }

    /**
     * Accept the visitor.
     **/
    public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=dd3177da4e27de8af57d50aee8cf2d0b (do not edit this line) */