package uk.co.transputersystems.occam;

import uk.co.transputersystems.occam.il.*;

/**
 * Visitor for converting `ILOp<TIdentifier>`s to `ILOp<Integer>`s, where the `Integer` is the offset of that `ILOp` in the
 * `ILBlock` passed as context to the `visit()` method.
 * @param <TIdentifier> Usually `UUID`, but this may change if the unique `ILOp` identifiers change.
 */
public class ReferencedTSILGenerator<TIdentifier> extends ILOpVisitor<ILOp<Integer>,TIdentifier,ILBlock<TIdentifier,? extends ILOp<TIdentifier>>> {

    @Override
    public ILOp<Integer> visitMethodStart(MethodStart<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new MethodStart<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitMethodEnd(MethodEnd<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new MethodEnd<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitStartProcess(StartProcess<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new StartProcess<>(ctx.getOffset(op), ctx.getOffset(op.firstILOpID), op.newPriority, op.getComment());
    }

    @Override
    public ILOp<Integer> visitEndProcess(EndProcess<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new EndProcess<>(ctx.getOffset(op), ctx.getOffset(op.creatorILOpID), op.getComment());
    }

    @Override
    public ILOp<Integer> visitInitProcesses(InitProcesses<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new InitProcesses<>(ctx.getOffset(op), op.numProcesses, ctx.getOffset(op.continueILOpID), op.workspaceIds, op.getComment());
    }

    @Override
    public ILOp<Integer> visitSkip(Skip<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new Skip<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitLabel(Label<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new Label<>(ctx.getOffset(op), op.label, op.isGlobal, op.getComment());
    }

    @Override
    public ILOp<Integer> visitLoadConstant(LoadConstant<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new LoadConstant<>(ctx.getOffset(op), op.value, op.getComment());
    }

    @Override
    public ILOp<Integer> visitLoadLocal(LoadLocal<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new LoadLocal<>(ctx.getOffset(op), op.index, op.getComment(), op.loadAddress);
    }

    @Override
    public ILOp<Integer> visitLoadArgument(LoadArgument<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new LoadArgument<>(ctx.getOffset(op), op.index, op.getComment(), op.loadAddress);
    }

    @Override
    public ILOp<Integer> visitLoadGlobal(LoadGlobal<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new LoadGlobal<>(ctx.getOffset(op), op.globalName, op.getComment(), op.loadAddress);
    }

    @Override
    public ILOp<Integer> visitStoreLocal(StoreLocal<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new StoreLocal<>(ctx.getOffset(op), op.index, op.getComment());
    }

    @Override
    public ILOp<Integer> visitStoreArgument(StoreArgument<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new StoreArgument<>(ctx.getOffset(op), op.index, op.getComment());
    }

    @Override
    public ILOp<Integer> visitStoreGlobal(StoreGlobal<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new StoreGlobal<>(ctx.getOffset(op), op.globalName, op.getComment());
    }

    @Override
    public ILOp<Integer> visitReadChannel(ReadChannel<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new ReadChannel<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitAdd(Add<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new Add<>(ctx.getOffset(op), op.ignore_overflow, op.getComment());
    }

    @Override
    public ILOp<Integer> visitSubtract(Subtract<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new Subtract<>(ctx.getOffset(op), op.ignore_overflow, op.getComment());
    }

    @Override
    public ILOp<Integer> visitMultiply(Multiply<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new Multiply<>(ctx.getOffset(op), op.ignore_overflow, op.getComment());
    }

    @Override
    public ILOp<Integer> visitDuplicate(Duplicate<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new Duplicate<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitUnaryMinus(UnaryMinus<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new UnaryMinus<>(ctx.getOffset(op) , op.ignore_overflow , op.getComment());
    }

    @Override
    public ILOp<Integer> visitBranchEqZero(BranchEqZero<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new BranchEqZero<>(ctx.getOffset(op), ctx.getOffset(op.target), op.getComment());
    }

    @Override
    public ILOp<Integer> visitBranchNotEqZero(BranchNotEqZero<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new BranchNotEqZero<>(ctx.getOffset(op), ctx.getOffset(op.target), op.getComment());
    }

    @Override
    public ILOp<Integer> visitBranch(Branch<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new Branch<>(ctx.getOffset(op), ctx.getOffset(op.target), op.getComment());
    }

    @Override
    public ILOp<Integer> visitCompareGreaterThan(CompareGreaterThan<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new CompareGreaterThan<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitCompareGreaterThanOrEqual(CompareGreaterThanOrEqual<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new CompareGreaterThanOrEqual<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitBranchIfTrue(BranchIfTrue<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new BranchIfTrue<>(ctx.getOffset(op), ctx.getOffset(op.target), op.getComment());
    }

    @Override
    public ILOp<Integer> visitBranchIfFalse(BranchIfFalse<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new BranchIfFalse<>(ctx.getOffset(op), ctx.getOffset(op.target), op.getComment());
    }

    @Override
    public ILOp<Integer> visitCompareLessThan(CompareLessThan<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new CompareLessThan<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitCompareLessThanOrEqual(CompareLessThanOrEqual<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new CompareLessThanOrEqual<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitCompareEqual(CompareEqual<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new CompareEqual<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitCompareNotEqual(CompareNotEqual<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new CompareNotEqual<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitBitwiseAnd(BitwiseAnd<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new BitwiseAnd<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitBitwiseOr(BitwiseOr<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new BitwiseOr<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitBitwiseNot(BitwiseNot<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new BitwiseNot<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitBooleanNot(BooleanNot<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new BooleanNot<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitDivide(Divide<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new Divide<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitModulo(Modulo<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new Modulo<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitBitwiseXor(BitwiseXor<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new BitwiseXor<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitCall(Call<TIdentifier> op, ILBlock<TIdentifier,? extends ILOp<TIdentifier>> ctx) {
        return new Call<>(ctx.getOffset(op), op.functionName, op.getComment());
    }

    @Override
    public ILOp<Integer> visitWriteChannel(WriteChannel<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new WriteChannel<>(ctx.getOffset(op) , op.getComment());
    }

    @Override
    public ILOp<Integer> visitReadPort(ReadPort<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new ReadPort<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitWritePort(WritePort<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new WritePort<>(ctx.getOffset(op),  op.getComment());
    }

    @Override
    public ILOp<Integer> visitReadTimer(ReadTimer<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new ReadTimer<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitInitChannel(InitChannel<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new InitChannel<>(ctx.getOffset(op), op.name, op.index , op.typeName ,  op.getComment());
    }

    @Override
    public ILOp<Integer> visitLoadChannelRef(LoadChannelRef<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new LoadChannelRef<>(ctx.getOffset(op) , op.index, op.getComment());
    }

    @Override
    public ILOp<Integer> visitLoadPortRef(LoadPortRef<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new LoadPortRef<>(ctx.getOffset(op) , op.index, op.name, op.typeName , op.getComment());
    }

    @Override
    public ILOp<Integer> visitDelayedTimerInput(DelayedTimerInput<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new DelayedTimerInput<>(ctx.getOffset(op), op.getComment());
    }


    @Override
    public ILOp<Integer> visitAfter(After<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new After<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitLeftShift(LeftShift<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new LeftShift<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitRightShift(RightShift<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new RightShift<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitDisableChannel(DisableChannel<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new DisableChannel<>( ctx.getOffset(op), ctx.getOffset(op.target) , ctx.getOffset(op.endAlt) , op.getComment());
    }

    @Override
    public ILOp<Integer> visitDisableSkip(DisableSkip<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new DisableSkip<>( ctx.getOffset(op), ctx.getOffset(op.target), ctx.getOffset(op.endAlt) , op.getComment());
    }

    @Override
    public ILOp<Integer> visitDisableTimer(DisableTimer<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new DisableTimer<>( ctx.getOffset(op), ctx.getOffset(op.target), ctx.getOffset(op.endAlt) , op.getComment());
    }

    @Override
    public ILOp<Integer> visitEnableChannel(EnableChannel<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new EnableChannel<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitEnableSkip(EnableSkip<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new EnableSkip<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitEnableTimer(EnableTimer<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new EnableTimer<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitInitAlt(InitAlt<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new InitAlt<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitEndAlt(EndAlt<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new EndAlt<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitWaitAlt(WaitAlt<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new WaitAlt<>(ctx.getOffset(op), op.getComment());
    }

    @Override
    public ILOp<Integer> visitDisablePort(DisablePort<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new DisablePort<>(ctx.getOffset(op),  ctx.getOffset(op.target) , ctx.getOffset(op.endAlt)  , op.getComment());
    }

    @Override
    public ILOp<Integer> visitEnablePort(EnablePort<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new EnablePort<>(ctx.getOffset(op), op.getComment());
    }


    @Override
    public ILOp<Integer> visitMostNegative(MostNegative<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new MostNegative<>(ctx.getOffset(op), op.type ,  op.getComment());
    }

    @Override
    public ILOp<Integer> visitMostPositive(MostPositive<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new MostPositive<>(ctx.getOffset(op), op.type ,  op.getComment());
    }


    @Override
    public ILOp<Integer> visitBooleanAnd(BooleanAnd<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new BooleanAnd<>(ctx.getOffset(op), op.getComment());
    }


    @Override
    public ILOp<Integer> visitBooleanOr(BooleanOr<TIdentifier> op, ILBlock<TIdentifier, ? extends ILOp<TIdentifier>> ctx) {
        return new BooleanOr<>(ctx.getOffset(op) , op.getComment());
    }
}


