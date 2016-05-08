package uk.co.transputersystems.occam;

import uk.co.transputersystems.occam.il.ILBlock;
import uk.co.transputersystems.occam.il.ILOp;
import uk.co.transputersystems.occam.metadata.LibraryInformation;
import uk.co.transputersystems.occam.open_transputer.ASMBlock;
import uk.co.transputersystems.occam.open_transputer.ASMGenerator;
import uk.co.transputersystems.occam.open_transputer.ASMGeneratorContext;
import uk.co.transputersystems.occam.open_transputer.ASMOpFormatter;
import uk.co.transputersystems.occam.open_transputer.assembly.ASMOp;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class OccamCompiler {

    public void writeILBlocks(FileWriter outputILWriter, List<ILBlock<Integer, ILOp<Integer>>> ilBlocks) throws IOException {
        for (ILBlock block : ilBlocks) {
            outputILWriter.write(block.toString());
        }
        outputILWriter.flush();
    }

    public void writeASMBlocks(Writer outputWriter, List<ASMBlock> asmBlocks) throws IOException {
        for (ASMBlock block : asmBlocks) {
            for (ASMOp op : block.getOps()) {
                outputWriter.write(ASMOpFormatter.formatOp(op));
                outputWriter.write('\n');
            }
            outputWriter.write('\n');
        }
        outputWriter.flush();
    }

    public void writeDivider(Writer out) throws IOException {
        out.write("-------------------------------------------------------------------------\n");
    }

    public List<ILBlock<UUID, ILOp<UUID>>> generateTSIL(TSILGenerator tsilGenerator, ParseTree tree, String filePath, String fileName)  {
        LibraryInformation libraryInfo = tsilGenerator.getLibraryInfo();
        libraryInfo.startNewFile(filePath, fileName);
        return tsilGenerator.visit(tree);

    }

    public List<ILBlock<Integer, ILOp<Integer>>> generateReferencedTSIL(@Nonnull ReferencedTSILGenerator<UUID> tsilGenerator, @Nonnull List<ILBlock<UUID, ILOp<UUID>>> ilBlocks) {
        List<ILBlock<Integer,ILOp<Integer>>> referencedIlBlocks = new ArrayList<>();
        for (ILBlock<UUID, ILOp<UUID>> ilBlock : ilBlocks) {
            ILBlock<Integer, ILOp<Integer>> newBlock = new ILBlock<>(ilBlock.getScopeId(), ilBlock.isFunctionBlock());
            for (ILOp<UUID> op : ilBlock.getAll()) {
                newBlock.add(tsilGenerator.visit(op, ilBlock));
            }
            referencedIlBlocks.add(newBlock);
        }
        return referencedIlBlocks;
    }

    public List<ASMBlock> generateASM(@Nonnull ASMGenerator asmGenerator, @Nonnull List<ILBlock<Integer, ILOp<Integer>>> ilBlocks, ASMGeneratorContext asmGeneratorContext, boolean suppressErrors) throws Exception {
        List<ASMBlock> asmBlocks = asmGenerator.generateASM(ilBlocks, asmGeneratorContext, suppressErrors);
        asmBlocks.add(asmGeneratorContext.getGlobalsBlock());
        return asmBlocks;
    }

    public ParseTree makeParseTree(InputStream inputStream, Function<OccamParser, ParseTree> startRule, ErrorListener errorListener) {
        try {
            ANTLRInputStream antlrInputStream = new ANTLRInputStream(inputStream);
            OccamLexer synLexer = new OccamLexer(antlrInputStream);

            CommonTokenStream tokenStream = new CommonTokenStream(synLexer);
            OccamParser occamParser = new OccamParser(tokenStream);
            occamParser.addErrorListener(errorListener);

            return startRule.apply(occamParser);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public void printParseTree(PrintWriter out, ParseTree tree) {
        new OccamParser(null);

        ParseTreePretty listener = new ParseTreePretty(new OccamParser(null));
        ParseTreeWalker.DEFAULT.walk(listener,tree);
        out.println(listener.toString());
    }

}
