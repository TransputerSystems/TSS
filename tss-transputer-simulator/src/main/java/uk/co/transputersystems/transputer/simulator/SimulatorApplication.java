package uk.co.transputersystems.transputer.simulator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by Ed on 08/05/2016.
 */
public class SimulatorApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

        String[] args = new String[getParameters().getRaw().size()];
        args = getParameters().getRaw().toArray(args);

        Simulator.run(args);

        Platform.exit();
    }
    public static void main(String[] args) throws IOException, UnexpectedOverflowException {
        Application.launch(args);
    }
}
