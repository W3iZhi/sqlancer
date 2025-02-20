package sqlancer.datafusion.ast;

import java.util.ArrayList;
import java.util.List;

import sqlancer.Randomly;
import sqlancer.common.ast.JoinBase;
import sqlancer.common.ast.newast.Join;
import sqlancer.datafusion.DataFusionProvider.DataFusionGlobalState;
import sqlancer.datafusion.DataFusionSchema;
import sqlancer.datafusion.DataFusionSchema.DataFusionColumn;
import sqlancer.datafusion.DataFusionSchema.DataFusionTable;
import sqlancer.datafusion.gen.DataFusionExpressionGenerator;

/*
    NOT IMPLEMENTED YET
 */
public class DataFusionJoin extends JoinBase<DataFusionExpression>
        implements DataFusionExpression, Join<DataFusionExpression, DataFusionTable, DataFusionColumn> {

    private final DataFusionTableReference leftTable;
    private final DataFusionTableReference rightTable;

    public DataFusionJoin(DataFusionTableReference leftTable, DataFusionTableReference rightTable, JoinType joinType,
            DataFusionExpression whereCondition) {
        super(joinType, whereCondition);
        this.leftTable = leftTable;
        this.rightTable = rightTable;

    }

    public static List<DataFusionJoin> getJoins(List<DataFusionTableReference> tableList,
            DataFusionGlobalState globalState) {
        // [t1_join_t2, t1_join_t3, ...]
        List<DataFusionJoin> joinExpressions = new ArrayList<>();
        while (tableList.size() >= 2 && Randomly.getBooleanWithRatherLowProbability()) {
            DataFusionTableReference leftTable = tableList.remove(0);
            DataFusionTableReference rightTable = tableList.remove(0);
            List<DataFusionColumn> columns = new ArrayList<>(leftTable.getTable().getColumns());
            columns.addAll(rightTable.getTable().getColumns());
            // TODO(datafusion) this `joinGen` can generate super chaotic exprsions, maybe we should make it more like a
            // normal join expression
            DataFusionExpressionGenerator joinGen = new DataFusionExpressionGenerator(globalState).setColumns(columns);
            switch (DataFusionJoin.JoinType.getRandomForDatabase("DATAFUSION")) {
            case INNER:
                joinExpressions.add(DataFusionJoin.createInnerJoin(leftTable, rightTable,
                        joinGen.generateExpression(DataFusionSchema.DataFusionDataType.BOOLEAN)));
                break;
            default:
                throw new AssertionError();
            }
        }
        return joinExpressions;
    }

    public static DataFusionJoin createInnerJoin(DataFusionTableReference left, DataFusionTableReference right,
            DataFusionExpression predicate) {
        return new DataFusionJoin(left, right, JoinType.INNER, predicate);
    }

    public DataFusionTableReference getLeftTable() {
        return leftTable;
    }

    public DataFusionTableReference getRightTable() {
        return rightTable;
    }

    public JoinType getJoinType() {
        return type;
    }

    public DataFusionExpression getOnCondition() {
        return onClause;
    }

    @Override
    public void setOnClause(DataFusionExpression onClause) {
        super.onClause = onClause;
    }
}
