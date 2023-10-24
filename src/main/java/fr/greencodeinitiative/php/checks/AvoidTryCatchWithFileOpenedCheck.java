/*
 * ecoCode - PHP language - Provides rules to reduce the environmental footprint of your PHP programs
 * Copyright © 2023 Green Code Initiative (https://www.ecocode.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.greencodeinitiative.php.checks;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.sonar.check.Rule;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.NamespaceNameTree;
import org.sonar.plugins.php.api.tree.expression.AssignmentExpressionTree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.statement.*;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@Rule(key = "EC35")
@DeprecatedRuleKey(repositoryKey = "gci-php", ruleKey = "S34")
@DeprecatedRuleKey(repositoryKey = "gci-php", ruleKey = "EC34")

public class AvoidTryCatchWithFileOpenedCheck extends PHPSubscriptionCheck {

    public static final String ERROR_MESSAGE = "Avoid the use of try-catch with a file open in try block";

    @Override
    public List<Tree.Kind> nodesToVisit() {
        return Collections.singletonList(Tree.Kind.TRY_STATEMENT);
    }

    @Override
    public void visitNode(Tree tree) {

        TryStatementTree tryStatement = (TryStatementTree) tree;

        // parcours des statements du bloc try
        visitStatementsList(tryStatement.block().statements());

    }

    private void visitStatementsList(List<StatementTree> lstStmts) {
        for (StatementTree stmt : lstStmts){
            if (stmt.is(Tree.Kind.EXPRESSION_STATEMENT)) { // seulement si Expression
                visitExpressionStatement(((ExpressionStatementTree) stmt).expression());
            } else if (stmt.is(Tree.Kind.BLOCK)) {
                visitStatementsList(((BlockTree) stmt).statements());
            } else if(stmt.is(Tree.Kind.IF_STATEMENT) || stmt.is(Tree.Kind.ALTERNATIVE_IF_STATEMENT)) {
                visitIfStatement((IfStatementTree) stmt);
            }else if(stmt.is(Tree.Kind.FOR_STATEMENT) || stmt.is(Tree.Kind.ALTERNATIVE_FOR_STATEMENT)) {
                visitStatementsList(((ForStatementTree) stmt).statements());
            } else if(stmt.is(Tree.Kind.WHILE_STATEMENT) || stmt.is(Tree.Kind.ALTERNATIVE_WHILE_STATEMENT)) {
                visitStatementsList(((WhileStatementTree) stmt).statements());
            } else if(stmt.is(Tree.Kind.DO_WHILE_STATEMENT)) {
                visitStatementsList(Arrays.asList(((DoWhileStatementTree) stmt).statement()));
            } else if(stmt.is(Tree.Kind.FOREACH_STATEMENT) || stmt.is(Tree.Kind.ALTERNATIVE_FOREACH_STATEMENT)) {
                visitStatementsList(((ForEachStatementTree) stmt).statements());
            } else if(stmt.is(Tree.Kind.CASE_CLAUSE)) {
                visitStatementsList(((CaseClauseTree) stmt).statements());
            } else if(stmt.is(Tree.Kind.SWITCH_STATEMENT)) {
                visitSwitchStatement((SwitchStatementTree) stmt);
            } else if(stmt.is(Tree.Kind.DEFAULT_CLAUSE)) {
                visitStatementsList(((DefaultClauseTree) stmt).statements());
            } else if(stmt.is(Tree.Kind.TRY_STATEMENT)) {
                visitTryStatement((TryStatementTree) stmt);
            }
        }
    }

    private void visitIfStatement(IfStatementTree ifStatement) {
        visitStatementsList(ifStatement.statements());

        if (ifStatement.elseClause() != null) {
            visitStatementsList(ifStatement.elseClause().statements());
        }

        if (ifStatement.elseifClauses() != null) {
            for (ElseifClauseTree stmtElseIf : ifStatement.elseifClauses()) {
                visitStatementsList(stmtElseIf.statements());
            }
        }
    }

    private void visitTryStatement(TryStatementTree tryStatement) {
        for (CatchBlockTree stmtCatch : tryStatement.catchBlocks()) {
            visitStatementsList(stmtCatch.block().statements());
        }

        if (tryStatement.finallyBlock() != null) {
            visitStatementsList(tryStatement.finallyBlock().statements());
        }
    }

    private void visitSwitchStatement(SwitchStatementTree switchStatement) {
        for (SwitchCaseClauseTree switchCaseStmt : switchStatement.cases()) {
            visitStatementsList(switchCaseStmt.statements());
        }
    }

    private void visitExpressionStatement(ExpressionTree exprTree){
        if (exprTree.is(Tree.Kind.FUNCTION_CALL)) { // si directement "function call"
            visitCallExpression((FunctionCallTree) exprTree);
        } else if (exprTree.is(Tree.Kind.ASSIGNMENT)) { // ou si assignment
            AssignmentExpressionTree assignTree = (AssignmentExpressionTree) exprTree;
            if (assignTree.value().is(Tree.Kind.FUNCTION_CALL)) { // et si "function call"
                visitCallExpression((FunctionCallTree) assignTree.value());
            }
        }
    }

    private void visitCallExpression(FunctionCallTree functionCall){
        String funcName = getFunctionNameFromCallExpression(functionCall);
        if (funcName.startsWith("PDF_open")
                || "fopen".equals(funcName)
                || "readfile".equals(funcName)) {
            context().newIssue(this, functionCall, ERROR_MESSAGE);
        }
    }

    private String getFunctionNameFromCallExpression(FunctionCallTree functionCall) {
        NamespaceNameTree nspTree = (NamespaceNameTree)functionCall.callee();
        return nspTree != null && nspTree.fullName() != null ? nspTree.fullName() : "";
    }

}
