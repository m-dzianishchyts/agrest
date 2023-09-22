/* Generated By:JJTree: Do not edit this line. ExpNot.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

import io.agrest.protocol.Exp;

public
class ExpNot extends ExpCondition {
  public ExpNot(int id) {
    super(id);
  }

  public ExpNot(AgExpressionParser p, int id) {
    super(p, id);
  }

  public ExpNot() {
    super(AgExpressionParserTreeConstants.JJTNOT);
  }

  /** Accept the visitor. **/
  public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {

    return
    visitor.visit(this, data);
  }

  @Override
  protected ExpNot shallowCopy() {
    return new ExpNot(id);
  }

  @Override
  public Exp not() {
    return jjtGetNumChildren() > 0 ? jjtGetChild(0) : new ExpTrue();
  }

  @Override
  public String toString() {
    return children != null
            ? "not (" + children[0] + ")"
            : "not ?";
  }
}
/* JavaCC - OriginalChecksum=0fe2b3f475a020609f2cfa348e9fe03d (do not edit this line) */
