/* Generated By:JJTree: Do not edit this line. ExpMod.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

public
class ExpMod extends SimpleNode {
    public ExpMod(int id) {
        super(id);
    }

  public ExpMod(AgExpressionParser p, int id) {
    super(p, id);
  }

  public ExpMod() {
    super(AgExpressionParserTreeConstants.JJTMOD);
  }

    /**
     * Accept the visitor.
     **/
    public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {

        return
                visitor.visit(this, data);
    }

    @Override
    protected SimpleNode shallowCopy() {
        return new ExpMod(id);
    }

    @Override
    public String toString() {
        return ExpStringConverter.convert(this);
    }
}
/* JavaCC - OriginalChecksum=6563b330904d626ec86b2bd306259dd3 (do not edit this line) */
