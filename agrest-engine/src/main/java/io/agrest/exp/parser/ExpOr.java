/* Generated By:JJTree: Do not edit this line. ExpOr.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

import io.agrest.protocol.Exp;

public class ExpOr extends SimpleNode {

    public ExpOr(int id) {
        super(id);
    }

    public ExpOr(AgExpressionParser p, int id) {
        super(p, id);
    }

    /**
     * Accept the visitor.
     **/
    public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {
        return visitor.visit(this, data);
    }

    @Override
    public Exp or(Exp exp) {
        if (exp == null) {
            return this;
        }
        jjtAddChild((Node) exp, jjtGetNumChildren());
        return this;
    }
}
/* JavaCC - OriginalChecksum=3faa5d28a1281e9ded5ee704fae0f47d (do not edit this line) */
