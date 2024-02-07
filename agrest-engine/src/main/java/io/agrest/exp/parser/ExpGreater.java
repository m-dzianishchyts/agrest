/* Generated By:JJTree: Do not edit this line. ExpGreater.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

public
class ExpGreater extends SimpleNode {
  public ExpGreater(int id) {
    super(id);
  }

  public ExpGreater(AgExpressionParser p, int id) {
    super(p, id);
  }

  public ExpGreater() {
    super(AgExpressionParserTreeConstants.JJTGREATER);
  }

  /** Accept the visitor. **/
  public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {

    return
    visitor.visit(this, data);
  }

  @Override
  protected ExpGreater shallowCopy() {
    return new ExpGreater(id);
  }

  @Override
  public String toString() {
    return ExpStringConverter.convert(this);
  }
}
/* JavaCC - OriginalChecksum=ea0ce31b9d9db21857900434d14bd004 (do not edit this line) */
