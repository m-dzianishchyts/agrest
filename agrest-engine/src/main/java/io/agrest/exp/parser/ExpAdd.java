/* Generated By:JJTree: Do not edit this line. ExpAdd.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

public
class ExpAdd extends SimpleNode {
  public ExpAdd(int id) {
    super(id);
  }

  public ExpAdd(AgExpressionParser p, int id) {
    super(p, id);
  }

  public ExpAdd() {
    super(AgExpressionParserTreeConstants.JJTADD);
  }

  /** Accept the visitor. **/
  public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {

    return
    visitor.visit(this, data);
  }

  @Override
  protected SimpleNode shallowCopy() {
    return new ExpAdd(id);
  }

  @Override
  public String toString() {
    return ExpStringConverter.convert(this);
  }
}
/* JavaCC - OriginalChecksum=67440c31da272c3cf98559aaecf49bc2 (do not edit this line) */
