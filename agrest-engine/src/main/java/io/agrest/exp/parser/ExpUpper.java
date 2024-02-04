/* Generated By:JJTree: Do not edit this line. ExpUpper.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=Exp,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package io.agrest.exp.parser;

public
class ExpUpper extends SimpleNode {
  public ExpUpper(int id) {
    super(id);
  }

  public ExpUpper(AgExpressionParser p, int id) {
    super(p, id);
  }

  public ExpUpper() {
    super(AgExpressionParserTreeConstants.JJTUPPER);
  }


  /** Accept the visitor. **/
  public <T> T jjtAccept(AgExpressionParserVisitor<T> visitor, T data) {

    return
    visitor.visit(this, data);
  }

  @Override
  protected ExpUpper shallowCopy() {
    return new ExpUpper(id);
  }

  @Override
  public String toString() {
    return children != null
            ? "upper(" + children[0] + ")"
            : "upper(?)";
  }
}
/* JavaCC - OriginalChecksum=579bc67a9a18bf15184b23f2649cc451 (do not edit this line) */
