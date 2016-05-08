package uk.co.transputersystems.transputer.assembler.config;

public class AssemblerConfig {
    public Processor getProcessor() {
        return processor;
    }

    public void setProcessor(Processor processor) {
        this.processor = processor;
    }

    private Processor processor;

    public AssemblerConfig() {
        processor = new Processor();
    }
}
