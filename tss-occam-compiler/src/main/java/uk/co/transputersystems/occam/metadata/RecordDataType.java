package uk.co.transputersystems.occam.metadata;

import java.util.ArrayList;
import java.util.List;

public class RecordDataType extends DataType {
    private List<Field> fields = new ArrayList<>();

    public RecordDataType(String name) {
        super(name);
    }

    public List<Field> getFields() {
        return new ArrayList<>(fields);
    }
    public void addField(String name, String typeName) {
        fields.add(new Field(name, typeName, fields.size()));
    }
    public Field getField(String name) {
        for (Field field : fields) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    @Override
    public void verify(VerificationContext ctx) {
        super.verify(ctx);

        for (Field field : fields) {
            assert (field != null);
            field.verify(ctx);
        }
    }
}
