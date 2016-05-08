package uk.co.transputersystems.occam.metadata;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileInformation extends Scope {

    private String filePath;
    private String fileName;

    private List<Function> functions = new ArrayList<>();

    public FileInformation(Scope parent, String filePath, String fileName) {
        super(parent, -1, false);

        this.filePath = filePath;
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }
    
    public List<Function> getFunctions() {
        return new ArrayList<>(functions);
    }
    public void addFunction(Function function){
        functions.add(function);
    }
    public Function getFunction(String name) {
        for (Function function : functions) {
            if (function.getName().equals(name)) {
                return function;
            }
        }
        return null;
    }

    @Override
    public void verify(VerificationContext ctx) {
        super.verify(ctx);

        assert (filePath != null);
        assert (!filePath.isEmpty());

        File f = new File(filePath);
        assert (f.exists());
        assert (!f.isDirectory());

        assert (fileName != null);
        assert (!fileName.isEmpty());

        assert (functions != null);
        List<String> usedNames = new ArrayList<>();
        for (Function function : functions) {
            assert (function != null);
            function.verify(ctx);

            assert (!usedNames.contains(function.getName()));
            usedNames.add(function.getName());
        }
    }
}
