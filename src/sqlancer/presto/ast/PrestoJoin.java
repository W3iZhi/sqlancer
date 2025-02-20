package sqlancer.presto.ast;

import java.util.ArrayList;
import java.util.List;

import sqlancer.Randomly;
import sqlancer.common.ast.JoinBase;
import sqlancer.common.ast.newast.Join;
import sqlancer.presto.PrestoGlobalState;
import sqlancer.presto.PrestoSchema;
import sqlancer.presto.PrestoSchema.PrestoColumn;
import sqlancer.presto.PrestoSchema.PrestoTable;
import sqlancer.presto.gen.PrestoTypedExpressionGenerator;

public class PrestoJoin extends JoinBase<PrestoExpression> implements PrestoExpression, Join<PrestoExpression, PrestoTable, PrestoColumn> {

    private final PrestoTableReference leftTable;
    private final PrestoTableReference rightTable;
    private OuterType outerType;

    public PrestoJoin(PrestoTableReference leftTable, PrestoTableReference rightTable, JoinType joinType,
            PrestoExpression whereCondition) {
        super(joinType, whereCondition);
        this.leftTable = leftTable;
        this.rightTable = rightTable;
    }

    public static List<PrestoJoin> getJoins(List<PrestoTableReference> tableList, PrestoGlobalState globalState) {
        List<PrestoJoin> joinExpressions = new ArrayList<>();
        while (tableList.size() >= 2 && Randomly.getBooleanWithRatherLowProbability()) {
            PrestoTableReference leftTable = tableList.remove(0);
            PrestoTableReference rightTable = tableList.remove(0);
            List<PrestoColumn> columns = new ArrayList<>(leftTable.getTable().getColumns());
            columns.addAll(rightTable.getTable().getColumns());
            PrestoTypedExpressionGenerator joinGen = new PrestoTypedExpressionGenerator(globalState)
                    .setColumns(columns);
            switch (JoinType.getRandomForDatabase("PRESTO")) {
            case INNER:
                joinExpressions.add(PrestoJoin.createInnerJoin(leftTable, rightTable, joinGen.generateExpression(
                        PrestoSchema.PrestoCompositeDataType.fromDataType(PrestoSchema.PrestoDataType.BOOLEAN))));
                break;
            case LEFT:
                joinExpressions.add(PrestoJoin.createLeftOuterJoin(leftTable, rightTable, joinGen.generateExpression(
                        PrestoSchema.PrestoCompositeDataType.fromDataType(PrestoSchema.PrestoDataType.BOOLEAN))));
                break;
            case RIGHT:
                joinExpressions.add(PrestoJoin.createRightOuterJoin(leftTable, rightTable, joinGen.generateExpression(
                        PrestoSchema.PrestoCompositeDataType.fromDataType(PrestoSchema.PrestoDataType.BOOLEAN))));
                break;
            default:
                throw new AssertionError();
            }
        }
        return joinExpressions;
    }

    public static PrestoJoin createRightOuterJoin(PrestoTableReference left, PrestoTableReference right,
            PrestoExpression predicate) {
        return new PrestoJoin(left, right, JoinType.RIGHT, predicate);
    }

    public static PrestoJoin createLeftOuterJoin(PrestoTableReference left, PrestoTableReference right,
            PrestoExpression predicate) {
        return new PrestoJoin(left, right, JoinType.LEFT, predicate);
    }

    public static PrestoJoin createInnerJoin(PrestoTableReference left, PrestoTableReference right,
            PrestoExpression predicate) {
        return new PrestoJoin(left, right, JoinType.INNER, predicate);
    }

    public PrestoTableReference getLeftTable() {
        return leftTable;
    }

    public PrestoTableReference getRightTable() {
        return rightTable;
    }

    public JoinType getJoinType() {
        return type;
    }

    public PrestoExpression getOnCondition() {
        return onClause;
    }

    public OuterType getOuterType() {
        return outerType;
    }

    @SuppressWarnings("unused")
    private void setOuterType(OuterType outerType) {
        this.outerType = outerType;
    }

    public enum OuterType {
        FULL, LEFT, RIGHT;

        public static OuterType getRandom() {
            return Randomly.fromOptions(values());
        }
    }

    @Override
    public void setOnClause(PrestoExpression onClause) {
        super.onClause = onClause;
    }

}
