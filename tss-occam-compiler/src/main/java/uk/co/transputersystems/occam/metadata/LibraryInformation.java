package uk.co.transputersystems.occam.metadata;

import java.util.ArrayList;
import java.util.List;

public class LibraryInformation implements VerifiableData {

    private String name;
    private List<DataType> dataTypes = new ArrayList<>();
    private List<FileInformation> fileInfos = new ArrayList<>();
    private FileInformation currentFileInfo;
    private Scope currentScope;
    private int scopeIdGenerator = 0;

    /**
     * Construct a LibraryInformation object.
     * @param name The name of the library.
     */
    public LibraryInformation(String name) {
        this.name = name;

        addDataType(new PrimitiveDataType("BOOL", 1));
        addDataType(new PrimitiveDataType("BYTE", 1));
        addDataType(new PrimitiveDataType("INT", 4)); //TODO: This size should be configurable based on the target platform word size
        addDataType(new PrimitiveDataType("INT16", 2));
        addDataType(new PrimitiveDataType("INT32", 4));

        //TODO: Uncomment when support for INT64/REAL32/REAL64 is actually added
        //addDataType(new PrimitiveDataType("INT64", 8));
        //addDataType(new PrimitiveDataType("REAL32", 4));
        //addDataType(new PrimitiveDataType("REAL64", 8));
    }

    /**
     * @return The name of the library
     */
    public String getName() {
        return name;
    }

    /**
     * @return The list of data types declared in all files within the library.
     *
     * Declared data types are any declared Array, Named, Primitive or Record types.
     */
    public List<DataType> getDataTypes() {
        return new ArrayList<>(dataTypes);
    }

    /**
     * Add a provided `DataType` to the list of data types.
     */
    public void addDataType(DataType dataType){
        dataTypes.add(dataType);
    }

    /**
     * Retrieve a `DataType` from the list of data types by name.
     * @return The first matching `DataType`
     */
    public DataType getDataType(String name) {
        for (DataType dataType : dataTypes) {
            if (dataType.getName().equals(name)) {
                return dataType;
            }
        }
        return null;
    }

    /**
     * @return `FileInformation` objects representing the files contained in this library
     */
    public List<FileInformation> getFileInfos() {
        return new ArrayList<>(fileInfos);
    }

    public void addFileInfo(FileInformation info) {
        fileInfos.add(info);
    }

    /**
     * Return the first `FileInformation` object which has a path matching the supplied path.
     * @param filePath The file path to search for. Format is absolute file path and uses the Java standard format for the current platform.
     * @return The `FileInformation` for the first matching file.
     */
    public FileInformation getFileInfo(String filePath) {
        for (FileInformation fileInfo : fileInfos) {
            if (fileInfo.getFilePath().equals(filePath)) {
                return fileInfo;
            }
        }
        return null;
    }

    public FileInformation getCurrentFileInfo() {
        return currentFileInfo;
    }

    public void startNewFile(String filePath, String fileName) {
        FileInformation newInfo = new FileInformation(null, filePath, fileName);
        fileInfos.add(newInfo);
        currentFileInfo = newInfo;
        currentScope = newInfo;
    }

    public Scope getScopeById(int id) {
        if (id == -1) {
            return null;
        }

        for (FileInformation file : fileInfos) {
            Scope potentialResult = file.getScopeById(id);
            if (potentialResult != null) {
                return potentialResult;
            }
        }

        return null;
    }
    public Scope getCurrentScope() {
        return currentScope;
    }
    public Scope pushNewScope() {
        if (currentScope instanceof FileInformation) {
            return currentScope;
        }
        return pushScope(new Scope(currentScope, scopeIdGenerator++, false));
    }
    public Scope pushNewFunctionScope(String name) {
        Function newFunction = new Function(currentScope, scopeIdGenerator++, name, new ArrayList<>(), new ArrayList<>());
        currentFileInfo.addFunction(newFunction);
        newFunction.newWorkspace(true);
        return pushScope(newFunction);
    }
    private Scope pushScope(Scope scope) {
        currentScope.addChild(scope);
        currentScope = scope;
        return currentScope;
    }
    public void popScope() {
        currentScope = currentScope.parent;
    }

    public Function getFunction(String name) {
        for (FileInformation fileInfo : fileInfos) {
            Function potentialResult = fileInfo.getFunction(name);
            if (potentialResult != null) {
                return potentialResult;
            }
        }
        return null;
    }

    /**
     * Search for the provided name in the current scope and its parent scopes until:
     *
     * * the name is found, in which case return the relevant `NamedOperand`
     * * we have reached the final parent, in which case return null
     *
     * @param name An operand name to search for in the current scope
     * @return A `NamedOperand` matching the provided name, or null
     */
    public NamedOperand searchForNamedOperand(String name) {
        return searchForNamedOperand(name, this.currentScope);
    }
    public static NamedOperand searchForNamedOperand(String name, Scope bottomScope) {
        // Search for the name in the current scope. If it is not found, recursively search
        // the parent scopes. When the name is found, return the relevant object
        while (bottomScope != null) {
            NamedOperand result = bottomScope.searchForNamedOperand(name);
            if (result != null) {
                return result;
            }
            bottomScope = bottomScope.parent;
        }

        // If nothing is found, return null
        return null;
    }

    public NamedOperand searchForGlobalNamedOperand(String name) {
        for (FileInformation fileInfo : fileInfos) {
            NamedOperand result = searchForGlobalNamedOperand(name, fileInfo);
            if (result != null) {
                return result;
            }
        }

        // If nothing is found, return null
        return null;
    }
    private NamedOperand searchForGlobalNamedOperand(String name, Scope scope) {
        NamedOperand result = scope.searchForNamedOperand(name);
        if (result != null) {
            return result;
        }

        for (Scope childScope : scope.children) {
            result = searchForGlobalNamedOperand(name, childScope);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    public boolean isGlobal(NamedOperand operand) {
        //TODO: Should global variables be shared across files, like data types are?
        //  If so, this function should search all files
        //  In which case, we need to process global parts of files before the functions within them
        return currentFileInfo.searchForNamedOperand(operand.getName()) == operand;
    }

    public int getTypeSize(String typeName) throws Exception {
        return getTypeSize(getDataType(typeName));
    }
    public int getTypeSize(DataType type) throws Exception {
        if (type instanceof PrimitiveDataType) {
            return ((PrimitiveDataType) type).getSize();
        } else if (type instanceof NamedDataType) {
            return getTypeSize(((NamedDataType) type).getUnderlyingTypeName());
        } else if (type instanceof ArrayDataType) {
            int elementSize = getTypeSize(((ArrayDataType) type).getElementTypeName());
            int size = elementSize;
            for (Integer dimension : ((ArrayDataType) type).getDimensions()) {
                size *= dimension;
            }
            return size;
        } else if (type instanceof RecordDataType) {
            int size = 0;
            for(Field field : ((RecordDataType) type).getFields()) {
                size += getTypeSize(field.getTypeName());
            }
            return size;
        } else {
            throw new Exception("Unknown data type descriptor type! " + type.getClass().getName());
        }
    }

    public void verify(VerificationContext ctx) {
        assert (name != null);
        assert (!name.isEmpty());

        for (DataType aType : dataTypes) {
            assert (aType != null);
            aType.verify(ctx);
        }

        List<String> usedPaths = new ArrayList<>();
        for (FileInformation fileInfo : fileInfos) {
            assert (fileInfo != null);
            fileInfo.verify(ctx);

            assert (!usedPaths.contains(fileInfo.getFilePath()));
            usedPaths.add(fileInfo.getFilePath());
        }

        assert (currentScope == null);
    }
}
