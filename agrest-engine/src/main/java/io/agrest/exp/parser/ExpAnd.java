/* Generated By:JJTree: Do not edit this line. ExpAnd.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

import io.agrest.exp.AgExpression;
import io.agrest.protocol.Exp;

public
class ExpAnd extends ExpCondition {
  public ExpAnd(int id) {
    super(id);
  }

  public ExpAnd(AgExpressionParser p, int id) {
    super(p, id);
  }

  @Override
  public Exp and(Exp exp) {
    if (exp == null) {
      return this;
    }
    jjtAddChild((Node) exp, jjtGetNumChildren());
    return this;
  }

  /** Accept the visitor. **/
  public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {

    return
    visitor.visit(this, data);
  }

  @Override
  protected AgExpression shallowCopy() {
    return new ExpAnd(id);
  }

  @Override
  public String toString() {
    return children[0] + " and " + children[1];
  }
}
/* JavaCC - OriginalChecksum=8fe8825e52daaa8c28bda97ebce7e8ec (do not edit this line) */
